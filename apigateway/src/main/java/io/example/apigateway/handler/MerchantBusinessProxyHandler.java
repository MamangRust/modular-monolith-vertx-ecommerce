package io.example.apigateway.handler;

import io.example.apigateway.utils.ProtoMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pb.merchant_business.MerchantBusinessCommon;
import pb.merchant_business.MerchantBusinessCommand;
import pb.merchant_business.VertxMerchantBusinessCommandServiceGrpcClient;
import pb.merchant_business.VertxMerchantBusinessQueryServiceGrpcClient;
import pb.merchant.MerchantQuery;

public class MerchantBusinessProxyHandler {
  private final VertxMerchantBusinessQueryServiceGrpcClient queryClient;
  private final VertxMerchantBusinessCommandServiceGrpcClient commandClient;

  public MerchantBusinessProxyHandler(
      VertxMerchantBusinessQueryServiceGrpcClient queryClient,
      VertxMerchantBusinessCommandServiceGrpcClient commandClient) {
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
    var req = MerchantBusinessCommon.FindByIdMerchantBusinessRequest.newBuilder().setId(id).build();

    queryClient.findById(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void create(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantBusinessCommand.CreateMerchantBusinessRequest.newBuilder()
        .setMerchantId(body.getInteger("merchant_id", 0))
        .setBusinessType(body.getString("business_type", ""))
        .setTaxId(body.getString("tax_id", ""))
        .setEstablishedYear(body.getInteger("established_year", 0))
        .setNumberOfEmployees(body.getInteger("number_of_employees", 0))
        .setWebsiteUrl(body.getString("website_url", ""))
        .build();

    commandClient.create(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 201))
        .onFailure(ctx::fail);
  }

  public void update(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantBusinessCommand.UpdateMerchantBusinessRequest.newBuilder()
        .setMerchantBusinessInfoId(id)
        .setBusinessType(body.getString("business_type", ""))
        .setTaxId(body.getString("tax_id", ""))
        .setEstablishedYear(body.getInteger("established_year", 0))
        .setNumberOfEmployees(body.getInteger("number_of_employees", 0))
        .setWebsiteUrl(body.getString("website_url", ""))
        .build();

    commandClient.update(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void trash(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    var req = MerchantBusinessCommon.FindByIdMerchantBusinessRequest.newBuilder().setId(id).build();

    commandClient.trashedMerchantBusiness(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void restore(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    var req = MerchantBusinessCommon.FindByIdMerchantBusinessRequest.newBuilder().setId(id).build();

    commandClient.restoreMerchantBusiness(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void deletePermanent(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    var req = MerchantBusinessCommon.FindByIdMerchantBusinessRequest.newBuilder().setId(id).build();

    commandClient.deleteMerchantBusinessPermanent(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void restoreAll(RoutingContext ctx) {
    commandClient.restoreAllMerchantBusiness(com.google.protobuf.Empty.getDefaultInstance())
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void deleteAllPermanent(RoutingContext ctx) {
    commandClient.deleteAllMerchantBusinessPermanent(com.google.protobuf.Empty.getDefaultInstance())
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
