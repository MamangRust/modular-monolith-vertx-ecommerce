package io.example.apigateway.handler;

import io.example.apigateway.utils.ProtoMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pb.merchant.MerchantCommon;
import pb.merchant.MerchantQuery;
import pb.merchant.MerchantCommand;
import pb.merchant.VertxMerchantCommandServiceGrpcClient;
import pb.merchant.VertxMerchantQueryServiceGrpcClient;

public class MerchantProxyHandler {
  private final VertxMerchantQueryServiceGrpcClient queryClient;
  private final VertxMerchantCommandServiceGrpcClient commandClient;

  public MerchantProxyHandler(
      VertxMerchantQueryServiceGrpcClient queryClient,
      VertxMerchantCommandServiceGrpcClient commandClient) {
    this.queryClient = queryClient;
    this.commandClient = commandClient;
  }

  public void findAll(RoutingContext ctx) {
    var req = MerchantQuery.FindAllMerchantRequest.newBuilder()
        .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
        .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
        .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
        .build();

    queryClient.findAll(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void findActive(RoutingContext ctx) {
    var req = MerchantQuery.FindAllMerchantRequest.newBuilder()
        .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
        .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
        .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
        .build();

    queryClient.findByActive(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void findTrashed(RoutingContext ctx) {
    var req = MerchantQuery.FindAllMerchantRequest.newBuilder()
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
    var req = MerchantCommon.FindByIdMerchantRequest.newBuilder().setId(id).build();

    queryClient.findById(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void create(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantCommand.CreateMerchantRequest.newBuilder()
        .setUserId(body.getInteger("user_id", 0))
        .setName(body.getString("name", ""))
        .setDescription(body.getString("description", ""))
        .setAddress(body.getString("address", ""))
        .setContactEmail(body.getString("contact_email", ""))
        .setContactPhone(body.getString("contact_phone", ""))
        .setStatus(body.getString("status", "ACTIVE"))
        .build();

    commandClient.create(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 201))
        .onFailure(ctx::fail);
  }

  public void update(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantCommand.UpdateMerchantRequest.newBuilder()
        .setMerchantId(id)
        .setUserId(body.getInteger("user_id", 0))
        .setName(body.getString("name", ""))
        .setDescription(body.getString("description", ""))
        .setAddress(body.getString("address", ""))
        .setContactEmail(body.getString("contact_email", ""))
        .setContactPhone(body.getString("contact_phone", ""))
        .setStatus(body.getString("status", ""))
        .build();

    commandClient.update(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void updateStatus(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantCommand.UpdateMerchantStatusRequest.newBuilder()
        .setMerchantId(id)
        .setStatus(body.getString("status", ""))
        .build();

    commandClient.updateStatus(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void trash(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    var req = MerchantCommon.FindByIdMerchantRequest.newBuilder().setId(id).build();

    commandClient.trashedMerchant(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void restore(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    var req = MerchantCommon.FindByIdMerchantRequest.newBuilder().setId(id).build();

    commandClient.restoreMerchant(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void deletePermanent(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    var req = MerchantCommon.FindByIdMerchantRequest.newBuilder().setId(id).build();

    commandClient.deleteMerchantPermanent(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void restoreAll(RoutingContext ctx) {
    commandClient.restoreAllMerchant(com.google.protobuf.Empty.getDefaultInstance())
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void deleteAllPermanent(RoutingContext ctx) {
    commandClient.deleteAllMerchantPermanent(com.google.protobuf.Empty.getDefaultInstance())
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
