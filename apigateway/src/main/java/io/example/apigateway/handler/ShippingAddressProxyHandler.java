package io.example.apigateway.handler;

import io.example.apigateway.utils.ProtoMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pb.shipping_address.ShippingAddressCommon;
import pb.shipping_address.ShippingAddressQuery;
import pb.shipping_address.ShippingAddressCommand;
import pb.shipping_address.VertxShippingQueryServiceGrpcClient;
import pb.shipping_address.VertxShippingCommandServiceGrpcClient;

public class ShippingAddressProxyHandler {
    private final VertxShippingQueryServiceGrpcClient queryClient;
    private final VertxShippingCommandServiceGrpcClient commandClient;

    public ShippingAddressProxyHandler(VertxShippingQueryServiceGrpcClient queryClient, VertxShippingCommandServiceGrpcClient commandClient) {
        this.queryClient = queryClient;
        this.commandClient = commandClient;
    }

    public void findAll(RoutingContext ctx) {
        var req = ShippingAddressQuery.FindAllShippingRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findAll(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findActive(RoutingContext ctx) {
        var req = ShippingAddressQuery.FindAllShippingRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findByActive(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findTrashed(RoutingContext ctx) {
        var req = ShippingAddressQuery.FindAllShippingRequest.newBuilder()
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
        var req = ShippingAddressCommon.FindByIdShippingRequest.newBuilder().setId(id).build();

        queryClient.findById(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findByOrder(RoutingContext ctx) {
        int orderId = Integer.parseInt(ctx.pathParam("orderId"));
        var req = ShippingAddressCommon.FindByIdShippingRequest.newBuilder().setId(orderId).build();

        queryClient.findByOrder(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void create(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        var req = ShippingAddressCommand.CreateShippingAddressRequest.newBuilder()
                .setOrderId(body.getInteger("order_id", 0))
                .setAlamat(body.getString("alamat", ""))
                .setProvinsi(body.getString("provinsi", ""))
                .setKota(body.getString("kota", ""))
                .setCourier(body.getString("courier", ""))
                .setShippingMethod(body.getString("shipping_method", ""))
                .setShippingCost(body.getInteger("shipping_cost", 0))
                .setNegara(body.getString("negara", ""))
                .build();

        commandClient.createShipping(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 201))
                .onFailure(ctx::fail);
    }

    public void update(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        JsonObject body = ctx.body().asJsonObject();
        var req = ShippingAddressCommand.UpdateShippingAddressRequest.newBuilder()
                .setShippingId(id)
                .setOrderId(body.getInteger("order_id", 0))
                .setAlamat(body.getString("alamat", ""))
                .setProvinsi(body.getString("provinsi", ""))
                .setKota(body.getString("kota", ""))
                .setCourier(body.getString("courier", ""))
                .setShippingMethod(body.getString("shipping_method", ""))
                .setShippingCost(body.getInteger("shipping_cost", 0))
                .setNegara(body.getString("negara", ""))
                .build();

        commandClient.updateShipping(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void trash(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = ShippingAddressCommon.FindByIdShippingRequest.newBuilder().setId(id).build();

        commandClient.trashedShipping(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void restore(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = ShippingAddressCommon.FindByIdShippingRequest.newBuilder().setId(id).build();

        commandClient.restoreShipping(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void deletePermanent(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = ShippingAddressCommon.FindByIdShippingRequest.newBuilder().setId(id).build();

        commandClient.deleteShippingPermanent(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void deleteByOrderPermanent(RoutingContext ctx) {
        int orderId = Integer.parseInt(ctx.pathParam("orderId"));
        var req = ShippingAddressCommon.FindByIdShippingRequest.newBuilder().setId(orderId).build();

        commandClient.deleteShippingByOrderPermanent(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void restoreAll(RoutingContext ctx) {
        commandClient.restoreAllShipping(com.google.protobuf.Empty.getDefaultInstance())
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void deleteAll(RoutingContext ctx) {
        commandClient.deleteAllShippingPermanent(com.google.protobuf.Empty.getDefaultInstance())
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
