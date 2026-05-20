package io.example.review;

import io.example.common.config.AppConfig;
import io.example.common.config.RedisConfig;
import io.example.common.config.TelemetryConfig;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.review.handler.ReviewCommandHandler;
import io.example.review.handler.ReviewQueryHandler;
import io.example.review.repository.ReviewCommandRepository;
import io.example.review.repository.ReviewQueryRepository;
import io.example.review.repository.UserQueryRepository;
import io.example.review.repository.ProductQueryRepository;
import io.example.review.repository.impl.ReviewCommandRepositoryImpl;
import io.example.review.repository.impl.ReviewQueryRepositoryImpl;
import io.example.review.repository.impl.UserQueryRepositoryImpl;
import io.example.review.repository.impl.ProductQueryRepositoryImpl;
import io.example.review.service.ReviewCommandService;
import io.example.review.service.ReviewQueryService;
import io.example.review.service.impl.ReviewCommandServiceImpl;
import io.example.review.service.impl.ReviewQueryServiceImpl;
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

public class ReviewVerticle extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(ReviewVerticle.class);

  private TelemetryConfig telemetryConfig;
  private GrpcClient grpcClient;

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    JsonObject config = new JsonObject()
        .put("database", new JsonObject()
            .put("host", "localhost")
            .put("port", 5446)
            .put("database", "ecommerce_review")
            .put("user", "DRAGON")
            .put("password", "DRAGON")
            .put("pool_size", 5))
        .put("grpc_port", 50061)
        .put("service.name", "review-service");

    DeploymentOptions options = new DeploymentOptions().setConfig(config);

    vertx.deployVerticle(new ReviewVerticle(), options)
        .onSuccess(id -> {
          log.info("✅ Review Service successfully deployed! ID: {}", id);
          log.info("🚀 gRPC Server running on port 50061");
        })
        .onFailure(err -> {
          log.error("❌ Failed to deploy ReviewVerticle", err);
        });
  }

  @Override
  public void start(Promise<Void> startPromise) {
    JsonObject rawConfig = config();
    
    // 1. Initialize Telemetry
    JsonObject telConfig = rawConfig.copy();
    if (!telConfig.containsKey("service.name")) {
        telConfig.put("service.name", "review-service");
    }
    telemetryConfig = new TelemetryConfig(telConfig);
    OpenTelemetry openTelemetry = telemetryConfig.initialize();
    TracingMetrics tracingMetrics = new TracingMetrics(openTelemetry, "review-service");

    // 2. Initialize Repositories
    AppConfig cfg = AppConfig.from(rawConfig);
    var dbCfg = cfg.getDatabaseConfig();

    PgConnectOptions connectOptions = new PgConnectOptions()
        .setHost(dbCfg.getString("host", "localhost"))
        .setPort(dbCfg.getInteger("port", 5446))
        .setDatabase(dbCfg.getString("database", "ecommerce_review"))
        .setUser(dbCfg.getString("user", "DRAGON"))
        .setPassword(dbCfg.getString("password", "DRAGON"));

    PoolOptions poolOptions = new PoolOptions()
        .setMaxSize(dbCfg.getInteger("pool_size", 5));

    Pool pool = Pool.pool(vertx, connectOptions, poolOptions);
    
    // 3. Initialize unified gRPC Client pool & downstream clients
    grpcClient = GrpcClient.client(vertx);
    SocketAddress addrUser = resolveGrpcAddress("USER", "user", 50053);
    SocketAddress addrProduct = resolveGrpcAddress("PRODUCT", "product", 50058);

    var userQueryClient = new pb.user.VertxUserQueryServiceGrpcClient(grpcClient, addrUser);
    var productQueryClient = new pb.product.VertxProductQueryServiceGrpcClient(grpcClient, addrProduct);

    ReviewQueryRepository queryRepo = new ReviewQueryRepositoryImpl(pool);
    ReviewCommandRepository cmdRepo = new ReviewCommandRepositoryImpl(pool);
    UserQueryRepository userRepo = new UserQueryRepositoryImpl(userQueryClient);
    ProductQueryRepository productRepo = new ProductQueryRepositoryImpl(productQueryClient);

    // 4. Initialize Caching
    RedisAPI redisAPI = RedisConfig.createClient(vertx);
    RedisService redisService = new RedisService(redisAPI, openTelemetry);

    // 5. Initialize Services
    ReviewQueryService queryService = new ReviewQueryServiceImpl(queryRepo, redisService, tracingMetrics);
    ReviewCommandService cmdService = new ReviewCommandServiceImpl(cmdRepo, userRepo, productRepo, redisService, tracingMetrics);

    // 6. Initialize Handlers
    var queryHandler = new ReviewQueryHandler(queryService);
    var cmdHandler = new ReviewCommandHandler(cmdService);
    
    int port = cfg.getGrpcPort();

    startGrpcServer(queryHandler, cmdHandler, port)
        .onSuccess(v -> {
          log.info("ReviewVerticle fully initialized with CQRS. Listening for gRPC on port {}", port);
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
    stopPromise.complete();
  }

  private SocketAddress resolveGrpcAddress(String envPrefix, String defaultHost, int defaultPort) {
    String host = System.getenv().getOrDefault("GRPC_" + envPrefix + "_ADDR", defaultHost);
    int port = System.getenv("GRPC_" + envPrefix + "_PORT") != null
        ? Integer.parseInt(System.getenv("GRPC_" + envPrefix + "_PORT"))
        : defaultPort;

    // If ADDR contains a colon, split it
    if (host.contains(":")) {
      String[] parts = host.split(":");
      host = parts[0];
      port = Integer.parseInt(parts[1]);
    }

    log.info("📍 Service {} mapped to {}:{}", envPrefix, host, port);
    return SocketAddress.inetSocketAddress(port, host);
  }

  private Future<Void> startGrpcServer(ReviewQueryHandler queryHandler, ReviewCommandHandler cmdHandler, int grpcPort) {
    GrpcServer grpcServer = GrpcServer.server(vertx);

    queryHandler.bindAll(grpcServer);
    cmdHandler.bindAll(grpcServer);

    return vertx.createHttpServer()
        .requestHandler(grpcServer)
        .listen(grpcPort)
        .mapEmpty();
  }
}
