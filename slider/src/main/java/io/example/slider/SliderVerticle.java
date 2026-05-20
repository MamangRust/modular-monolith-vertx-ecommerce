package io.example.slider;

import io.example.common.config.AppConfig;
import io.example.common.config.RedisConfig;
import io.example.common.config.TelemetryConfig;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.slider.handler.SliderCommandHandler;
import io.example.slider.handler.SliderQueryHandler;
import io.example.slider.repository.SliderCommandRepository;
import io.example.slider.repository.SliderQueryRepository;
import io.example.slider.repository.impl.SliderCommandRepositoryImpl;
import io.example.slider.repository.impl.SliderQueryRepositoryImpl;
import io.example.slider.service.SliderCommandService;
import io.example.slider.service.SliderQueryService;
import io.example.slider.service.impl.SliderCommandServiceImpl;
import io.example.slider.service.impl.SliderQueryServiceImpl;
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
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SliderVerticle extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(SliderVerticle.class);

  private TelemetryConfig telemetryConfig;

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    JsonObject config = new JsonObject()
        .put("database", new JsonObject()
            .put("host", "localhost")
            .put("port", 5448)
            .put("database", "ecommerce_slider")
            .put("user", "DRAGON")
            .put("password", "DRAGON")
            .put("pool_size", 5))
        .put("grpc_port", 50062)
        .put("service.name", "slider-service");

    DeploymentOptions options = new DeploymentOptions().setConfig(config);

    vertx.deployVerticle(new SliderVerticle(), options)
        .onSuccess(id -> {
          log.info("✅ Slider Service successfully deployed! ID: {}", id);
          log.info("🚀 gRPC Server running on port 50062");
        })
        .onFailure(err -> {
          log.error("❌ Failed to deploy SliderVerticle", err);
        });
  }

  @Override
  public void start(Promise<Void> startPromise) {
    JsonObject rawConfig = config();
    
    // 1. Initialize Telemetry
    JsonObject telConfig = rawConfig.copy();
    if (!telConfig.containsKey("service.name")) {
        telConfig.put("service.name", "slider-service");
    }
    telemetryConfig = new TelemetryConfig(telConfig);
    OpenTelemetry openTelemetry = telemetryConfig.initialize();
    TracingMetrics tracingMetrics = new TracingMetrics(openTelemetry, "slider-service");

    // 2. Initialize Repositories
    AppConfig cfg = AppConfig.from(rawConfig);
    var dbCfg = cfg.getDatabaseConfig();

    PgConnectOptions connectOptions = new PgConnectOptions()
        .setHost(dbCfg.getString("host", "localhost"))
        .setPort(dbCfg.getInteger("port", 5448))
        .setDatabase(dbCfg.getString("database", "ecommerce_slider"))
        .setUser(dbCfg.getString("user", "DRAGON"))
        .setPassword(dbCfg.getString("password", "DRAGON"));

    PoolOptions poolOptions = new PoolOptions()
        .setMaxSize(dbCfg.getInteger("pool_size", 5));

    Pool pool = Pool.pool(vertx, connectOptions, poolOptions);

    SliderQueryRepository queryRepo = new SliderQueryRepositoryImpl(pool);
    SliderCommandRepository cmdRepo = new SliderCommandRepositoryImpl(pool);

    // 3. Initialize Caching
    RedisAPI redisAPI = RedisConfig.createClient(vertx);
    RedisService redisService = new RedisService(redisAPI, openTelemetry);

    // 4. Initialize Services
    SliderQueryService queryService = new SliderQueryServiceImpl(queryRepo, redisService, tracingMetrics);
    SliderCommandService cmdService = new SliderCommandServiceImpl(cmdRepo, redisService, tracingMetrics);

    // 5. Initialize Handlers
    var queryHandler = new SliderQueryHandler(queryService);
    var cmdHandler = new SliderCommandHandler(cmdService);
    
    int port = cfg.getGrpcPort();

    startGrpcServer(queryHandler, cmdHandler, port)
        .onSuccess(v -> {
          log.info("SliderVerticle fully initialized with CQRS. Listening for gRPC on port {}", port);
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

  private Future<Void> startGrpcServer(SliderQueryHandler queryHandler, SliderCommandHandler cmdHandler, int grpcPort) {
    GrpcServer grpcServer = GrpcServer.server(vertx);

    queryHandler.bindAll(grpcServer);
    cmdHandler.bindAll(grpcServer);

    return vertx.createHttpServer()
        .requestHandler(grpcServer)
        .listen(grpcPort)
        .mapEmpty();
  }
}
