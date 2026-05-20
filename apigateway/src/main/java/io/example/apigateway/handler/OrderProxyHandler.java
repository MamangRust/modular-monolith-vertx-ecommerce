package io.example.apigateway.handler;

import io.example.apigateway.utils.ProtoMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pb.order.OrderCommon;
import pb.order.OrderCommand;
import pb.order.OrderQuery;
import pb.order.VertxOrderQueryServiceGrpcClient;
import pb.order.VertxOrderCommandServiceGrpcClient;
import pb.order.VertxOrderStatsServiceGrpcClient;

public class OrderProxyHandler {
    private final VertxOrderQueryServiceGrpcClient queryClient;
    private final VertxOrderCommandServiceGrpcClient commandClient;
    private final VertxOrderStatsServiceGrpcClient statsClient;

    public OrderProxyHandler(
            VertxOrderQueryServiceGrpcClient queryClient,
            VertxOrderCommandServiceGrpcClient commandClient,
            VertxOrderStatsServiceGrpcClient statsClient) {
        this.queryClient = queryClient;
        this.commandClient = commandClient;
        this.statsClient = statsClient;
    }

    public void findAll(RoutingContext ctx) {
        var req = OrderQuery.FindAllOrderRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findAll(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findActive(RoutingContext ctx) {
        var req = OrderQuery.FindAllOrderRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findByActive(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findTrashed(RoutingContext ctx) {
        var req = OrderQuery.FindAllOrderRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findByTrashed(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findById(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = OrderCommon.FindByIdOrderRequest.newBuilder().setId(id).build();

        queryClient.findById(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void create(RoutingContext ctx) {
        if (ctx.user() == null || ctx.user().principal() == null) {
            ctx.response().setStatusCode(401).end("Unauthorized");
            return;
        }
        int userId = ctx.user().principal().getInteger("userId", 0);
        JsonObject body = ctx.body().asJsonObject();

        var builder = OrderCommand.CreateOrderRequest.newBuilder()
                .setMerchantId(body.getInteger("merchant_id", 0))
                .setUserId(userId)
                .setTotalPrice(body.getInteger("total_price", 0));

        JsonArray itemsArr = body.getJsonArray("items");
        if (itemsArr != null) {
            for (int i = 0; i < itemsArr.size(); i++) {
                JsonObject itemObj = itemsArr.getJsonObject(i);
                builder.addItems(OrderCommand.CreateOrderItemRequest.newBuilder()
                        .setProductId(itemObj.getInteger("product_id", 0))
                        .setQuantity(itemObj.getInteger("quantity", 0))
                        .setPrice(itemObj.getInteger("price", 0))
                        .build());
            }
        }

        JsonObject shippingObj = body.getJsonObject("shipping");
        if (shippingObj != null) {
            builder.setShipping(pb.shipping_address.ShippingAddressCommand.CreateShippingAddressRequest.newBuilder()
                    .setAlamat(shippingObj.getString("alamat", ""))
                    .setProvinsi(shippingObj.getString("provinsi", ""))
                    .setKota(shippingObj.getString("kota", ""))
                    .setCourier(shippingObj.getString("courier", ""))
                    .setShippingMethod(shippingObj.getString("shipping_method", ""))
                    .setShippingCost(shippingObj.getInteger("shipping_cost", 0))
                    .setNegara(shippingObj.getString("negara", ""))
                    .build());
        }

        commandClient.create(builder.build())
                .onSuccess(resp -> sendResponse(ctx, resp, 201))
                .onFailure(ctx::fail);
    }

    public void update(RoutingContext ctx) {
        if (ctx.user() == null || ctx.user().principal() == null) {
            ctx.response().setStatusCode(401).end("Unauthorized");
            return;
        }
        int userId = ctx.user().principal().getInteger("userId", 0);
        int orderId = Integer.parseInt(ctx.pathParam("id"));
        JsonObject body = ctx.body().asJsonObject();

        var builder = OrderCommand.UpdateOrderRequest.newBuilder()
                .setOrderId(orderId)
                .setUserId(userId)
                .setTotalPrice(body.getInteger("total_price", 0));

        JsonArray itemsArr = body.getJsonArray("items");
        if (itemsArr != null) {
            for (int i = 0; i < itemsArr.size(); i++) {
                JsonObject itemObj = itemsArr.getJsonObject(i);
                builder.addItems(OrderCommand.UpdateOrderItemRequest.newBuilder()
                        .setOrderItemId(itemObj.getInteger("order_item_id", 0))
                        .setProductId(itemObj.getInteger("product_id", 0))
                        .setQuantity(itemObj.getInteger("quantity", 0))
                        .setPrice(itemObj.getInteger("price", 0))
                        .build());
            }
        }

        JsonObject shippingObj = body.getJsonObject("shipping");
        if (shippingObj != null) {
            builder.setShipping(pb.shipping_address.ShippingAddressCommand.UpdateShippingAddressRequest.newBuilder()
                    .setShippingId(shippingObj.getInteger("shipping_id", 0))
                    .setAlamat(shippingObj.getString("alamat", ""))
                    .setProvinsi(shippingObj.getString("provinsi", ""))
                    .setKota(shippingObj.getString("kota", ""))
                    .setCourier(shippingObj.getString("courier", ""))
                    .setShippingMethod(shippingObj.getString("shipping_method", ""))
                    .setShippingCost(shippingObj.getInteger("shipping_cost", 0))
                    .setNegara(shippingObj.getString("negara", ""))
                    .build());
        }

        commandClient.update(builder.build())
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void trash(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = OrderCommon.FindByIdOrderRequest.newBuilder().setId(id).build();

        commandClient.trashedOrder(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void restore(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = OrderCommon.FindByIdOrderRequest.newBuilder().setId(id).build();

        commandClient.restoreOrder(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void deletePermanent(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = OrderCommon.FindByIdOrderRequest.newBuilder().setId(id).build();

        commandClient.deleteOrderPermanent(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void restoreAll(RoutingContext ctx) {
        commandClient.restoreAllOrder(com.google.protobuf.Empty.getDefaultInstance())
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void deleteAll(RoutingContext ctx) {
        commandClient.deleteAllOrderPermanent(com.google.protobuf.Empty.getDefaultInstance())
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    // Stats Mappings
    public void findMonthlyTotalRevenue(RoutingContext ctx) {
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;
        int month = ctx.queryParams().contains("month") ? Integer.parseInt(ctx.queryParams().get("month")) : 0;

        var req = OrderQuery.FindYearMonthTotalRevenue.newBuilder()
                .setYear(year)
                .setMonth(month)
                .build();

        statsClient.findMonthlyTotalRevenue(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findYearlyTotalRevenue(RoutingContext ctx) {
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;

        var req = OrderQuery.FindYearTotalRevenue.newBuilder()
                .setYear(year)
                .build();

        statsClient.findYearlyTotalRevenue(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findMonthlyTotalRevenueByMerchant(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;
        int month = ctx.queryParams().contains("month") ? Integer.parseInt(ctx.queryParams().get("month")) : 0;

        var req = OrderQuery.FindYearMonthTotalRevenueByMerchant.newBuilder()
                .setMerchantId(id)
                .setYear(year)
                .setMonth(month)
                .build();

        statsClient.findMonthlyTotalRevenueByMerchant(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findYearlyTotalRevenueByMerchant(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;

        var req = OrderQuery.FindYearTotalRevenueByMerchant.newBuilder()
                .setMerchantId(id)
                .setYear(year)
                .build();

        statsClient.findYearlyTotalRevenueByMerchant(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findMonthlyRevenue(RoutingContext ctx) {
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;

        var req = OrderQuery.FindYearOrder.newBuilder()
                .setYear(year)
                .build();

        statsClient.findMonthlyRevenue(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findYearlyRevenue(RoutingContext ctx) {
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;

        var req = OrderQuery.FindYearOrder.newBuilder()
                .setYear(year)
                .build();

        statsClient.findYearlyRevenue(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findMonthlyRevenueByMerchant(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;

        var req = OrderQuery.FindYearOrderByMerchant.newBuilder()
                .setMerchantId(id)
                .setYear(year)
                .build();

        statsClient.findMonthlyRevenueByMerchant(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findYearlyRevenueByMerchant(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;

        var req = OrderQuery.FindYearOrderByMerchant.newBuilder()
                .setMerchantId(id)
                .setYear(year)
                .build();

        statsClient.findYearlyRevenueByMerchant(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    private void sendResponse(RoutingContext ctx, com.google.protobuf.MessageOrBuilder proto, int defaultStatus) {
        JsonObject json = ProtoMapper.toJson(proto);
        int status = json.getInteger("status", defaultStatus);
        ctx.response()
                .setStatusCode(status == 0 ? defaultStatus : status)
                .putHeader("Content-Type", "application/json")
                .end(json.encode());
    }
}
