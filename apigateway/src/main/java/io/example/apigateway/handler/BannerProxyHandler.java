package io.example.apigateway.handler;

import io.example.apigateway.utils.ProtoMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pb.banner.BannerCommon;
import pb.banner.BannerQuery;
import pb.banner.BannerCommand;
import pb.banner.VertxBannerQueryServiceGrpcClient;
import pb.banner.VertxBannerCommandServiceGrpcClient;

public class BannerProxyHandler {
    private final VertxBannerQueryServiceGrpcClient queryClient;
    private final VertxBannerCommandServiceGrpcClient commandClient;

    public BannerProxyHandler(VertxBannerQueryServiceGrpcClient queryClient, VertxBannerCommandServiceGrpcClient commandClient) {
        this.queryClient = queryClient;
        this.commandClient = commandClient;
    }

    public void findAll(RoutingContext ctx) {
        var req = BannerQuery.FindAllBannerRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findAll(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findActive(RoutingContext ctx) {
        var req = BannerQuery.FindAllBannerRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findByActive(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findTrashed(RoutingContext ctx) {
        var req = BannerQuery.FindAllBannerRequest.newBuilder()
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
        var req = BannerCommon.FindByIdBannerRequest.newBuilder().setId(id).build();

        queryClient.findById(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void create(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        var req = BannerCommand.CreateBannerRequest.newBuilder()
                .setName(body.getString("name", ""))
                .setStartDate(body.getString("start_date", ""))
                .setEndDate(body.getString("end_date", ""))
                .setStartTime(body.getString("start_time", ""))
                .setEndTime(body.getString("end_time", ""))
                .setIsActive(body.getBoolean("is_active", false))
                .build();

        commandClient.create(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 201))
                .onFailure(ctx::fail);
    }

    public void update(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        JsonObject body = ctx.body().asJsonObject();
        var req = BannerCommand.UpdateBannerRequest.newBuilder()
                .setBannerId(id)
                .setName(body.getString("name", ""))
                .setStartDate(body.getString("start_date", ""))
                .setEndDate(body.getString("end_date", ""))
                .setStartTime(body.getString("start_time", ""))
                .setEndTime(body.getString("end_time", ""))
                .setIsActive(body.getBoolean("is_active", false))
                .build();

        commandClient.update(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void trash(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = BannerCommon.FindByIdBannerRequest.newBuilder().setId(id).build();

        commandClient.trash(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void restore(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = BannerCommon.FindByIdBannerRequest.newBuilder().setId(id).build();

        commandClient.restore(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void deletePermanent(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = BannerCommon.FindByIdBannerRequest.newBuilder().setId(id).build();

        commandClient.deletePermanent(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void restoreAll(RoutingContext ctx) {
        commandClient.restoreAll(com.google.protobuf.Empty.getDefaultInstance())
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void deleteAll(RoutingContext ctx) {
        commandClient.deleteAll(com.google.protobuf.Empty.getDefaultInstance())
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
