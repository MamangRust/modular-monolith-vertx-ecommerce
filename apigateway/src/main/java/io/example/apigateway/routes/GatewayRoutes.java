package io.example.apigateway.routes;

import io.example.apigateway.handler.AuthProxyHandler;
import io.example.apigateway.handler.RoleProxyHandler;
import io.example.apigateway.handler.UserProxyHandler;
import io.example.apigateway.handler.BannerProxyHandler;
import io.example.apigateway.handler.CartProxyHandler;
import io.example.apigateway.handler.CategoryProxyHandler;
import io.example.apigateway.handler.MerchantProxyHandler;
import io.example.apigateway.handler.MerchantAwardProxyHandler;
import io.example.apigateway.handler.MerchantBusinessProxyHandler;
import io.example.apigateway.handler.MerchantDetailProxyHandler;
import io.example.apigateway.handler.MerchantPolicyProxyHandler;
import io.example.apigateway.handler.OrderProxyHandler;
import io.example.apigateway.handler.OrderItemProxyHandler;
import io.example.apigateway.handler.ProductProxyHandler;
import io.example.apigateway.handler.SliderProxyHandler;
import io.example.apigateway.handler.ShippingAddressProxyHandler;
import io.example.apigateway.handler.ReviewProxyHandler;
import io.example.apigateway.handler.ReviewDetailProxyHandler;
import io.example.apigateway.handler.TransactionProxyHandler;
import io.example.apigateway.middleware.JwtMiddleware;
import io.example.apigateway.middleware.RoleMiddleware;
import io.example.common.chaos.ChaosManager;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public final class GatewayRoutes {
  private GatewayRoutes() {
  }

  public static Router register(
      Router router,
      JWTAuth jwtAuth,
      AuthProxyHandler auth,
      UserProxyHandler user,
      RoleProxyHandler role,
      BannerProxyHandler banner,
      CartProxyHandler cart,
      CategoryProxyHandler category,
      MerchantProxyHandler merchant,
      MerchantAwardProxyHandler merchantAward,
      MerchantBusinessProxyHandler merchantBusiness,
      MerchantDetailProxyHandler merchantDetail,
      MerchantPolicyProxyHandler merchantPolicy,
      OrderProxyHandler order,
      OrderItemProxyHandler orderItem,
      ProductProxyHandler product,
      SliderProxyHandler slider,
      ShippingAddressProxyHandler shippingAddress,
      ReviewProxyHandler review,
      ReviewDetailProxyHandler reviewDetail,
      TransactionProxyHandler transaction,
      ChaosManager chaosManager) {

    // 1. Global middleware (BodyParser is required for all JSON posts)
    router.route().handler(BodyHandler.create());

    // 2. Public / Health routes
    router.get("/health").handler(ctx -> ctx.response()
        .putHeader("Content-Type", "application/json")
        .end("{\"status\":\"UP\",\"service\":\"gateway\"}"));

    // =========================================================================
    // AUTH ROUTES (No API prefix in monolithic routes)
    // =========================================================================
    router.post("/register").handler(auth::register);
    router.post("/login").handler(auth::login);
    router.post("/refresh-token").handler(auth::refreshToken);
    router.get("/me").handler(JwtMiddleware.jwt(jwtAuth)).handler(auth::getMe);
    router.get("/logout").handler(JwtMiddleware.jwt(jwtAuth)).handler(auth::logout);

    // =========================================================================
    // USER ROUTES (JWT guarded, Admin only for list views)
    // =========================================================================
    router.route("/users*").handler(JwtMiddleware.jwt(jwtAuth));
    router.get("/users/active").handler(RoleMiddleware.requireRole("ADMIN")).handler(user::findActive);
    router.get("/users/trashed").handler(RoleMiddleware.requireRole("ADMIN")).handler(user::findTrashed);
    router.get("/users").handler(RoleMiddleware.requireRole("ADMIN")).handler(user::findAll);
    router.get("/users/:id").handler(user::findById);
    router.post("/users/update/:id").handler(user::update);
    router.post("/users/restore/:id").handler(user::restore);
    router.post("/users/trashed/:id").handler(user::trashed);
    router.delete("/users/deletePermanent/:id").handler(user::deletePermanent);
    router.post("/users/restore-all").handler(user::restoreAllUsers);
    router.post("/users/delete-all").handler(user::deleteAllPermanentUsers);

    // =========================================================================
    // ROLE ROUTES (JWT guarded, Admin restricted)
    // =========================================================================
    router.route("/roles*").handler(JwtMiddleware.jwt(jwtAuth));
    router.get("/roles/active").handler(RoleMiddleware.requireRole("ADMIN")).handler(role::findActive);
    router.get("/roles/trashed").handler(RoleMiddleware.requireRole("ADMIN")).handler(role::findTrashed);
    router.get("/roles").handler(RoleMiddleware.requireRole("ADMIN")).handler(role::findAll);
    router.get("/roles/:id").handler(role::findById);
    router.post("/roles").handler(role::create);
    router.post("/roles/:id").handler(role::update);
    router.post("/roles/restore/:id").handler(role::restore);
    router.post("/roles/trashed/:id").handler(role::trashed);
    router.delete("/roles/deletePermanent/:id").handler(role::deletePermanent);
    router.post("/roles/restore-all").handler(role::restoreAllRoles);
    router.post("/roles/delete-all").handler(role::deleteAllPermanentRoles);

    // =========================================================================
    // BANNER ROUTES
    // =========================================================================
    router.get("/banners").handler(banner::findAll);
    router.get("/banners/active").handler(banner::findActive);
    router.get("/banners/:id").handler(banner::findById);

    // Admin restricted banner routes
    router.route("/banners/trashed*").handler(JwtMiddleware.jwt(jwtAuth)).handler(RoleMiddleware.requireRole("ADMIN"));
    router.get("/banners/trashed").handler(banner::findTrashed);

    router.route("/banners/manage*").handler(JwtMiddleware.jwt(jwtAuth)).handler(RoleMiddleware.requireRole("ADMIN"));
    router.post("/banners/manage").handler(banner::create);
    router.post("/banners/manage/:id").handler(banner::update);
    router.post("/banners/manage/restore/:id").handler(banner::restore);
    router.post("/banners/manage/trashed/:id").handler(banner::trash);
    router.delete("/banners/manage/deletePermanent/:id").handler(banner::deletePermanent);
    router.post("/banners/manage/restore-all").handler(banner::restoreAll);
    router.delete("/banners/manage/delete-all").handler(banner::deleteAll);

    // =========================================================================
    // CART ROUTES (JWT guarded)
    // =========================================================================
    router.route("/carts*").handler(JwtMiddleware.jwt(jwtAuth));
    router.get("/carts").handler(cart::findAll);
    router.post("/carts").handler(cart::create);
    router.delete("/carts/:id").handler(cart::delete);
    router.delete("/carts").handler(cart::deleteAll);

    // =========================================================================
    // CATEGORY ROUTES
    // =========================================================================
    router.get("/categories").handler(category::findAll);
    router.get("/categories/active").handler(category::findActive);
    router.get("/categories/:id").handler(category::findById);

    // Admin restricted category write routes
    router.route("/categories/manage*").handler(JwtMiddleware.jwt(jwtAuth)).handler(RoleMiddleware.requireRole("ADMIN"));
    router.post("/categories/manage").handler(category::create);
    router.post("/categories/manage/:id").handler(category::update);
    router.post("/categories/manage/restore/:id").handler(category::restore);
    router.post("/categories/manage/trashed/:id").handler(category::trash);
    router.delete("/categories/manage/deletePermanent/:id").handler(category::deletePermanent);
    router.post("/categories/manage/restore-all").handler(category::restoreAll);
    router.delete("/categories/manage/delete-all").handler(category::deleteAll);

    // Admin restricted category stats routes
    router.route("/categories/stats*").handler(JwtMiddleware.jwt(jwtAuth)).handler(RoleMiddleware.requireRole("ADMIN"));
    router.get("/categories/stats/monthly-total-prices").handler(category::findMonthlyTotalPrices);
    router.get("/categories/stats/yearly-total-prices").handler(category::findYearlyTotalPrices);
    router.get("/categories/stats/month-price").handler(category::findMonthPrice);
    router.get("/categories/stats/year-price").handler(category::findYearPrice);

    router.get("/categories/stats/monthly-total-prices/id/:id").handler(category::findMonthlyTotalPricesById);
    router.get("/categories/stats/yearly-total-prices/id/:id").handler(category::findYearlyTotalPricesById);
    router.get("/categories/stats/month-price/id/:id").handler(category::findMonthPriceById);
    router.get("/categories/stats/year-price/id/:id").handler(category::findYearPriceById);

    router.get("/categories/stats/monthly-total-prices/merchant/:id").handler(category::findMonthlyTotalPricesByMerchant);
    router.get("/categories/stats/yearly-total-prices/merchant/:id").handler(category::findYearlyTotalPricesByMerchant);
    router.get("/categories/stats/month-price/merchant/:id").handler(category::findMonthPriceByMerchant);
    router.get("/categories/stats/year-price/merchant/:id").handler(category::findYearPriceByMerchant);

    // =========================================================================
    // MERCHANT ROUTES
    // =========================================================================
    router.get("/merchants").handler(merchant::findAll);
    router.get("/merchants/active").handler(merchant::findActive);
    router.get("/merchants/trashed").handler(merchant::findTrashed);
    router.get("/merchants/:id").handler(merchant::findById);

    router.route("/merchants/manage*").handler(JwtMiddleware.jwt(jwtAuth));
    router.post("/merchants/manage").handler(merchant::create);
    router.post("/merchants/manage/:id").handler(merchant::update);
    router.post("/merchants/manage/status/:id").handler(merchant::updateStatus);
    router.post("/merchants/manage/restore/:id").handler(merchant::restore);
    router.post("/merchants/manage/trashed/:id").handler(merchant::trash);
    router.delete("/merchants/manage/deletePermanent/:id").handler(merchant::deletePermanent);
    router.post("/merchants/manage/restore-all").handler(merchant::restoreAll);
    router.delete("/merchants/manage/delete-all").handler(merchant::deleteAllPermanent);

    // =========================================================================
    // PRODUCT ROUTES
    // =========================================================================
    router.get("/products").handler(product::findAll);
    router.get("/products/active").handler(product::findActive);
    router.get("/products/:id").handler(product::findById);
    router.get("/products/merchant/:merchantId").handler(product::findByMerchant);
    router.get("/products/category/:categoryName").handler(product::findByCategory);

    // Guarded product routes
    router.route("/products/manage*").handler(JwtMiddleware.jwt(jwtAuth));
    router.post("/products/manage").handler(product::create);
    router.post("/products/manage/:id").handler(product::update);
    router.post("/products/manage/stock/:id").handler(product::updateStock);
    router.post("/products/manage/restore/:id").handler(product::restore);
    router.post("/products/manage/trashed/:id").handler(product::trash);
    router.delete("/products/manage/deletePermanent/:id").handler(product::deletePermanent);
    router.post("/products/manage/restore-all").handler(product::restoreAll);
    router.delete("/products/manage/delete-all").handler(product::deleteAll);

    router.route("/products/trashed*").handler(JwtMiddleware.jwt(jwtAuth)).handler(RoleMiddleware.requireRole("ADMIN"));
    router.get("/products/trashed").handler(product::findTrashed);

    // =========================================================================
    // ORDER ROUTES (Guarded)
    // =========================================================================
    router.route("/orders*").handler(JwtMiddleware.jwt(jwtAuth));
    router.get("/orders").handler(order::findAll);
    router.get("/orders/active").handler(order::findActive);
    router.get("/orders/:id").handler(order::findById);
    router.post("/orders").handler(order::create);
    router.post("/orders/:id").handler(order::update);
    router.post("/orders/restore/:id").handler(order::restore);
    router.post("/orders/trashed/:id").handler(order::trash);
    router.delete("/orders/deletePermanent/:id").handler(order::deletePermanent);

    // Admin guarded order routes
    router.route("/orders/manage*").handler(RoleMiddleware.requireRole("ADMIN"));
    router.get("/orders/trashed").handler(RoleMiddleware.requireRole("ADMIN")).handler(order::findTrashed);
    router.post("/orders/manage/restore-all").handler(order::restoreAll);
    router.delete("/orders/manage/delete-all").handler(order::deleteAll);

    // Order Stats
    router.get("/orders/stats/monthly-total-revenue").handler(order::findMonthlyTotalRevenue);
    router.get("/orders/stats/yearly-total-revenue").handler(order::findYearlyTotalRevenue);
    router.get("/orders/stats/monthly-total-revenue/merchant/:id").handler(order::findMonthlyTotalRevenueByMerchant);
    router.get("/orders/stats/yearly-total-revenue/merchant/:id").handler(order::findYearlyTotalRevenueByMerchant);
    router.get("/orders/stats/monthly-revenue").handler(order::findMonthlyRevenue);
    router.get("/orders/stats/yearly-revenue").handler(order::findYearlyRevenue);
    router.get("/orders/stats/monthly-revenue/merchant/:id").handler(order::findMonthlyRevenueByMerchant);
    router.get("/orders/stats/yearly-revenue/merchant/:id").handler(order::findYearlyRevenueByMerchant);

    // =========================================================================
    // ORDER ITEM ROUTES (Guarded)
    // =========================================================================
    router.route("/order-items*").handler(JwtMiddleware.jwt(jwtAuth));
    router.get("/order-items").handler(orderItem::findAll);
    router.get("/order-items/active").handler(orderItem::findActive);
    router.get("/order-items/order/:orderId").handler(orderItem::findOrderItemByOrder);

    // Admin order-items
    router.get("/order-items/trashed").handler(RoleMiddleware.requireRole("ADMIN")).handler(orderItem::findTrashed);

    // =========================================================================
    // MERCHANT AWARD ROUTES
    // =========================================================================
    router.get("/merchant-awards").handler(merchantAward::findAll);
    router.get("/merchant-awards/active").handler(merchantAward::findActive);
    router.get("/merchant-awards/trashed").handler(merchantAward::findTrashed);
    router.get("/merchant-awards/:id").handler(merchantAward::findById);

    router.route("/merchant-awards/manage*").handler(JwtMiddleware.jwt(jwtAuth));
    router.post("/merchant-awards/manage").handler(merchantAward::create);
    router.post("/merchant-awards/manage/:id").handler(merchantAward::update);
    router.post("/merchant-awards/manage/restore/:id").handler(merchantAward::restore);
    router.post("/merchant-awards/manage/trashed/:id").handler(merchantAward::trash);
    router.delete("/merchant-awards/manage/deletePermanent/:id").handler(merchantAward::deletePermanent);
    router.post("/merchant-awards/manage/restore-all").handler(merchantAward::restoreAll);
    router.delete("/merchant-awards/manage/delete-all").handler(merchantAward::deleteAllPermanent);

    // =========================================================================
    // MERCHANT BUSINESS ROUTES
    // =========================================================================
    router.get("/merchant-businesses").handler(merchantBusiness::findAll);
    router.get("/merchant-businesses/active").handler(merchantBusiness::findActive);
    router.get("/merchant-businesses/trashed").handler(merchantBusiness::findTrashed);
    router.get("/merchant-businesses/:id").handler(merchantBusiness::findById);

    router.route("/merchant-businesses/manage*").handler(JwtMiddleware.jwt(jwtAuth));
    router.post("/merchant-businesses/manage").handler(merchantBusiness::create);
    router.post("/merchant-businesses/manage/:id").handler(merchantBusiness::update);
    router.post("/merchant-businesses/manage/restore/:id").handler(merchantBusiness::restore);
    router.post("/merchant-businesses/manage/trashed/:id").handler(merchantBusiness::trash);
    router.delete("/merchant-businesses/manage/deletePermanent/:id").handler(merchantBusiness::deletePermanent);
    router.post("/merchant-businesses/manage/restore-all").handler(merchantBusiness::restoreAll);
    router.delete("/merchant-businesses/manage/delete-all").handler(merchantBusiness::deleteAllPermanent);

    // =========================================================================
    // MERCHANT DETAIL ROUTES
    // =========================================================================
    router.get("/merchant-details").handler(merchantDetail::findAll);
    router.get("/merchant-details/active").handler(merchantDetail::findActive);
    router.get("/merchant-details/trashed").handler(merchantDetail::findTrashed);
    router.get("/merchant-details/:id").handler(merchantDetail::findById);

    router.route("/merchant-details/manage*").handler(JwtMiddleware.jwt(jwtAuth));
    router.post("/merchant-details/manage").handler(merchantDetail::create);
    router.post("/merchant-details/manage/:id").handler(merchantDetail::update);
    router.post("/merchant-details/manage/restore/:id").handler(merchantDetail::restore);
    router.post("/merchant-details/manage/trashed/:id").handler(merchantDetail::trash);
    router.delete("/merchant-details/manage/deletePermanent/:id").handler(merchantDetail::deletePermanent);
    router.post("/merchant-details/manage/restore-all").handler(merchantDetail::restoreAll);
    router.delete("/merchant-details/manage/delete-all").handler(merchantDetail::deleteAllPermanent);

    // Social Media Links
    router.post("/merchant-details/manage/socials").handler(merchantDetail::createSocial);
    router.post("/merchant-details/manage/socials/:id").handler(merchantDetail::updateSocial);

    // =========================================================================
    // MERCHANT POLICY ROUTES
    // =========================================================================
    router.get("/merchant-policies").handler(merchantPolicy::findAll);
    router.get("/merchant-policies/active").handler(merchantPolicy::findActive);
    router.get("/merchant-policies/trashed").handler(merchantPolicy::findTrashed);
    router.get("/merchant-policies/:id").handler(merchantPolicy::findById);

    router.route("/merchant-policies/manage*").handler(JwtMiddleware.jwt(jwtAuth));
    router.post("/merchant-policies/manage").handler(merchantPolicy::create);
    router.post("/merchant-policies/manage/:id").handler(merchantPolicy::update);
    router.post("/merchant-policies/manage/restore/:id").handler(merchantPolicy::restore);
    router.post("/merchant-policies/manage/trashed/:id").handler(merchantPolicy::trash);
    router.delete("/merchant-policies/manage/deletePermanent/:id").handler(merchantPolicy::deletePermanent);
    router.post("/merchant-policies/manage/restore-all").handler(merchantPolicy::restoreAll);
    router.delete("/merchant-policies/manage/delete-all").handler(merchantPolicy::deleteAllPermanent);

    // =========================================================================
    // SLIDER ROUTES
    // =========================================================================
    router.get("/sliders").handler(slider::findAll);
    router.get("/sliders/active").handler(slider::findActive);
    router.get("/sliders/trashed").handler(slider::findTrashed);
    router.get("/sliders/:id").handler(slider::findById);

    router.route("/sliders/manage*").handler(JwtMiddleware.jwt(jwtAuth));
    router.post("/sliders/manage").handler(slider::create);
    router.post("/sliders/manage/:id").handler(slider::update);
    router.post("/sliders/manage/restore/:id").handler(slider::restore);
    router.post("/sliders/manage/trashed/:id").handler(slider::trash);
    router.delete("/sliders/manage/deletePermanent/:id").handler(slider::deletePermanent);
    router.post("/sliders/manage/restore-all").handler(slider::restoreAll);
    router.delete("/sliders/manage/delete-all").handler(slider::deleteAll);

    // =========================================================================
    // SHIPPING ADDRESS ROUTES
    // =========================================================================
    router.get("/shipping-addresses").handler(shippingAddress::findAll);
    router.get("/shipping-addresses/active").handler(shippingAddress::findActive);
    router.get("/shipping-addresses/trashed").handler(shippingAddress::findTrashed);
    router.get("/shipping-addresses/:id").handler(shippingAddress::findById);
    router.get("/shipping-addresses/order/:orderId").handler(shippingAddress::findByOrder);

    router.route("/shipping-addresses/manage*").handler(JwtMiddleware.jwt(jwtAuth));
    router.post("/shipping-addresses/manage").handler(shippingAddress::create);
    router.post("/shipping-addresses/manage/:id").handler(shippingAddress::update);
    router.post("/shipping-addresses/manage/restore/:id").handler(shippingAddress::restore);
    router.post("/shipping-addresses/manage/trashed/:id").handler(shippingAddress::trash);
    router.delete("/shipping-addresses/manage/deletePermanent/:id").handler(shippingAddress::deletePermanent);
    router.delete("/shipping-addresses/manage/deleteByOrderPermanent/:orderId").handler(shippingAddress::deleteByOrderPermanent);
    router.post("/shipping-addresses/manage/restore-all").handler(shippingAddress::restoreAll);
    router.delete("/shipping-addresses/manage/delete-all").handler(shippingAddress::deleteAll);

    // =========================================================================
    // REVIEW ROUTES
    // =========================================================================
    router.get("/reviews").handler(review::findAll);
    router.get("/reviews/active").handler(review::findActive);
    router.get("/reviews/trashed").handler(review::findTrashed);
    router.get("/reviews/:id").handler(review::findById);
    router.get("/reviews/product/:productId").handler(review::findByProduct);
    router.get("/reviews/merchant/:merchantId").handler(review::findByMerchant);

    router.route("/reviews/manage*").handler(JwtMiddleware.jwt(jwtAuth));
    router.post("/reviews/manage").handler(review::create);
    router.post("/reviews/manage/:id").handler(review::update);
    router.post("/reviews/manage/restore/:id").handler(review::restore);
    router.post("/reviews/manage/trashed/:id").handler(review::trash);
    router.delete("/reviews/manage/deletePermanent/:id").handler(review::deletePermanent);
    router.post("/reviews/manage/restore-all").handler(review::restoreAll);
    router.delete("/reviews/manage/delete-all").handler(review::deleteAll);

    // =========================================================================
    // REVIEW DETAIL ROUTES
    // =========================================================================
    router.get("/review-details").handler(reviewDetail::findAll);
    router.get("/review-details/active").handler(reviewDetail::findActive);
    router.get("/review-details/trashed").handler(reviewDetail::findTrashed);
    router.get("/review-details/:id").handler(reviewDetail::findById);

    router.route("/review-details/manage*").handler(JwtMiddleware.jwt(jwtAuth));
    router.post("/review-details/manage").handler(reviewDetail::create);
    router.post("/review-details/manage/:id").handler(reviewDetail::update);
    router.post("/review-details/manage/restore/:id").handler(reviewDetail::restore);
    router.post("/review-details/manage/trashed/:id").handler(reviewDetail::trash);
    router.delete("/review-details/manage/deletePermanent/:id").handler(reviewDetail::deletePermanent);
    router.post("/review-details/manage/restore-all").handler(reviewDetail::restoreAll);
    router.delete("/review-details/manage/delete-all").handler(reviewDetail::deleteAll);

    // =========================================================================
    // TRANSACTION ROUTES
    // =========================================================================
    router.get("/transactions").handler(transaction::findAll);
    router.get("/transactions/active").handler(transaction::findActive);
    router.get("/transactions/trashed").handler(transaction::findTrashed);
    router.get("/transactions/:id").handler(transaction::findById);
    router.get("/transactions/order/:orderId").handler(transaction::findByOrderId);
    router.get("/transactions/merchant/:merchantId").handler(transaction::findByMerchant);

    router.route("/transactions/manage*").handler(JwtMiddleware.jwt(jwtAuth));
    router.post("/transactions/manage").handler(transaction::create);
    router.post("/transactions/manage/:id").handler(transaction::update);
    router.post("/transactions/manage/restore/:id").handler(transaction::restore);
    router.post("/transactions/manage/trashed/:id").handler(transaction::trash);
    router.delete("/transactions/manage/deletePermanent/:id").handler(transaction::deletePermanent);
    router.delete("/transactions/manage/deleteByOrderPermanent/:orderId").handler(transaction::deleteByOrderPermanent);
    router.post("/transactions/manage/restore-all").handler(transaction::restoreAll);
    router.delete("/transactions/manage/delete-all").handler(transaction::deleteAll);

    // Stats
    router.route("/transactions/stats*").handler(JwtMiddleware.jwt(jwtAuth));
    router.get("/transactions/stats/monthly-success").handler(transaction::getMonthlyAmountSuccess);
    router.get("/transactions/stats/yearly-success").handler(transaction::getYearlyAmountSuccess);
    router.get("/transactions/stats/monthly-failed").handler(transaction::getMonthlyAmountFailed);
    router.get("/transactions/stats/yearly-failed").handler(transaction::getYearlyAmountFailed);
    router.get("/transactions/stats/monthly-method-success").handler(transaction::getMonthlyTransactionMethodSuccess);
    router.get("/transactions/stats/yearly-method-success").handler(transaction::getYearlyTransactionMethodSuccess);
    router.get("/transactions/stats/monthly-method-failed").handler(transaction::getMonthlyTransactionMethodFailed);
    router.get("/transactions/stats/yearly-method-failed").handler(transaction::getYearlyTransactionMethodFailed);

    // Stats By Merchant
    router.get("/transactions/stats/merchant/:merchantId/monthly-success").handler(transaction::getMonthlyAmountSuccessByMerchant);
    router.get("/transactions/stats/merchant/:merchantId/yearly-success").handler(transaction::getYearlyAmountSuccessByMerchant);
    router.get("/transactions/stats/merchant/:merchantId/monthly-failed").handler(transaction::getMonthlyAmountFailedByMerchant);
    router.get("/transactions/stats/merchant/:merchantId/yearly-failed").handler(transaction::getYearlyAmountFailedByMerchant);
    router.get("/transactions/stats/merchant/:merchantId/monthly-method-success").handler(transaction::getMonthlyTransactionMethodSuccessByMerchant);
    router.get("/transactions/stats/merchant/:merchantId/yearly-method-success").handler(transaction::getYearlyTransactionMethodSuccessByMerchant);
    router.get("/transactions/stats/merchant/:merchantId/monthly-method-failed").handler(transaction::getMonthlyTransactionMethodFailedByMerchant);
    router.get("/transactions/stats/merchant/:merchantId/yearly-method-failed").handler(transaction::getYearlyTransactionMethodFailedByMerchant);

    // =========================================================================
    // CHAOS CONTROL PLANE ROUTES
    // =========================================================================
    router.get("/api/chaos/policies").handler(ctx -> {
      JsonArray policiesArr = new JsonArray();
      chaosManager.getPolicies().forEach(policy -> {
        policiesArr.add(JsonObject.mapFrom(policy));
      });
      ctx.response()
          .putHeader("Content-Type", "application/json")
          .end(new JsonObject().put("policies", policiesArr).encodePrettily());
    });

    router.post("/api/chaos/halt").handler(ctx -> {
      chaosManager.halt();
      ctx.response()
          .putHeader("Content-Type", "application/json")
          .end(new JsonObject().put("status", "success").put("message", "All chaos experiments halted").encodePrettily());
    });

    router.post("/api/chaos/policies/reload").handler(ctx -> {
      chaosManager.loadConfig();
      ctx.response()
          .putHeader("Content-Type", "application/json")
          .end(new JsonObject().put("status", "success").put("message", "Chaos policies reloaded").encodePrettily());
    });

    return router;
  }
}
