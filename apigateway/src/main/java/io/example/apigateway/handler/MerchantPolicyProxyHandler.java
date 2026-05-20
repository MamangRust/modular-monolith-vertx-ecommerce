package io.example.apigateway.handler;

import io.example.apigateway.utils.ProtoMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pb.merchant_policy.MerchantPolicyCommon;
import pb.merchant_policy.MerchantPolicyCommand;
import pb.merchant_policy.VertxMerchantPolicyCommandServiceGrpcClient;
import pb.merchant_policy.VertxMerchantPolicyQueryServiceGrpcClient;
import pb.merchant.MerchantQuery;

public class MerchantPolicyProxyHandler {
  private final VertxMerchantPolicyQueryServiceGrpcClient queryClient;
  private final VertxMerchantPolicyCommandServiceGrpcClient commandClient;

  public MerchantPolicyProxyHandler(
      VertxMerchantPolicyQueryServiceGrpcClient queryClient,
      VertxMerchantPolicyCommandServiceGrpcClient commandClient) {
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
    var req = MerchantPolicyCommon.FindByIdMerchantPoliciesRequest.newBuilder().setId(id).build();

    queryClient.findById(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void create(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantPolicyCommand.CreateMerchantPoliciesRequest.newBuilder()
        .setMerchantId(body.getInteger("merchant_id", 0))
        .setPolicyType(body.getString("policy_type", ""))
        .setTitle(body.getString("title", ""))
        .setDescription(body.getString("description", ""))
        .build();

    commandClient.create(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 201))
        .onFailure(ctx::fail);
  }

  public void update(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantPolicyCommand.UpdateMerchantPoliciesRequest.newBuilder()
        .setMerchantPolicyId(id)
        .setPolicyType(body.getString("policy_type", ""))
        .setTitle(body.getString("title", ""))
        .setDescription(body.getString("description", ""))
        .build();

    commandClient.update(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void trash(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    var req = MerchantPolicyCommon.FindByIdMerchantPoliciesRequest.newBuilder().setId(id).build();

    commandClient.trashedMerchantPolicies(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void restore(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    var req = MerchantPolicyCommon.FindByIdMerchantPoliciesRequest.newBuilder().setId(id).build();

    commandClient.restoreMerchantPolicies(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void deletePermanent(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    var req = MerchantPolicyCommon.FindByIdMerchantPoliciesRequest.newBuilder().setId(id).build();

    commandClient.deleteMerchantPoliciesPermanent(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void restoreAll(RoutingContext ctx) {
    commandClient.restoreAllMerchantPolicies(com.google.protobuf.Empty.getDefaultInstance())
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void deleteAllPermanent(RoutingContext ctx) {
    commandClient.deleteAllMerchantPoliciesPermanent(com.google.protobuf.Empty.getDefaultInstance())
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
