package io.example.apigateway.handler;

import static io.example.apigateway.utils.GrpcGatewayUtils.sendResponse;

import io.example.apigateway.utils.GrpcGatewayUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pb.order.OrderCommon;
import pb.order.OrderCommand;
import pb.order.OrderQuery;
import pb.order.VertxOrderQueryServiceGrpcClient;
import pb.order.VertxOrderCommandServiceGrpcClient;
import pb.order.VertxOrderStatsServiceGrpcClient;

@Slf4j
@RequiredArgsConstructor
public class OrderProxyHandler {
        private final VertxOrderQueryServiceGrpcClient queryClient;
        private final VertxOrderCommandServiceGrpcClient commandClient;
        private final VertxOrderStatsServiceGrpcClient statsClient;

        public void findAll(RoutingContext ctx) {
                var req = OrderQuery.FindAllOrderRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findAll(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findActive(RoutingContext ctx) {
                var req = OrderQuery.FindAllOrderRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findByActive(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findTrashed(RoutingContext ctx) {
                var req = OrderQuery.FindAllOrderRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findByTrashed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findById(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = OrderCommon.FindByIdOrderRequest.newBuilder().setId(id).build();

                queryClient.findById(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void create(RoutingContext ctx) {
                if (ctx.user() == null || ctx.user().principal() == null) {
                        GrpcGatewayUtils.handleError(ctx, new Exception("Unauthorized"));
                        return;
                }

                int userId = ctx.user().principal().getInteger("userId", 0);
                JsonObject body = ctx.body().asJsonObject();

                var builder = OrderCommand.CreateOrderRequest.newBuilder()
                                .setMerchantId(GrpcGatewayUtils.getJsonInteger(body, "merchant_id", 0))
                                .setUserId(userId)
                                .setTotalPrice(GrpcGatewayUtils.getJsonInteger(body, "total_price", 0));

                JsonArray itemsArr = body.getJsonArray("items");
                if (itemsArr != null) {
                        for (int i = 0; i < itemsArr.size(); i++) {
                                JsonObject itemObj = itemsArr.getJsonObject(i);
                                builder.addItems(OrderCommand.CreateOrderItemRequest.newBuilder()
                                                .setProductId(GrpcGatewayUtils.getJsonInteger(itemObj, "product_id", 0))
                                                .setQuantity(GrpcGatewayUtils.getJsonInteger(itemObj, "quantity", 0))
                                                .setPrice(GrpcGatewayUtils.getJsonInteger(itemObj, "price", 0))
                                                .build());
                        }
                }

                JsonObject shippingObj = body.getJsonObject("shipping");
                if (shippingObj != null) {
                        builder.setShipping(pb.shipping_address.ShippingAddressCommand.CreateShippingAddressRequest
                                        .newBuilder()
                                        .setAlamat(GrpcGatewayUtils.getJsonString(shippingObj, "alamat", ""))
                                        .setProvinsi(GrpcGatewayUtils.getJsonString(shippingObj, "provinsi", ""))
                                        .setKota(GrpcGatewayUtils.getJsonString(shippingObj, "kota", ""))
                                        .setCourier(GrpcGatewayUtils.getJsonString(shippingObj, "courier", ""))
                                        .setShippingMethod(GrpcGatewayUtils.getJsonString(shippingObj,
                                                        "shipping_method", ""))
                                        .setShippingCost(GrpcGatewayUtils.getJsonInteger(shippingObj, "shipping_cost",
                                                        0))
                                        .setNegara(GrpcGatewayUtils.getJsonString(shippingObj, "negara", ""))
                                        .build());
                }

                commandClient.create(builder.build())
                                .onSuccess(resp -> sendResponse(ctx, resp, 201))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void update(RoutingContext ctx) {
                if (ctx.user() == null || ctx.user().principal() == null) {
                        GrpcGatewayUtils.handleError(ctx, new Exception("Unauthorized"));
                        return;
                }

                int userId = ctx.user().principal().getInteger("userId", 0);
                int orderId = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                JsonObject body = ctx.body().asJsonObject();

                var builder = OrderCommand.UpdateOrderRequest.newBuilder()
                                .setOrderId(orderId)
                                .setUserId(userId)
                                .setTotalPrice(GrpcGatewayUtils.getJsonInteger(body, "total_price", 0));

                JsonArray itemsArr = body.getJsonArray("items");
                if (itemsArr != null) {
                        for (int i = 0; i < itemsArr.size(); i++) {
                                JsonObject itemObj = itemsArr.getJsonObject(i);
                                builder.addItems(OrderCommand.UpdateOrderItemRequest.newBuilder()
                                                .setOrderItemId(GrpcGatewayUtils.getJsonInteger(itemObj,
                                                                "order_item_id", 0))
                                                .setProductId(GrpcGatewayUtils.getJsonInteger(itemObj, "product_id", 0))
                                                .setQuantity(GrpcGatewayUtils.getJsonInteger(itemObj, "quantity", 0))
                                                .setPrice(GrpcGatewayUtils.getJsonInteger(itemObj, "price", 0))
                                                .build());
                        }
                }

                JsonObject shippingObj = body.getJsonObject("shipping");
                if (shippingObj != null) {
                        builder.setShipping(pb.shipping_address.ShippingAddressCommand.UpdateShippingAddressRequest
                                        .newBuilder()
                                        .setShippingId(GrpcGatewayUtils.getJsonInteger(shippingObj, "shipping_id", 0))
                                        .setAlamat(GrpcGatewayUtils.getJsonString(shippingObj, "alamat", ""))
                                        .setProvinsi(GrpcGatewayUtils.getJsonString(shippingObj, "provinsi", ""))
                                        .setKota(GrpcGatewayUtils.getJsonString(shippingObj, "kota", ""))
                                        .setCourier(GrpcGatewayUtils.getJsonString(shippingObj, "courier", ""))
                                        .setShippingMethod(GrpcGatewayUtils.getJsonString(shippingObj,
                                                        "shipping_method", ""))
                                        .setShippingCost(GrpcGatewayUtils.getJsonInteger(shippingObj, "shipping_cost",
                                                        0))
                                        .setNegara(GrpcGatewayUtils.getJsonString(shippingObj, "negara", ""))
                                        .build());
                }

                commandClient.update(builder.build())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void trash(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = OrderCommon.FindByIdOrderRequest.newBuilder().setId(id).build();

                commandClient.trashedOrder(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restore(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = OrderCommon.FindByIdOrderRequest.newBuilder().setId(id).build();

                commandClient.restoreOrder(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deletePermanent(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = OrderCommon.FindByIdOrderRequest.newBuilder().setId(id).build();

                commandClient.deleteOrderPermanent(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restoreAll(RoutingContext ctx) {
                commandClient.restoreAllOrder(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deleteAll(RoutingContext ctx) {
                commandClient.deleteAllOrderPermanent(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findMonthlyTotalRevenue(RoutingContext ctx) {
                var req = OrderQuery.FindYearMonthTotalRevenue.newBuilder()
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .setMonth(GrpcGatewayUtils.getQueryInt(ctx, "month", 0))
                                .build();

                statsClient.findMonthlyTotalRevenue(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findYearlyTotalRevenue(RoutingContext ctx) {
                var req = OrderQuery.FindYearTotalRevenue.newBuilder()
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .build();

                statsClient.findYearlyTotalRevenue(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findMonthlyTotalRevenueByMerchant(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = OrderQuery.FindYearMonthTotalRevenueByMerchant.newBuilder()
                                .setMerchantId(id)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .setMonth(GrpcGatewayUtils.getQueryInt(ctx, "month", 0))
                                .build();

                statsClient.findMonthlyTotalRevenueByMerchant(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findYearlyTotalRevenueByMerchant(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = OrderQuery.FindYearTotalRevenueByMerchant.newBuilder()
                                .setMerchantId(id)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .build();

                statsClient.findYearlyTotalRevenueByMerchant(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findMonthlyRevenue(RoutingContext ctx) {
                var req = OrderQuery.FindYearOrder.newBuilder()
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .build();

                statsClient.findMonthlyRevenue(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findYearlyRevenue(RoutingContext ctx) {
                var req = OrderQuery.FindYearOrder.newBuilder()
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .build();

                statsClient.findYearlyRevenue(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findMonthlyRevenueByMerchant(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = OrderQuery.FindYearOrderByMerchant.newBuilder()
                                .setMerchantId(id)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .build();

                statsClient.findMonthlyRevenueByMerchant(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findYearlyRevenueByMerchant(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = OrderQuery.FindYearOrderByMerchant.newBuilder()
                                .setMerchantId(id)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .build();

                statsClient.findYearlyRevenueByMerchant(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }
}