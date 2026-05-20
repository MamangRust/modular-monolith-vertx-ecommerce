package io.example.apigateway.handler;

import io.example.apigateway.utils.ProtoMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pb.merchant_award.MerchantAwardCommon;
import pb.merchant_award.MerchantAwardCommand;
import pb.merchant_award.VertxMerchantAwardCommandServiceGrpcClient;
import pb.merchant_award.VertxMerchantAwardQueryServiceGrpcClient;
import pb.merchant.MerchantQuery;

public class MerchantAwardProxyHandler {
  private final VertxMerchantAwardQueryServiceGrpcClient queryClient;
  private final VertxMerchantAwardCommandServiceGrpcClient commandClient;

  public MerchantAwardProxyHandler(
      VertxMerchantAwardQueryServiceGrpcClient queryClient,
      VertxMerchantAwardCommandServiceGrpcClient commandClient) {
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
    var req = MerchantAwardCommon.FindByIdMerchantAwardRequest.newBuilder().setId(id).build();

    queryClient.findById(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void create(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantAwardCommand.CreateMerchantAwardRequest.newBuilder()
        .setMerchantId(body.getInteger("merchant_id", 0))
        .setTitle(body.getString("title", ""))
        .setDescription(body.getString("description", ""))
        .setIssuedBy(body.getString("issued_by", ""))
        .setIssueDate(body.getString("issue_date", ""))
        .setExpiryDate(body.getString("expiry_date", ""))
        .setCertificateUrl(body.getString("certificate_url", ""))
        .build();

    commandClient.create(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 201))
        .onFailure(ctx::fail);
  }

  public void update(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantAwardCommand.UpdateMerchantAwardRequest.newBuilder()
        .setMerchantCertificationId(id)
        .setTitle(body.getString("title", ""))
        .setDescription(body.getString("description", ""))
        .setIssuedBy(body.getString("issued_by", ""))
        .setIssueDate(body.getString("issue_date", ""))
        .setExpiryDate(body.getString("expiry_date", ""))
        .setCertificateUrl(body.getString("certificate_url", ""))
        .build();

    commandClient.update(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void trash(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    var req = MerchantAwardCommon.FindByIdMerchantAwardRequest.newBuilder().setId(id).build();

    commandClient.trashedMerchantAward(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void restore(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    var req = MerchantAwardCommon.FindByIdMerchantAwardRequest.newBuilder().setId(id).build();

    commandClient.restoreMerchantAward(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void deletePermanent(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    var req = MerchantAwardCommon.FindByIdMerchantAwardRequest.newBuilder().setId(id).build();

    commandClient.deleteMerchantAwardPermanent(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void restoreAll(RoutingContext ctx) {
    commandClient.restoreAllMerchantAward(com.google.protobuf.Empty.getDefaultInstance())
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void deleteAllPermanent(RoutingContext ctx) {
    commandClient.deleteAllMerchantAwardPermanent(com.google.protobuf.Empty.getDefaultInstance())
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
