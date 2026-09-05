package io.example.shipping_address;

import io.example.common.config.AppConfig;
import io.example.common.config.RedisConfig;
import io.example.common.config.TelemetryConfig;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.shipping_address.handler.ShippingAddressCommandHandler;
import io.example.shipping_address.handler.ShippingAddressQueryHandler;
import io.example.shipping_address.repository.ShippingAddressCommandRepository;
import io.example.shipping_address.repository.ShippingAddressQueryRepository;
import io.example.shipping_address.repository.impl.ShippingAddressCommandRepositoryImpl;
import io.example.shipping_address.repository.impl.ShippingAddressQueryRepositoryImpl;
import io.example.shipping_address.service.ShippingAddressCommandService;
import io.example.shipping_address.service.ShippingAddressQueryService;
import io.example.shipping_address.service.impl.ShippingAddressCommandServiceImpl;
import io.example.shipping_address.service.impl.ShippingAddressQueryServiceImpl;
import io.opentelemetry.api.OpenTelemetry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.redis.client.RedisAPI;
import io.example.common.chaos.ChaosGrpcServerInterceptor;
import io.example.common.chaos.ChaosManager;
import io.example.common.chaos.ChaosSqlProxy;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShippingAddressVerticle extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(ShippingAddressVerticle.class);

  private TelemetryConfig telemetryConfig;
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
        .put("grpc_port", 50063)
        .put("service.name", "shipping-address-service");

    DeploymentOptions options = new DeploymentOptions().setConfig(config);

    vertx.deployVerticle(new ShippingAddressVerticle(), options)
        .onSuccess(id -> {
          log.info("✅ ShippingAddress Service successfully deployed! ID: {}", id);
          log.info("🚀 gRPC Server running on port 50063");
        })
        .onFailure(err -> {
          log.error("❌ Failed to deploy ShippingAddressVerticle", err);
        });
  }

  @Override
  public void start(Promise<Void> startPromise) {
    JsonObject rawConfig = config();
    
    // 1. Initialize Telemetry
    JsonObject telConfig = rawConfig.copy();
    if (!telConfig.containsKey("service.name")) {
        telConfig.put("service.name", "shipping-address-service");
    }
    telemetryConfig = new TelemetryConfig(telConfig);
    OpenTelemetry openTelemetry = telemetryConfig.initialize();
    TracingMetrics tracingMetrics = new TracingMetrics(openTelemetry, "shipping-address-service");

    // 2. Initialize Repositories
    AppConfig cfg = AppConfig.from(rawConfig);
    var dbCfg = cfg.getDatabaseConfig();

    PgConnectOptions connectOptions = new PgConnectOptions()
        .setHost(dbCfg.getString("host", "localhost"))
        .setPort(dbCfg.getInteger("port", 5449))
        .setDatabase(dbCfg.getString("database", "ecommerce_shipping_address"))
        .setUser(dbCfg.getString("user", "DRAGON"))
        .setPassword(dbCfg.getString("password", "DRAGON"))
        // PgBouncer uses transaction pooling; do not reuse session-bound prepared statements.
        .setCachePreparedStatements(false);

    PoolOptions poolOptions = new PoolOptions()
        .setMaxSize(dbCfg.getInteger("pool_size", 5));

    Pool pool = Pool.pool(vertx, connectOptions, poolOptions);
    chaosManager = new ChaosManager();
    chaosManager.startWatcher(vertx);
    Pool chaosPool = ChaosSqlProxy.wrap(pool, chaosManager, vertx);

    ShippingAddressQueryRepository queryRepo = new ShippingAddressQueryRepositoryImpl(chaosPool);
    ShippingAddressCommandRepository cmdRepo = new ShippingAddressCommandRepositoryImpl(chaosPool);

    // 3. Initialize Caching
    RedisAPI redisAPI = RedisConfig.createClient(vertx);
    RedisService redisService = new RedisService(redisAPI, openTelemetry);

    // 4. Initialize Services
    ShippingAddressQueryService queryService = new ShippingAddressQueryServiceImpl(queryRepo, redisService, tracingMetrics);
    ShippingAddressCommandService cmdService = new ShippingAddressCommandServiceImpl(cmdRepo, queryRepo, redisService, tracingMetrics);

    // 5. Initialize Handlers
    var queryHandler = new ShippingAddressQueryHandler(queryService);
    var cmdHandler = new ShippingAddressCommandHandler(cmdService);
    
    int port = cfg.getGrpcPort();

    startGrpcServer(queryHandler, cmdHandler, port)
        .onSuccess(v -> {
          log.info("ShippingAddressVerticle fully initialized with CQRS. Listening for gRPC on port {}", port);
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
    stopPromise.complete();
  }

  private Future<Void> startGrpcServer(ShippingAddressQueryHandler queryHandler, ShippingAddressCommandHandler cmdHandler, int grpcPort) {
    GrpcServer grpcServer = GrpcServer.server(vertx);

    queryHandler.bindAll(grpcServer);
    cmdHandler.bindAll(grpcServer);

    return vertx.createHttpServer()
        .requestHandler(new ChaosGrpcServerInterceptor(grpcServer, chaosManager, vertx))
        .listen(grpcPort)
        .mapEmpty();
  }
}
