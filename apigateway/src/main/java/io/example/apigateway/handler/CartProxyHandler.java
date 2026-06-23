package io.example.apigateway.handler;

import static io.example.apigateway.utils.GrpcGatewayUtils.sendResponse;

import io.example.apigateway.utils.GrpcGatewayUtils;
import io.example.common.exception.api.UnauthorizedException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import pb.cart.CartQuery;
import pb.cart.CartCommand;
import pb.cart.VertxCartQueryServiceGrpcClient;
import pb.cart.VertxCartCommandServiceGrpcClient;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CartProxyHandler {
    private final VertxCartQueryServiceGrpcClient queryClient;
    private final VertxCartCommandServiceGrpcClient commandClient;

    public void findAll(RoutingContext ctx) {
        int userId = requireUserId(ctx);
        if (userId == -1)
            return;

        var req = CartQuery.FindAllCartRequest.newBuilder()
                .setUserId(userId)
                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                .build();

        queryClient.findAll(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
    }

    public void create(RoutingContext ctx) {
        int userId = requireUserId(ctx);
        if (userId == -1)
            return;

        JsonObject body = ctx.body().asJsonObject();

        var req = CartCommand.CreateCartRequest.newBuilder()
                .setUserId(userId)
                .setProductId(GrpcGatewayUtils.getJsonInteger(body, "product_id", 0))
                .setQuantity(GrpcGatewayUtils.getJsonInteger(body, "quantity", 1))
                .build();

        commandClient.create(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 201))
                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
    }

    public void delete(RoutingContext ctx) {
        int userId = requireUserId(ctx);
        if (userId == -1)
            return;

        int cartId = GrpcGatewayUtils.getSafePathInt(ctx, "id");

        var req = CartCommand.DeleteCartRequest.newBuilder()
                .setUserId(userId)
                .setCartId(cartId)
                .build();

        commandClient.delete(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
    }

    public void deleteAll(RoutingContext ctx) {
        int userId = requireUserId(ctx);
        if (userId == -1)
            return;

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
                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
    }

    private int requireUserId(RoutingContext ctx) {
        if (ctx.user() == null || ctx.user().principal() == null) {
            ctx.fail(new UnauthorizedException("Unauthorized"));
            return -1;
        }

        int userId = ctx.user().principal().getInteger("userId", 0);
        if (userId == 0) {
            ctx.fail(new UnauthorizedException("Invalid user token payload"));
            return -1;
        }

        return userId;
    }
}