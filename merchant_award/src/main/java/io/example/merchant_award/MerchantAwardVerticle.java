package io.example.merchant_award;

import io.example.common.config.AppConfig;
import io.example.common.config.RedisConfig;
import io.example.common.config.TelemetryConfig;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_award.handler.MerchantAwardCommandHandler;
import io.example.merchant_award.handler.MerchantAwardQueryHandler;
import io.example.merchant_award.repository.MerchantAwardCommandRepository;
import io.example.merchant_award.repository.MerchantAwardQueryRepository;
import io.example.merchant_award.repository.MerchantQueryRepository;
import io.example.merchant_award.repository.impl.MerchantAwardCommandRepositoryImpl;
import io.example.merchant_award.repository.impl.MerchantAwardQueryRepositoryImpl;
import io.example.merchant_award.repository.impl.MerchantQueryRepositoryImpl;
import io.example.merchant_award.service.MerchantAwardCommandService;
import io.example.merchant_award.service.MerchantAwardQueryService;
import io.example.merchant_award.service.impl.MerchantAwardCommandServiceImpl;
import io.example.merchant_award.service.impl.MerchantAwardQueryServiceImpl;
import io.example.common.chaos.ChaosGrpcServerInterceptor;
import io.example.common.chaos.ChaosManager;
import io.example.common.chaos.ChaosSqlProxy;
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

public class MerchantAwardVerticle extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(MerchantAwardVerticle.class);

  private TelemetryConfig telemetryConfig;
  private GrpcClient grpcClient;
  private ChaosManager chaosManager;

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    JsonObject config = new JsonObject()
        .put("database", new JsonObject()
            .put("host", "pgbouncer")
            .put("port", 6432)
            .put("database", "ECOMMERCE")
            .put("user", "DRAGON")
            .put("password", "DRAGON")
            .put("pool_size", 5))
        .put("grpc_port", 50056)
        .put("service.name", "merchant-award-service");

    DeploymentOptions options = new DeploymentOptions().setConfig(config);

    vertx.deployVerticle(new MerchantAwardVerticle(), options)
        .onSuccess(id -> {
          log.info("✅ MerchantAward Service successfully deployed! ID: {}", id);
          log.info("🚀 gRPC Server running on port 50056");
        })
        .onFailure(err -> {
          log.error("❌ Failed to deploy MerchantAwardVerticle", err);
        });
  }

  @Override
  public void start(Promise<Void> startPromise) {
    JsonObject rawConfig = config();

    // 1. Initialize Telemetry
    JsonObject telConfig = rawConfig.copy();
    if (!telConfig.containsKey("service.name")) {
      telConfig.put("service.name", "merchant-award-service");
    }
    telemetryConfig = new TelemetryConfig(telConfig);
    OpenTelemetry openTelemetry = telemetryConfig.initialize();
    TracingMetrics tracingMetrics = new TracingMetrics(openTelemetry, "merchant-award-service");

    // 2. Initialize Repositories
    AppConfig cfg = AppConfig.from(rawConfig);
    var dbCfg = cfg.getDatabaseConfig();

    PgConnectOptions connectOptions = new PgConnectOptions()
        .setHost(dbCfg.getString("host", "localhost"))
        .setPort(dbCfg.getInteger("port", 5432))
        .setDatabase(dbCfg.getString("database", "vertxdb"))
        .setUser(dbCfg.getString("user", "vertx"))
        .setPassword(dbCfg.getString("password", "vertx"))
    // PgBouncer uses transaction pooling; do not reuse session-bound prepared statements.
    .setCachePreparedStatements(false);

    PoolOptions poolOptions = new PoolOptions()
        .setMaxSize(dbCfg.getInteger("pool_size", 5));

    Pool pool = Pool.pool(vertx, connectOptions, poolOptions);
    chaosManager = new ChaosManager();
    chaosManager.startWatcher(vertx);
    Pool chaosPool = ChaosSqlProxy.wrap(pool, chaosManager, vertx);

    // 3. Initialize unified gRPC Client pool & clients
    grpcClient = GrpcClient.client(vertx);
    SocketAddress addrMerchant = resolveGrpcAddress("MERCHANT", "merchant", 50055);
    var merchantQueryClient = new pb.merchant.VertxMerchantQueryServiceGrpcClient(grpcClient, addrMerchant);

    MerchantAwardQueryRepository queryRepo = new MerchantAwardQueryRepositoryImpl(chaosPool);
    MerchantAwardCommandRepository cmdRepo = new MerchantAwardCommandRepositoryImpl(chaosPool);
    MerchantQueryRepository merchantRepo = new MerchantQueryRepositoryImpl(merchantQueryClient);

    // 4. Initialize Caching
    RedisAPI redisAPI = RedisConfig.createClient(vertx);
    RedisService redisService = new RedisService(redisAPI, openTelemetry);

    // 5. Initialize Services
    MerchantAwardQueryService queryService = new MerchantAwardQueryServiceImpl(queryRepo, redisService, tracingMetrics);
    MerchantAwardCommandService cmdService = new MerchantAwardCommandServiceImpl(cmdRepo, queryRepo, merchantRepo,
        redisService, tracingMetrics);

    // 6. Initialize Handlers
    var queryHandler = new MerchantAwardQueryHandler(queryService);
    var cmdHandler = new MerchantAwardCommandHandler(cmdService);

    int port = cfg.getGrpcPort();

    startGrpcServer(queryHandler, cmdHandler, port)
        .onSuccess(v -> {
          log.info("MerchantAwardVerticle fully initialized with CQRS. Listening for gRPC on port {}", port);
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
    log.info("MerchantAwardVerticle successfully stopped!");
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
      MerchantAwardQueryHandler queryHandler,
      MerchantAwardCommandHandler cmdHandler,
      int grpcPort) {

    GrpcServer grpcServer = GrpcServer.server(vertx);

    queryHandler.bindAll(grpcServer);
    cmdHandler.bindAll(grpcServer);

    return vertx.createHttpServer()
        .requestHandler(new ChaosGrpcServerInterceptor(grpcServer, chaosManager, vertx))
        .listen(grpcPort)
        .mapEmpty();
  }
}
