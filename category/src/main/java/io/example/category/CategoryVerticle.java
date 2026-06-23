package io.example.category;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.category.handler.CategoryCommandHandler;
import io.example.category.handler.CategoryQueryHandler;
import io.example.category.handler.CategoryStatsByIdHandler;
import io.example.category.handler.CategoryStatsByMerchantHandler;
import io.example.category.handler.CategoryStatsHandler;
import io.example.category.repository.impl.CategoryCommandRepositoryImpl;
import io.example.category.repository.impl.CategoryQueryRepositoryImpl;
import io.example.category.repository.impl.CategoryStatsByIdRepositoryImpl;
import io.example.category.repository.impl.CategoryStatsByMerchantRepositoryImpl;
import io.example.category.repository.impl.CategoryStatsRepositoryImpl;
import io.example.category.service.impl.CategoryCommandServiceImpl;
import io.example.category.service.impl.CategoryQueryServiceImpl;
import io.example.category.service.impl.CategoryStatsByIdServiceImpl;
import io.example.category.service.impl.CategoryStatsByMerchantServiceImpl;
import io.example.category.service.impl.CategoryStatsServiceImpl;
import io.example.common.config.AppConfig;
import io.example.common.config.RedisConfig;
import io.example.common.config.TelemetryConfig;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
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
import io.vertx.grpc.server.GrpcServer;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;

public class CategoryVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(CategoryVerticle.class);

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
                .put("grpc_port", 8082)
                .put("service.name", "category-service");

        DeploymentOptions options = new DeploymentOptions().setConfig(config);

        vertx.deployVerticle(new CategoryVerticle(), options)
                .onSuccess(id -> {
                    log.info("✅ Category Service successfully deployed! ID: {}", id);
                    log.info("🚀 gRPC Server running on port 8082");
                })
                .onFailure(err -> {
                    log.error("❌ Failed to deploy CategoryVerticle", err);
                });
    }

    @Override
    public void start(Promise<Void> startPromise) {
        JsonObject rawConfig = config();

        // 1. Initialize Telemetry
        JsonObject telConfig = rawConfig.copy();
        if (!telConfig.containsKey("service.name")) {
            telConfig.put("service.name", "category-service");
        }
        telemetryConfig = new TelemetryConfig(telConfig);
        OpenTelemetry openTelemetry = telemetryConfig.initialize();
        TracingMetrics tracingMetrics = new TracingMetrics(openTelemetry, "category-service");

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
        chaosManager = new ChaosManager();
        chaosManager.startWatcher(vertx);
        Pool chaosPool = ChaosSqlProxy.wrap(pool, chaosManager, vertx);

        var queryRepo = new CategoryQueryRepositoryImpl(chaosPool);
        var cmdRepo = new CategoryCommandRepositoryImpl(chaosPool);
        var statsRepo = new CategoryStatsRepositoryImpl(chaosPool);
        var statsByIdRepo = new CategoryStatsByIdRepositoryImpl(chaosPool);
        var statsByMerchantRepo = new CategoryStatsByMerchantRepositoryImpl(chaosPool);

        // 3. Initialize Caching
        RedisAPI redisAPI = RedisConfig.createClient(vertx);
        RedisService redisService = new RedisService(redisAPI, openTelemetry);

        // 4. Initialize Services
        var queryService = new CategoryQueryServiceImpl(queryRepo, redisService, tracingMetrics);
        var cmdService = new CategoryCommandServiceImpl(cmdRepo, queryRepo, redisService, tracingMetrics);
        var statsService = new CategoryStatsServiceImpl(statsRepo, redisService, tracingMetrics);
        var statsByIdService = new CategoryStatsByIdServiceImpl(statsByIdRepo, redisService, tracingMetrics);
        var statsByMerchantService = new CategoryStatsByMerchantServiceImpl(statsByMerchantRepo, redisService,
                tracingMetrics);

        // 5. Initialize Handlers
        var queryHandler = new CategoryQueryHandler(queryService);
        var cmdHandler = new CategoryCommandHandler(cmdService);
        var statsHandler = new CategoryStatsHandler(statsService);
        var statsByIdHandler = new CategoryStatsByIdHandler(statsByIdService);
        var statsByMerchantHandler = new CategoryStatsByMerchantHandler(statsByMerchantService);

        int port = cfg.getGrpcPort() > 0 ? cfg.getGrpcPort() : 8082;

        startGrpcServer(queryHandler, cmdHandler, statsHandler, statsByIdHandler, statsByMerchantHandler, port)
                .onSuccess(v -> {
                    log.info("CategoryVerticle fully initialized with CQRS. Listening for gRPC on port {}", port);
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

    private Future<Void> startGrpcServer(
            CategoryQueryHandler queryHandler,
            CategoryCommandHandler cmdHandler,
            CategoryStatsHandler statsHandler,
            CategoryStatsByIdHandler statsByIdHandler,
            CategoryStatsByMerchantHandler statsByMerchantHandler,
            int grpcPort) {
        GrpcServer grpcServer = GrpcServer.server(vertx);

        queryHandler.bindAll(grpcServer);
        cmdHandler.bindAll(grpcServer);
        statsHandler.bindAll(grpcServer);
        statsByIdHandler.bindAll(grpcServer);
        statsByMerchantHandler.bindAll(grpcServer);

        return vertx.createHttpServer()
                .requestHandler(new ChaosGrpcServerInterceptor(grpcServer, chaosManager, vertx))
                .listen(grpcPort)
                .mapEmpty();
    }
}
