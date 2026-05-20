package io.example.apigateway.handler;

import io.example.apigateway.utils.ProtoMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pb.order_item.OrderItemCommon;
import pb.order_item.OrderItemQuery;
import pb.order_item.VertxOrderItemQueryServiceGrpcClient;

public class OrderItemProxyHandler {
    private final VertxOrderItemQueryServiceGrpcClient queryClient;

    public OrderItemProxyHandler(VertxOrderItemQueryServiceGrpcClient queryClient) {
        this.queryClient = queryClient;
    }

    public void findAll(RoutingContext ctx) {
        var req = OrderItemQuery.FindAllOrderItemRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findAll(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findActive(RoutingContext ctx) {
        var req = OrderItemQuery.FindAllOrderItemRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findByActive(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findTrashed(RoutingContext ctx) {
        var req = OrderItemQuery.FindAllOrderItemRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findByTrashed(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findOrderItemByOrder(RoutingContext ctx) {
        int orderId = Integer.parseInt(ctx.pathParam("orderId"));
        var req = OrderItemCommon.FindByIdOrderItemRequest.newBuilder().setId(orderId).build();

        queryClient.findOrderItemByOrder(req)
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
