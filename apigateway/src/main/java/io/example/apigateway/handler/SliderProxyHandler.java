package io.example.apigateway.handler;

import io.example.apigateway.utils.ProtoMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pb.slider.SliderCommon;
import pb.slider.SliderQuery;
import pb.slider.SliderCommand;
import pb.slider.VertxSliderQueryServiceGrpcClient;
import pb.slider.VertxSliderCommandServiceGrpcClient;

public class SliderProxyHandler {
    private final VertxSliderQueryServiceGrpcClient queryClient;
    private final VertxSliderCommandServiceGrpcClient commandClient;

    public SliderProxyHandler(VertxSliderQueryServiceGrpcClient queryClient, VertxSliderCommandServiceGrpcClient commandClient) {
        this.queryClient = queryClient;
        this.commandClient = commandClient;
    }

    public void findAll(RoutingContext ctx) {
        var req = SliderQuery.FindAllSliderRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findAll(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findActive(RoutingContext ctx) {
        var req = SliderQuery.FindAllSliderRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findByActive(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findTrashed(RoutingContext ctx) {
        var req = SliderQuery.FindAllSliderRequest.newBuilder()
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
        var req = SliderCommon.FindByIdSliderRequest.newBuilder().setId(id).build();

        queryClient.findById(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void create(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        var req = SliderCommand.CreateSliderRequest.newBuilder()
                .setName(body.getString("name", ""))
                .setImage(body.getString("image", ""))
                .build();

        commandClient.create(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 201))
                .onFailure(ctx::fail);
    }

    public void update(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        JsonObject body = ctx.body().asJsonObject();
        var req = SliderCommand.UpdateSliderRequest.newBuilder()
                .setId(id)
                .setName(body.getString("name", ""))
                .setImage(body.getString("image", ""))
                .build();

        commandClient.update(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void trash(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = SliderCommon.FindByIdSliderRequest.newBuilder().setId(id).build();

        commandClient.trashedSlider(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void restore(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = SliderCommon.FindByIdSliderRequest.newBuilder().setId(id).build();

        commandClient.restoreSlider(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void deletePermanent(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = SliderCommon.FindByIdSliderRequest.newBuilder().setId(id).build();

        commandClient.deleteSliderPermanent(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void restoreAll(RoutingContext ctx) {
        commandClient.restoreAllSlider(com.google.protobuf.Empty.getDefaultInstance())
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void deleteAll(RoutingContext ctx) {
        commandClient.deleteAllSliderPermanent(com.google.protobuf.Empty.getDefaultInstance())
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
