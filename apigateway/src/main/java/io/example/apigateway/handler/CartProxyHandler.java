package io.example.apigateway.handler;

import io.example.apigateway.utils.ProtoMapper;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pb.cart.CartQuery;
import pb.cart.CartCommand;
import pb.cart.VertxCartQueryServiceGrpcClient;
import pb.cart.VertxCartCommandServiceGrpcClient;

import java.util.ArrayList;
import java.util.List;

public class CartProxyHandler {
    private final VertxCartQueryServiceGrpcClient queryClient;
    private final VertxCartCommandServiceGrpcClient commandClient;

    public CartProxyHandler(VertxCartQueryServiceGrpcClient queryClient, VertxCartCommandServiceGrpcClient commandClient) {
        this.queryClient = queryClient;
        this.commandClient = commandClient;
    }

    public void findAll(RoutingContext ctx) {
        if (ctx.user() == null || ctx.user().principal() == null) {
            ctx.response().setStatusCode(401).end("Unauthorized");
            return;
        }
        int userId = ctx.user().principal().getInteger("userId", 0);

        var req = CartQuery.FindAllCartRequest.newBuilder()
                .setUserId(userId)
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findAll(req)
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

        var req = CartCommand.CreateCartRequest.newBuilder()
                .setUserId(userId)
                .setProductId(body.getInteger("product_id", 0))
                .setQuantity(body.getInteger("quantity", 1))
                .build();

        commandClient.create(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 201))
                .onFailure(ctx::fail);
    }

    public void delete(RoutingContext ctx) {
        if (ctx.user() == null || ctx.user().principal() == null) {
            ctx.response().setStatusCode(401).end("Unauthorized");
            return;
        }
        int userId = ctx.user().principal().getInteger("userId", 0);
        int cartId = Integer.parseInt(ctx.pathParam("id"));

        var req = CartCommand.DeleteCartRequest.newBuilder()
                .setUserId(userId)
                .setCartId(cartId)
                .build();

        commandClient.delete(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void deleteAll(RoutingContext ctx) {
        if (ctx.user() == null || ctx.user().principal() == null) {
            ctx.response().setStatusCode(401).end("Unauthorized");
            return;
        }
        int userId = ctx.user().principal().getInteger("userId", 0);
        JsonObject body = ctx.body().asJsonObject();
        JsonArray cartIdsArr = body.getJsonArray("cart_ids");

        List<Integer> cartIds = new ArrayList<>();
        if (cartIdsArr != null) {
            for (int i = 0; i < cartIdsArr.size(); i++) {
                cartIds.add(cartIdsArr.getInteger(i));
            }
        }

        var req = CartCommand.DeleteAllCartRequest.newBuilder()
                .setUserId(userId)
                .addAllCartIds(cartIds)
                .build();

        commandClient.deleteAll(req)
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
