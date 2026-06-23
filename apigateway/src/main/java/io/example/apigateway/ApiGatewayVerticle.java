package io.example.apigateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.apigateway.handler.AuthProxyHandler;
import io.example.apigateway.handler.BannerProxyHandler;
import io.example.apigateway.handler.CartProxyHandler;
import io.example.apigateway.handler.CategoryProxyHandler;
import io.example.apigateway.handler.MerchantAwardProxyHandler;
import io.example.apigateway.handler.MerchantBusinessProxyHandler;
import io.example.apigateway.handler.MerchantDetailProxyHandler;
import io.example.apigateway.handler.MerchantPolicyProxyHandler;
import io.example.apigateway.handler.MerchantProxyHandler;
import io.example.apigateway.handler.OrderItemProxyHandler;
import io.example.apigateway.handler.OrderProxyHandler;
import io.example.apigateway.handler.ProductProxyHandler;
import io.example.apigateway.handler.ReviewDetailProxyHandler;
import io.example.apigateway.handler.ReviewProxyHandler;
import io.example.apigateway.handler.RoleProxyHandler;
import io.example.apigateway.handler.ShippingAddressProxyHandler;
import io.example.apigateway.handler.SliderProxyHandler;
import io.example.apigateway.handler.TransactionProxyHandler;
import io.example.apigateway.handler.UserProxyHandler;
import io.example.apigateway.routes.GatewayRoutes;
import io.example.common.config.JwtConfig;
import io.example.common.config.TelemetryConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.grpc.client.GrpcClient;

public class ApiGatewayVerticle extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(ApiGatewayVerticle.class);

  private TelemetryConfig telemetryConfig;
  private GrpcClient grpcClient;

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    JsonObject config = new JsonObject()
        .put("http_port", 8080)
        .put("service.name", "api-gateway");

    DeploymentOptions options = new DeploymentOptions().setConfig(config);

    vertx.deployVerticle(new ApiGatewayVerticle(), options)
        .onSuccess(id -> {
          log.info("✅ API Gateway successfully deployed! ID: {}", id);
          log.info("🚀 Gateway listening on port 8080");
        })
        .onFailure(err -> {
          log.error("❌ Failed to deploy ApiGatewayVerticle", err);
        });
  }

  @Override
  public void start(Promise<Void> startPromise) {
    JsonObject rawConfig = config();

    // 1. Setup OpenTelemetry for edge monitoring
    JsonObject telConfig = rawConfig.copy();
    if (!telConfig.containsKey("service.name")) {
      telConfig.put("service.name", "api-gateway");
    }
    telemetryConfig = new TelemetryConfig(telConfig);
    telemetryConfig.initialize();

    // 2. Instantiate unified gRPC Client pool
    grpcClient = GrpcClient.client(vertx);

    // 3. Define all SocketAddresses for backend microservices using environment
    // variables
    SocketAddress addrUser = resolveGrpcAddress("USER", "user", 8083);
    SocketAddress addrAuth = SocketAddress.inetSocketAddress(8083, "auth"); // Special case for auth as it might be used
                                                                            // differently
    SocketAddress addrRole = resolveGrpcAddress("ROLE", "role", 8083);
    SocketAddress addrBanner = resolveGrpcAddress("BANNER", "banner", 8083);
    SocketAddress addrCart = resolveGrpcAddress("CART", "cart", 8083);
    SocketAddress addrCategory = resolveGrpcAddress("CATEGORY", "category", 8083);
    SocketAddress addrMerchant = resolveGrpcAddress("MERCHANT", "merchant", 50055);
    SocketAddress addrMerchantAward = resolveGrpcAddress("MERCHANT_AWARD", "merchant_award", 50065);
    SocketAddress addrMerchantBusiness = resolveGrpcAddress("MERCHANT_BUSINESS", "merchant_business", 50066);
    SocketAddress addrMerchantDetail = resolveGrpcAddress("MERCHANT_DETAIL", "merchant_detail", 50067);
    SocketAddress addrMerchantPolicy = resolveGrpcAddress("MERCHANT_POLICY", "merchant_policy", 50068);
    SocketAddress addrOrder = resolveGrpcAddress("ORDER", "order", 50057);
    SocketAddress addrOrderItem = resolveGrpcAddress("ORDER_ITEM", "order-item", 50056);
    SocketAddress addrProduct = resolveGrpcAddress("PRODUCT", "product", 50058);
    SocketAddress addrSlider = resolveGrpcAddress("SLIDER", "slider", 50062);
    SocketAddress addrShipping = resolveGrpcAddress("SHIPPING", "shipping_address", 50063);
    SocketAddress addrReview = resolveGrpcAddress("REVIEW", "review", 50061);
    SocketAddress addrReviewDetail = resolveGrpcAddress("REVIEW_DETAIL", "review_detail", 50069);
    SocketAddress addrTransaction = resolveGrpcAddress("TRANSACTION", "transaction", 50059);

    // 4. Instantiate client stubs pointing to target address channels
    // User
    var userQuery = new pb.user.VertxUserQueryServiceGrpcClient(grpcClient, addrUser);
    var userCmd = new pb.user.VertxUserCommandServiceGrpcClient(grpcClient, addrUser);

    // Auth
    var authClient = new pb.VertxAuthServiceGrpcClient(grpcClient, addrAuth);

    // Role
    var roleQuery = new pb.VertxRoleQueryServiceGrpcClient(grpcClient, addrRole);
    var roleCmd = new pb.VertxRoleCommandServiceGrpcClient(grpcClient, addrRole);

    // Banner
    var bannerQuery = new pb.banner.VertxBannerQueryServiceGrpcClient(grpcClient, addrBanner);
    var bannerCmd = new pb.banner.VertxBannerCommandServiceGrpcClient(grpcClient, addrBanner);

    // Cart
    var cartQuery = new pb.cart.VertxCartQueryServiceGrpcClient(grpcClient, addrCart);
    var cartCmd = new pb.cart.VertxCartCommandServiceGrpcClient(grpcClient, addrCart);

    // Category
    var categoryQuery = new pb.category.VertxCategoryQueryServiceGrpcClient(grpcClient, addrCategory);
    var categoryCmd = new pb.category.VertxCategoryCommandServiceGrpcClient(grpcClient, addrCategory);
    var categoryStats = new pb.category.VertxCategoryStatsServiceGrpcClient(grpcClient, addrCategory);
    var categoryStatsById = new pb.category.VertxCategoryStatsByIdServiceGrpcClient(grpcClient, addrCategory);
    var categoryStatsByMerchant = new pb.category.VertxCategoryStatsByMerchantServiceGrpcClient(grpcClient,
        addrCategory);

    // Merchant
    var merchantQuery = new pb.merchant.VertxMerchantQueryServiceGrpcClient(grpcClient, addrMerchant);
    var merchantCmd = new pb.merchant.VertxMerchantCommandServiceGrpcClient(grpcClient, addrMerchant);

    // Merchant Award
    var merchantAwardQuery = new pb.merchant_award.VertxMerchantAwardQueryServiceGrpcClient(grpcClient,
        addrMerchantAward);
    var merchantAwardCmd = new pb.merchant_award.VertxMerchantAwardCommandServiceGrpcClient(grpcClient,
        addrMerchantAward);

    // Merchant Business
    var merchantBusinessQuery = new pb.merchant_business.VertxMerchantBusinessQueryServiceGrpcClient(grpcClient,
        addrMerchantBusiness);
    var merchantBusinessCmd = new pb.merchant_business.VertxMerchantBusinessCommandServiceGrpcClient(grpcClient,
        addrMerchantBusiness);

    // Merchant Detail
    var merchantDetailQuery = new pb.merchant_detail.VertxMerchantDetailQueryServiceGrpcClient(grpcClient,
        addrMerchantDetail);
    var merchantDetailCmd = new pb.merchant_detail.VertxMerchantDetailCommandServiceGrpcClient(grpcClient,
        addrMerchantDetail);
    var merchantSocialCmd = new pb.VertxMerchantSocialCommandServiceGrpcClient(grpcClient, addrMerchantDetail);

    // Merchant Policy
    var merchantPolicyQuery = new pb.merchant_policy.VertxMerchantPolicyQueryServiceGrpcClient(grpcClient,
        addrMerchantPolicy);
    var merchantPolicyCmd = new pb.merchant_policy.VertxMerchantPolicyCommandServiceGrpcClient(grpcClient,
        addrMerchantPolicy);

    // Order
    var orderQuery = new pb.order.VertxOrderQueryServiceGrpcClient(grpcClient, addrOrder);
    var orderCmd = new pb.order.VertxOrderCommandServiceGrpcClient(grpcClient, addrOrder);
    var orderStats = new pb.order.VertxOrderStatsServiceGrpcClient(grpcClient, addrOrder);

    // Order Item
    var orderItemQuery = new pb.order_item.VertxOrderItemQueryServiceGrpcClient(grpcClient, addrOrderItem);

    // Product
    var productQuery = new pb.product.VertxProductQueryServiceGrpcClient(grpcClient, addrProduct);
    var productCmd = new pb.product.VertxProductCommandServiceGrpcClient(grpcClient, addrProduct);

    // Slider
    var sliderQuery = new pb.slider.VertxSliderQueryServiceGrpcClient(grpcClient, addrSlider);
    var sliderCmd = new pb.slider.VertxSliderCommandServiceGrpcClient(grpcClient, addrSlider);

    // Shipping Address
    var shippingQuery = new pb.shipping_address.VertxShippingQueryServiceGrpcClient(grpcClient, addrShipping);
    var shippingCmd = new pb.shipping_address.VertxShippingCommandServiceGrpcClient(grpcClient, addrShipping);

    // Review
    var reviewQuery = new pb.review.VertxReviewQueryServiceGrpcClient(grpcClient, addrReview);
    var reviewCmd = new pb.review.VertxReviewCommandServiceGrpcClient(grpcClient, addrReview);

    // Review Detail
    var reviewDetailQuery = new pb.review_detail.VertxReviewDetailQueryServiceGrpcClient(grpcClient, addrReviewDetail);
    var reviewDetailCmd = new pb.review_detail.VertxReviewDetailCommandServiceGrpcClient(grpcClient, addrReviewDetail);

    // Transaction
    var transactionQuery = new pb.transaction.VertxTransactionQueryServiceGrpcClient(grpcClient, addrTransaction);
    var transactionCmd = new pb.transaction.VertxTransactionCommandServiceGrpcClient(grpcClient, addrTransaction);
    var transactionStats = new pb.transaction.VertxTransactionStatsServiceGrpcClient(grpcClient, addrTransaction);
    var transactionStatsByMerchant = new pb.transaction.VertxTransactionStatsByMerchantServiceGrpcClient(grpcClient,
        addrTransaction);

    // 5. Setup Security Utilities
    JWTAuth jwtAuth = JwtConfig.createProvider(vertx);

    // 6. Instantiate high-performance Proxy Handlers
    var authHandler = new AuthProxyHandler(authClient);
    var userHandler = new UserProxyHandler(userQuery, userCmd);
    var roleHandler = new RoleProxyHandler(roleQuery, roleCmd);
    var bannerHandler = new BannerProxyHandler(bannerQuery, bannerCmd);
    var cartHandler = new CartProxyHandler(cartQuery, cartCmd);
    var categoryHandler = new CategoryProxyHandler(categoryQuery, categoryCmd, categoryStats, categoryStatsById,
        categoryStatsByMerchant);
    var merchantHandler = new MerchantProxyHandler(merchantQuery, merchantCmd);
    var merchantAwardHandler = new MerchantAwardProxyHandler(merchantAwardQuery, merchantAwardCmd);
    var merchantBusinessHandler = new MerchantBusinessProxyHandler(merchantBusinessQuery, merchantBusinessCmd);
    var merchantDetailHandler = new MerchantDetailProxyHandler(merchantDetailQuery, merchantDetailCmd,
        merchantSocialCmd);
    var merchantPolicyHandler = new MerchantPolicyProxyHandler(merchantPolicyQuery, merchantPolicyCmd);
    var orderHandler = new OrderProxyHandler(orderQuery, orderCmd, orderStats);
    var orderItemHandler = new OrderItemProxyHandler(orderItemQuery);
    var productHandler = new ProductProxyHandler(productQuery, productCmd);
    var sliderHandler = new SliderProxyHandler(sliderQuery, sliderCmd);
    var shippingAddressHandler = new ShippingAddressProxyHandler(shippingQuery, shippingCmd);
    var reviewHandler = new ReviewProxyHandler(reviewQuery, reviewCmd);
    var reviewDetailHandler = new ReviewDetailProxyHandler(reviewDetailQuery, reviewDetailCmd);
    var transactionHandler = new TransactionProxyHandler(transactionQuery, transactionCmd, transactionStats,
        transactionStatsByMerchant);

    // 7. Configure web routers & launch web interface
    Router baseRouter = Router.router(vertx);

    // Initialize and hook up Chaos Engineering HTTP Middleware
    io.example.common.chaos.ChaosManager chaosManager = new io.example.common.chaos.ChaosManager();
    chaosManager.startWatcher(vertx);
    baseRouter.route().handler(new io.example.common.chaos.ChaosHttpMiddleware(chaosManager));

    Router registeredRouter = GatewayRoutes.register(
        baseRouter,
        jwtAuth,
        authHandler,
        userHandler,
        roleHandler,
        bannerHandler,
        cartHandler,
        categoryHandler,
        merchantHandler,
        merchantAwardHandler,
        merchantBusinessHandler,
        merchantDetailHandler,
        merchantPolicyHandler,
        orderHandler,
        orderItemHandler,
        productHandler,
        sliderHandler,
        shippingAddressHandler,
        reviewHandler,
        reviewDetailHandler,
        transactionHandler,
        chaosManager);

    int port = rawConfig.getInteger("http_port", 8080);
    String envHttpPort = System.getenv("HTTP_PORT");
    if (envHttpPort != null)
      port = Integer.parseInt(envHttpPort);

    final int finalPort = port;

    vertx.createHttpServer(new HttpServerOptions().setCompressionSupported(true))
        .requestHandler(registeredRouter)
        .listen(finalPort)
        .onSuccess(srv -> {
          log.info("====================================================================");
          log.info("🚀 API Gateway HUB successfully started at Port {}", finalPort);
          log.info("📈 OpenTelemetry instrumentation active for tracing.");
          log.info("====================================================================");
          startPromise.complete();
        })
        .onFailure(err -> {
          log.error("❌ CRITICAL: Failed to launch HTTP server for Gateway", err);
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
}
