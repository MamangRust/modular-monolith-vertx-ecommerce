package io.example.cart;

import io.example.common.config.AppConfig;
import io.example.common.config.RedisConfig;
import io.example.common.config.TelemetryConfig;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.cart.handler.CartCommandHandler;
import io.example.cart.handler.CartQueryHandler;
import io.example.cart.repository.CartCommandRepository;
import io.example.cart.repository.CartQueryRepository;
import io.example.cart.repository.ProductQueryRepository;
import io.example.cart.repository.UserQueryRepository;
import io.example.cart.repository.impl.CartCommandRepositoryImpl;
import io.example.cart.repository.impl.CartQueryRepositoryImpl;
import io.example.cart.repository.impl.ProductQueryRepositoryImpl;
import io.example.cart.repository.impl.UserQueryRepositoryImpl;
import io.example.cart.service.CartCommandService;
import io.example.cart.service.CartQueryService;
import io.example.cart.service.impl.CartCommandServiceImpl;
import io.example.cart.service.impl.CartQueryServiceImpl;
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

public class CartVerticle extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(CartVerticle.class);

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
        .put("grpc_port", 50060)
        .put("service.name", "cart-service");

    DeploymentOptions options = new DeploymentOptions().setConfig(config);

    vertx.deployVerticle(new CartVerticle(), options)
        .onSuccess(id -> {
          log.info("✅ Cart Service successfully deployed! ID: {}", id);
          log.info("🚀 gRPC Server running on port 50060");
        })
        .onFailure(err -> {
          log.error("❌ Failed to deploy CartVerticle", err);
        });
  }

  @Override
  public void start(Promise<Void> startPromise) {
    JsonObject rawConfig = config();
    
    // 1. Initialize Telemetry
    JsonObject telConfig = rawConfig.copy();
    if (!telConfig.containsKey("service.name")) {
        telConfig.put("service.name", "cart-service");
    }
    telemetryConfig = new TelemetryConfig(telConfig);
    OpenTelemetry openTelemetry = telemetryConfig.initialize();
    TracingMetrics tracingMetrics = new TracingMetrics(openTelemetry, "cart-service");

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
    SocketAddress addrUser = resolveGrpcAddress("USER", "user", 50053);
    SocketAddress addrProduct = resolveGrpcAddress("PRODUCT", "product", 50058);

    var userQueryClient = new pb.user.VertxUserQueryServiceGrpcClient(grpcClient, addrUser);
    var productQueryClient = new pb.product.VertxProductQueryServiceGrpcClient(grpcClient, addrProduct);

    CartQueryRepository queryRepo = new CartQueryRepositoryImpl(chaosPool);
    CartCommandRepository cmdRepo = new CartCommandRepositoryImpl(chaosPool);
    ProductQueryRepository productRepo = new ProductQueryRepositoryImpl(productQueryClient);
    UserQueryRepository userRepo = new UserQueryRepositoryImpl(userQueryClient);

    // 4. Initialize Caching
    RedisAPI redisAPI = RedisConfig.createClient(vertx);
    RedisService redisService = new RedisService(redisAPI, openTelemetry);

    // 5. Initialize Services
    CartQueryService queryService = new CartQueryServiceImpl(queryRepo, redisService, tracingMetrics);
    CartCommandService cmdService = new CartCommandServiceImpl(cmdRepo, productRepo, userRepo, redisService, tracingMetrics);

    // 6. Initialize Handlers
    var queryHandler = new CartQueryHandler(queryService);
    var cmdHandler = new CartCommandHandler(cmdService);
    
    int port = cfg.getGrpcPort();

    startGrpcServer(queryHandler, cmdHandler, port)
        .onSuccess(v -> {
          log.info("CartVerticle fully initialized with CQRS. Listening for gRPC on port {}", port);
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

  private Future<Void> startGrpcServer(CartQueryHandler queryHandler, CartCommandHandler cmdHandler, int grpcPort) {
    GrpcServer grpcServer = GrpcServer.server(vertx);

    queryHandler.bindAll(grpcServer);
    cmdHandler.bindAll(grpcServer);

    return vertx.createHttpServer()
        .requestHandler(new ChaosGrpcServerInterceptor(grpcServer, chaosManager, vertx))
        .listen(grpcPort)
        .mapEmpty();
  }
}
