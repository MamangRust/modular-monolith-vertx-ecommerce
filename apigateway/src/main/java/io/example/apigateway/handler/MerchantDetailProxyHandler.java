package io.example.apigateway.handler;

import io.example.apigateway.utils.ProtoMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pb.MerchantSocialLinkCommand.CreateMerchantSocialRequest;
import pb.MerchantSocialLinkCommand.UpdateMerchantSocialRequest;
import pb.VertxMerchantSocialCommandServiceGrpcClient;
import pb.merchant.MerchantQuery;
import pb.merchant_detail.MerchantDetailCommand;
import pb.merchant_detail.MerchantDetailCommon;
import pb.merchant_detail.VertxMerchantDetailCommandServiceGrpcClient;
import pb.merchant_detail.VertxMerchantDetailQueryServiceGrpcClient;

public class MerchantDetailProxyHandler {
  private final VertxMerchantDetailQueryServiceGrpcClient queryClient;
  private final VertxMerchantDetailCommandServiceGrpcClient commandClient;
  private final VertxMerchantSocialCommandServiceGrpcClient socialClient;

  public MerchantDetailProxyHandler(
      VertxMerchantDetailQueryServiceGrpcClient queryClient,
      VertxMerchantDetailCommandServiceGrpcClient commandClient,
      VertxMerchantSocialCommandServiceGrpcClient socialClient) {
    this.queryClient = queryClient;
    this.commandClient = commandClient;
    this.socialClient = socialClient;
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
    var req = MerchantDetailCommon.FindByIdMerchantDetailRequest.newBuilder().setId(id).build();

    queryClient.findById(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void create(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantDetailCommand.CreateMerchantDetailRequest.newBuilder()
        .setMerchantId(body.getInteger("merchant_id", 0))
        .setDisplayName(body.getString("display_name", ""))
        .setCoverImageUrl(body.getString("cover_image_url", ""))
        .setLogoUrl(body.getString("logo_url", ""))
        .setShortDescription(body.getString("short_description", ""))
        .setWebsiteUrl(body.getString("website_url", ""))
        .build();

    commandClient.create(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 201))
        .onFailure(ctx::fail);
  }

  public void update(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantDetailCommand.UpdateMerchantDetailRequest.newBuilder()
        .setMerchantDetailId(id)
        .setDisplayName(body.getString("display_name", ""))
        .setCoverImageUrl(body.getString("cover_image_url", ""))
        .setLogoUrl(body.getString("logo_url", ""))
        .setShortDescription(body.getString("short_description", ""))
        .setWebsiteUrl(body.getString("website_url", ""))
        .build();

    commandClient.update(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void trash(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    var req = MerchantDetailCommon.FindByIdMerchantDetailRequest.newBuilder().setId(id).build();

    commandClient.trashedMerchantDetail(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void restore(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    var req = MerchantDetailCommon.FindByIdMerchantDetailRequest.newBuilder().setId(id).build();

    commandClient.restoreMerchantDetail(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void deletePermanent(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    var req = MerchantDetailCommon.FindByIdMerchantDetailRequest.newBuilder().setId(id).build();

    commandClient.deleteMerchantDetailPermanent(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void restoreAll(RoutingContext ctx) {
    commandClient.restoreAllMerchantDetail(com.google.protobuf.Empty.getDefaultInstance())
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  public void deleteAllPermanent(RoutingContext ctx) {
    commandClient.deleteAllMerchantDetailPermanent(com.google.protobuf.Empty.getDefaultInstance())
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(ctx::fail);
  }

  // Social Links Proxy
  public void createSocial(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    var req = CreateMerchantSocialRequest.newBuilder()
        .setMerchantDetailId(body.getInteger("merchant_detail_id", 0))
        .setPlatform(body.getString("platform", ""))
        .setUrl(body.getString("url", ""))
        .build();

    socialClient.create(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 201))
        .onFailure(ctx::fail);
  }

  public void updateSocial(RoutingContext ctx) {
    int id = Integer.parseInt(ctx.pathParam("id"));
    JsonObject body = ctx.body().asJsonObject();
    var req = UpdateMerchantSocialRequest.newBuilder()
        .setId(id)
        .setMerchantDetailId(body.getInteger("merchant_detail_id", 0))
        .setPlatform(body.getString("platform", ""))
        .setUrl(body.getString("url", ""))
        .build();

    socialClient.update(req)
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
