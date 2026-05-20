package io.example.merchant_detail;

import io.example.common.config.AppConfig;
import io.example.common.config.RedisConfig;
import io.example.common.config.TelemetryConfig;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_detail.handler.MerchantDetailCommandHandler;
import io.example.merchant_detail.handler.MerchantDetailQueryHandler;
import io.example.merchant_detail.handler.MerchantSocialCommandHandler;
import io.example.merchant_detail.repository.MerchantDetailCommandRepository;
import io.example.merchant_detail.repository.MerchantDetailQueryRepository;
import io.example.merchant_detail.repository.MerchantQueryRepository;
import io.example.merchant_detail.repository.MerchantSocialLinkCommandRepository;
import io.example.merchant_detail.repository.impl.MerchantDetailCommandRepositoryImpl;
import io.example.merchant_detail.repository.impl.MerchantDetailQueryRepositoryImpl;
import io.example.merchant_detail.repository.impl.MerchantQueryRepositoryImpl;
import io.example.merchant_detail.repository.impl.MerchantSocialLinkCommandRepositoryImpl;
import io.example.merchant_detail.service.MerchantDetailCommandService;
import io.example.merchant_detail.service.MerchantDetailQueryService;
import io.example.merchant_detail.service.MerchantSocialLinkCommandService;
import io.example.merchant_detail.service.impl.MerchantDetailCommandServiceImpl;
import io.example.merchant_detail.service.impl.MerchantDetailQueryServiceImpl;
import io.example.merchant_detail.service.impl.MerchantSocialLinkCommandServiceImpl;
import io.opentelemetry.api.OpenTelemetry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MerchantDetailVerticle extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(MerchantDetailVerticle.class);

  private TelemetryConfig telemetryConfig;
  private GrpcClient grpcClient;

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    JsonObject config = new JsonObject()
        .put("database", new JsonObject()
            .put("host", "localhost")
            .put("port", 5432)
            .put("database", "vertxdb")
            .put("user", "vertx")
            .put("password", "vertx")
            .put("pool_size", 5))
        .put("grpc_port", 50058)
        .put("service.name", "merchant-detail-service");

    DeploymentOptions options = new DeploymentOptions().setConfig(config);

    vertx.deployVerticle(new MerchantDetailVerticle(), options)
        .onSuccess(id -> {
          log.info("✅ MerchantDetail Service successfully deployed! ID: {}", id);
          log.info("🚀 gRPC Server running on port 50058");
        })
        .onFailure(err -> {
          log.error("❌ Failed to deploy MerchantDetailVerticle", err);
        });
  }

  @Override
  public void start(Promise<Void> startPromise) {
    JsonObject rawConfig = config();

    // 1. Initialize Telemetry
    JsonObject telConfig = rawConfig.copy();
    if (!telConfig.containsKey("service.name")) {
      telConfig.put("service.name", "merchant-detail-service");
    }
    telemetryConfig = new TelemetryConfig(telConfig);
    OpenTelemetry openTelemetry = telemetryConfig.initialize();
    TracingMetrics tracingMetrics = new TracingMetrics(openTelemetry, "merchant-detail-service");

    // 2. Initialize Repositories
    AppConfig cfg = AppConfig.from(rawConfig);
    var dbCfg = cfg.getDatabaseConfig();

    PgConnectOptions connectOptions = new PgConnectOptions()
        .setHost(dbCfg.getString("host", "localhost"))
        .setPort(dbCfg.getInteger("port", 5432))
        .setDatabase(dbCfg.getString("database", "vertxdb"))
        .setUser(dbCfg.getString("user", "vertx"))
        .setPassword(dbCfg.getString("password", "vertx"));

    PoolOptions poolOptions = new PoolOptions()
        .setMaxSize(dbCfg.getInteger("pool_size", 5));

    Pool pool = Pool.pool(vertx, connectOptions, poolOptions);

    // 3. Initialize unified gRPC Client pool & clients
    grpcClient = GrpcClient.client(vertx);
    SocketAddress addrMerchant = resolveGrpcAddress("MERCHANT", "merchant", 50055);
    var merchantQueryClient = new pb.merchant.VertxMerchantQueryServiceGrpcClient(grpcClient, addrMerchant);

    MerchantDetailQueryRepository queryRepo = new MerchantDetailQueryRepositoryImpl(pool);
    MerchantDetailCommandRepository cmdRepo = new MerchantDetailCommandRepositoryImpl(pool);
    MerchantSocialLinkCommandRepository socialRepo = new MerchantSocialLinkCommandRepositoryImpl(pool);
    MerchantQueryRepository merchantRepo = new MerchantQueryRepositoryImpl(merchantQueryClient);

    // 4. Initialize Caching
    RedisAPI redisAPI = RedisConfig.createClient(vertx);
    RedisService redisService = new RedisService(redisAPI, openTelemetry);

    // 5. Initialize Services
    MerchantDetailQueryService queryService = new MerchantDetailQueryServiceImpl(queryRepo, redisService, tracingMetrics);
    MerchantDetailCommandService cmdService = new MerchantDetailCommandServiceImpl(cmdRepo, merchantRepo, redisService, tracingMetrics);
    MerchantSocialLinkCommandService socialService = new MerchantSocialLinkCommandServiceImpl(socialRepo, redisService, tracingMetrics);

    // 6. Initialize Handlers
    var queryHandler = new MerchantDetailQueryHandler(queryService);
    var cmdHandler = new MerchantDetailCommandHandler(cmdService);
    var socialHandler = new MerchantSocialCommandHandler(socialService);

    int port = cfg.getGrpcPort() > 0 ? cfg.getGrpcPort() : 50058;

    startGrpcServer(queryHandler, cmdHandler, socialHandler, port)
        .onSuccess(v -> {
          log.info("MerchantDetailVerticle fully initialized with CQRS. Listening for gRPC on port {}", port);
          startPromise.complete();
        })
        .onFailure(err -> {
          log.error("Failed to bind gRPC server", err);
          startPromise.fail(err);
        });
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    if (telemetryConfig != null) {
      telemetryConfig.shutdown();
    }
    if (grpcClient != null) {
      grpcClient.close();
    }
    log.info("MerchantDetailVerticle successfully stopped!");
    stopPromise.complete();
  }

  private SocketAddress resolveGrpcAddress(String envPrefix, String defaultHost, int defaultPort) {
    String host = System.getenv().getOrDefault("GRPC_" + envPrefix + "_ADDR", defaultHost);
    int port = System.getenv("GRPC_" + envPrefix + "_PORT") != null
        ? Integer.parseInt(System.getenv("GRPC_" + envPrefix + "_PORT"))
        : defaultPort;

    if (host.contains(":")) {
      String[] parts = host.split(":");
      host = parts[0];
      port = Integer.parseInt(parts[1]);
    }

    log.info("📍 Service {} mapped to {}:{}", envPrefix, host, port);
    return SocketAddress.inetSocketAddress(port, host);
  }

  private Future<Void> startGrpcServer(
      MerchantDetailQueryHandler queryHandler,
      MerchantDetailCommandHandler cmdHandler,
      MerchantSocialCommandHandler socialHandler,
      int grpcPort) {

    GrpcServer grpcServer = GrpcServer.server(vertx);

    queryHandler.bindAll(grpcServer);
    cmdHandler.bindAll(grpcServer);
    socialHandler.bindAll(grpcServer);

    return vertx.createHttpServer()
        .requestHandler(grpcServer)
        .listen(grpcPort)
        .mapEmpty();
  }
}
