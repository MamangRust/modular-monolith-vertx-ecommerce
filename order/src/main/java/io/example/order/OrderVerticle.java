package io.example.order;

import io.example.common.config.AppConfig;
import io.example.common.config.RedisConfig;
import io.example.common.config.TelemetryConfig;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.order.handler.OrderCommandHandler;
import io.example.order.handler.OrderQueryHandler;
import io.example.order.handler.OrderStatsHandler;
import io.example.order.repository.*;
import io.example.order.repository.impl.*;
import io.example.order.service.OrderCommandService;
import io.example.order.service.OrderQueryService;
import io.example.order.service.OrderStatsService;
import io.example.order.service.impl.OrderCommandServiceImpl;
import io.example.order.service.impl.OrderQueryServiceImpl;
import io.example.order.service.impl.OrderStatsServiceImpl;
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

public class OrderVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(OrderVerticle.class);

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
                .put("grpc_port", 50057)
                .put("service.name", "order-service");

        DeploymentOptions options = new DeploymentOptions().setConfig(config);

        vertx.deployVerticle(new OrderVerticle(), options)
                .onSuccess(id -> {
                    log.info("✅ Order Service successfully deployed! ID: {}", id);
                    log.info("🚀 gRPC Server running on port 50057");
                })
                .onFailure(err -> {
                    log.error("❌ Failed to deploy OrderVerticle", err);
                });
    }

    @Override
    public void start(Promise<Void> startPromise) {
        JsonObject rawConfig = config();

        // 1. Initialize Telemetry
        JsonObject telConfig = rawConfig.copy();
        if (!telConfig.containsKey("service.name")) {
            telConfig.put("service.name", "order-service");
        }
        telemetryConfig = new TelemetryConfig(telConfig);
        OpenTelemetry openTelemetry = telemetryConfig.initialize();
        TracingMetrics tracingMetrics = new TracingMetrics(openTelemetry, "order-service");

        // 2. Initialize Repositories
        AppConfig cfg = AppConfig.from(rawConfig);
        var dbCfg = cfg.getDatabaseConfig();

        PgConnectOptions connectOptions = new PgConnectOptions()
                .setHost(dbCfg.getString("host", "localhost"))
                .setPort(dbCfg.getInteger("port", 5432))
                .setDatabase(dbCfg.getString("database", "ecommerce_order"))
                .setUser(dbCfg.getString("user", "DRAGON"))
                .setPassword(dbCfg.getString("password", "DRAGON"));

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(dbCfg.getInteger("pool_size", 5));

        Pool pool = Pool.pool(vertx, connectOptions, poolOptions);
        chaosManager = new ChaosManager();
        chaosManager.startWatcher(vertx);
        Pool chaosPool = ChaosSqlProxy.wrap(pool, chaosManager, vertx);

        OrderQueryRepository orderQueryRepo = new OrderQueryRepositoryImpl(chaosPool);
        OrderCommandRepository orderCommandRepo = new OrderCommandRepositoryImpl(chaosPool);
        OrderStatsRepository orderStatsRepo = new OrderStatsRepositoryImpl(chaosPool);

        // 3. Initialize unified gRPC Client pool & microservice client adapters
        grpcClient = GrpcClient.client(vertx);
        SocketAddress addrUser = resolveGrpcAddress("USER", "user", 50053);
        SocketAddress addrProduct = resolveGrpcAddress("PRODUCT", "product", 50058);
        SocketAddress addrMerchant = resolveGrpcAddress("MERCHANT", "merchant", 50055);
        SocketAddress addrOrderItem = resolveGrpcAddress("ORDER_ITEM", "order-item", 50056);
        SocketAddress addrShipping = resolveGrpcAddress("SHIPPING", "shipping_address", 50063);
        SocketAddress addrTransaction = resolveGrpcAddress("TRANSACTION", "transaction", 50059);

        var userGrpcClient = new pb.user.VertxUserQueryServiceGrpcClient(grpcClient, addrUser);
        var productQueryGrpcClient = new pb.product.VertxProductQueryServiceGrpcClient(grpcClient, addrProduct);
        var productCommandGrpcClient = new pb.product.VertxProductCommandServiceGrpcClient(grpcClient, addrProduct);
        var merchantGrpcClient = new pb.merchant.VertxMerchantQueryServiceGrpcClient(grpcClient, addrMerchant);

        var orderItemQueryGrpcClient = new pb.order_item.VertxOrderItemQueryServiceGrpcClient(grpcClient, addrOrderItem);
        var orderItemCommandGrpcClient = new pb.order_item.VertxOrderItemCommandServiceGrpcClient(grpcClient, addrOrderItem);
        var shippingQueryGrpcClient = new pb.shipping_address.VertxShippingQueryServiceGrpcClient(grpcClient, addrShipping);
        var shippingCommandGrpcClient = new pb.shipping_address.VertxShippingCommandServiceGrpcClient(grpcClient, addrShipping);
        var transactionCommandGrpcClient = new pb.transaction.VertxTransactionCommandServiceGrpcClient(grpcClient, addrTransaction);

        UserQueryRepository userQueryRepo = new UserQueryRepositoryImpl(userGrpcClient);
        ProductQueryRepository productQueryRepo = new ProductQueryRepositoryImpl(productQueryGrpcClient);
        ProductCommandRepository productCommandRepo = new ProductCommandRepositoryImpl(productCommandGrpcClient);
        MerchantQueryRepository merchantQueryRepo = new MerchantQueryRepositoryImpl(merchantGrpcClient);

        OrderItemQueryRepository orderItemQueryRepo = new OrderItemQueryRepositoryImpl(orderItemQueryGrpcClient, orderItemCommandGrpcClient);
        OrderItemCommandRepository orderItemCommandRepo = new OrderItemCommandRepositoryImpl(orderItemCommandGrpcClient);
        ShippingAddressCommandRepository shippingAddressCommandRepo = new ShippingAddressCommandRepositoryImpl(shippingCommandGrpcClient, shippingQueryGrpcClient);
        TransactionCommandRepository transactionCommandRepo = new TransactionCommandRepositoryImpl(transactionCommandGrpcClient);

        // 4. Initialize Caching
        RedisAPI redisAPI = RedisConfig.createClient(vertx);
        RedisService redisService = new RedisService(redisAPI, openTelemetry);

        // 5. Initialize CQRS Services
        OrderQueryService queryService = new OrderQueryServiceImpl(orderQueryRepo, redisService, tracingMetrics);
        OrderCommandService cmdService = new OrderCommandServiceImpl(
                orderCommandRepo, orderQueryRepo,
                orderItemCommandRepo, orderItemQueryRepo,
                shippingAddressCommandRepo, transactionCommandRepo,
                userQueryRepo, productQueryRepo, merchantQueryRepo, productCommandRepo,
                redisService, tracingMetrics
        );
        OrderStatsService statsService = new OrderStatsServiceImpl(orderStatsRepo, redisService, tracingMetrics);

        // 6. Initialize Handlers
        var queryHandler = new OrderQueryHandler(queryService);
        var cmdHandler = new OrderCommandHandler(cmdService);
        var statsHandler = new OrderStatsHandler(statsService);

        int port = cfg.getGrpcPort() > 0 ? cfg.getGrpcPort() : 50057;

        startGrpcServer(queryHandler, cmdHandler, statsHandler, port)
                .onSuccess(v -> {
                    log.info("OrderVerticle fully initialized with CQRS. Listening for gRPC on port {}", port);
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

        if (host.contains(":")) {
            String[] parts = host.split(":");
            host = parts[0];
            port = Integer.parseInt(parts[1]);
        }

        log.info("📍 Service {} mapped to {}:{}", envPrefix, host, port);
        return SocketAddress.inetSocketAddress(port, host);
    }

    private Future<Void> startGrpcServer(
            OrderQueryHandler queryHandler,
            OrderCommandHandler cmdHandler,
            OrderStatsHandler statsHandler,
            int grpcPort) {

        GrpcServer grpcServer = GrpcServer.server(vertx);

        queryHandler.bindAll(grpcServer);
        cmdHandler.bindAll(grpcServer);
        statsHandler.bindAll(grpcServer);

        return vertx.createHttpServer()
                .requestHandler(new ChaosGrpcServerInterceptor(grpcServer, chaosManager, vertx))
                .listen(grpcPort)
                .mapEmpty();
    }
}
