package io.example.transaction;

import java.util.HashMap;
import java.util.Map;

import io.example.common.config.AppConfig;
import io.example.common.config.RedisConfig;
import io.example.common.config.TelemetryConfig;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.common.service.KafkaService;
import io.example.transaction.handler.*;
import io.example.transaction.repository.*;
import io.example.transaction.repository.impl.*;
import io.example.transaction.service.*;
import io.example.transaction.service.impl.*;
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
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(TransactionVerticle.class);

    private TelemetryConfig telemetryConfig;
    private GrpcClient grpcClient;
    private KafkaService kafkaService;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        JsonObject config = new JsonObject()
                .put("database", new JsonObject()
                        .put("host", "localhost")
                        .put("port", 5444)
                        .put("database", "ecommerce_transaction")
                        .put("user", "DRAGON")
                        .put("password", "DRAGON")
                        .put("pool_size", 5))
                .put("grpc_port", 50059)
                .put("service.name", "transaction-service");

        DeploymentOptions options = new DeploymentOptions().setConfig(config);

        vertx.deployVerticle(new TransactionVerticle(), options)
                .onSuccess(id -> {
                    log.info("✅ Transaction Service successfully deployed! ID: {}", id);
                    log.info("🚀 gRPC Server running on port 50059");
                })
                .onFailure(err -> {
                    log.error("❌ Failed to deploy TransactionVerticle", err);
                });
    }

    @Override
    public void start(Promise<Void> startPromise) {
        JsonObject rawConfig = config();

        // 1. Initialize Telemetry
        JsonObject telConfig = rawConfig.copy();
        if (!telConfig.containsKey("service.name")) {
            telConfig.put("service.name", "transaction-service");
        }
        telemetryConfig = new TelemetryConfig(telConfig);
        OpenTelemetry openTelemetry = telemetryConfig.initialize();
        TracingMetrics tracingMetrics = new TracingMetrics(openTelemetry, "transaction-service");

        // 2. Initialize Repositories
        AppConfig cfg = AppConfig.from(rawConfig);
        var dbCfg = cfg.getDatabaseConfig();

        PgConnectOptions connectOptions = new PgConnectOptions()
                .setHost(dbCfg.getString("host", "localhost"))
                .setPort(dbCfg.getInteger("port", 5444))
                .setDatabase(dbCfg.getString("database", "ecommerce_transaction"))
                .setUser(dbCfg.getString("user", "DRAGON"))
                .setPassword(dbCfg.getString("password", "DRAGON"));

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(dbCfg.getInteger("pool_size", 5));

        Pool pool = Pool.pool(vertx, connectOptions, poolOptions);

        TransactionQueryRepository queryRepo = new TransactionQueryRepositoryImpl(pool);
        TransactionCommandRepository cmdRepo = new TransactionCommandRepositoryImpl(pool);
        TransactionStatsRepository statsRepo = new TransactionStatsRepositoryImpl(pool);
        TransactionStatsByMerchantRepository merchantStatsRepo = new TransactionStatsByMerchantRepositoryImpl(pool);

        // 3. Initialize unified gRPC Client pool & microservice client adapters
        grpcClient = GrpcClient.client(vertx);
        SocketAddress addrUser = resolveGrpcAddress("USER", "user", 50053);
        SocketAddress addrMerchant = resolveGrpcAddress("MERCHANT", "merchant", 50055);
        SocketAddress addrOrder = resolveGrpcAddress("ORDER", "order", 50057);
        SocketAddress addrOrderItem = resolveGrpcAddress("ORDER_ITEM", "order_item", 50056);
        SocketAddress addrShipping = resolveGrpcAddress("SHIPPING", "shipping_address", 50063);

        var userGrpcClient = new pb.user.VertxUserQueryServiceGrpcClient(grpcClient, addrUser);
        var merchantGrpcClient = new pb.merchant.VertxMerchantQueryServiceGrpcClient(grpcClient, addrMerchant);
        var orderGrpcClient = new pb.order.VertxOrderQueryServiceGrpcClient(grpcClient, addrOrder);
        var orderItemGrpcClient = new pb.order_item.VertxOrderItemQueryServiceGrpcClient(grpcClient, addrOrderItem);
        var shippingGrpcClient = new pb.shipping_address.VertxShippingQueryServiceGrpcClient(grpcClient, addrShipping);

        UserQueryRepository userQueryRepo = new UserQueryRepositoryImpl(userGrpcClient);
        MerchantQueryRepository merchantQueryRepo = new MerchantQueryRepositoryImpl(merchantGrpcClient);
        OrderQueryRepository orderQueryRepo = new OrderQueryRepositoryImpl(orderGrpcClient);
        OrderItemRepository orderItemRepo = new OrderItemRepositoryImpl(orderItemGrpcClient);
        ShippingAddressQueryRepository shippingAddressRepo = new ShippingAddressQueryRepositoryImpl(shippingGrpcClient);

        // 4. Initialize Kafka Service
        Map<String, String> kafkaConfig = new HashMap<>();
        kafkaConfig.put("bootstrap.servers", cfg.getKafkaBrokers());
        kafkaConfig.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaConfig.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaConfig.put("acks", "1");
        KafkaProducer<String, String> producer = KafkaProducer.create(vertx, kafkaConfig);
        this.kafkaService = new KafkaService(producer);

        // 5. Initialize Caching
        RedisAPI redisAPI = RedisConfig.createClient(vertx);
        RedisService redisService = new RedisService(redisAPI, openTelemetry);

        // 6. Initialize Services
        TransactionQueryService queryService = new TransactionQueryServiceImpl(queryRepo, redisService, tracingMetrics);
        TransactionCommandService cmdService = new TransactionCommandServiceImpl(
                cmdRepo, merchantQueryRepo, orderQueryRepo, orderItemRepo, shippingAddressRepo, userQueryRepo, redisService, tracingMetrics, kafkaService
        );
        TransactionStatsService statsService = new TransactionStatsServiceImpl(statsRepo, redisService, tracingMetrics);
        TransactionStatsByMerchantService merchantStatsService = new TransactionStatsByMerchantServiceImpl(merchantStatsRepo, redisService, tracingMetrics);

        // 7. Initialize Handlers
        var queryHandler = new TransactionQueryHandler(queryService);
        var cmdHandler = new TransactionCommandHandler(cmdService);
        var statsHandler = new TransactionStatsHandler(statsService);
        var merchantStatsHandler = new TransactionStatsByMerchantHandler(merchantStatsService);

        int port = cfg.getGrpcPort() > 0 ? cfg.getGrpcPort() : 50059;

        startGrpcServer(queryHandler, cmdHandler, statsHandler, merchantStatsHandler, port)
                .onSuccess(v -> {
                    log.info("TransactionVerticle fully initialized with CQRS. Listening for gRPC on port {}", port);
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
        if (kafkaService != null) {
            kafkaService.close();
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
            TransactionQueryHandler queryHandler,
            TransactionCommandHandler cmdHandler,
            TransactionStatsHandler statsHandler,
            TransactionStatsByMerchantHandler merchantStatsHandler,
            int grpcPort) {

        GrpcServer grpcServer = GrpcServer.server(vertx);

        queryHandler.bindAll(grpcServer);
        cmdHandler.bindAll(grpcServer);
        statsHandler.bindAll(grpcServer);
        merchantStatsHandler.bindAll(grpcServer);

        return vertx.createHttpServer()
                .requestHandler(grpcServer)
                .listen(grpcPort)
                .mapEmpty();
    }
}
