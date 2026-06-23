package io.example.apigateway.handler;

import static io.example.apigateway.utils.GrpcGatewayUtils.sendResponse;

import io.example.apigateway.utils.GrpcGatewayUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pb.MerchantSocialLinkCommand.CreateMerchantSocialRequest;
import pb.MerchantSocialLinkCommand.UpdateMerchantSocialRequest;
import pb.VertxMerchantSocialCommandServiceGrpcClient;
import pb.merchant.MerchantQuery;
import pb.merchant_detail.MerchantDetailCommand;
import pb.merchant_detail.MerchantDetailCommon;
import pb.merchant_detail.VertxMerchantDetailCommandServiceGrpcClient;
import pb.merchant_detail.VertxMerchantDetailQueryServiceGrpcClient;

@Slf4j
@RequiredArgsConstructor
public class MerchantDetailProxyHandler {
  private final VertxMerchantDetailQueryServiceGrpcClient queryClient;
  private final VertxMerchantDetailCommandServiceGrpcClient commandClient;
  private final VertxMerchantSocialCommandServiceGrpcClient socialClient;

  public void findAll(RoutingContext ctx) {
    var req = MerchantQuery.FindAllMerchantRequest.newBuilder()
        .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
        .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
        .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
        .build();

    queryClient.findAll(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void findActive(RoutingContext ctx) {
    var req = MerchantQuery.FindAllMerchantRequest.newBuilder()
        .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
        .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
        .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
        .build();

    queryClient.findByActive(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void findTrashed(RoutingContext ctx) {
    var req = MerchantQuery.FindAllMerchantRequest.newBuilder()
        .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
        .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
        .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
        .build();

    queryClient.findByTrashed(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void findById(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    var req = MerchantDetailCommon.FindByIdMerchantDetailRequest.newBuilder().setId(id).build();

    queryClient.findById(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void create(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantDetailCommand.CreateMerchantDetailRequest.newBuilder()
        .setMerchantId(GrpcGatewayUtils.getJsonInteger(body, "merchant_id", 0))
        .setDisplayName(GrpcGatewayUtils.getJsonString(body, "display_name", ""))
        .setCoverImageUrl(GrpcGatewayUtils.getJsonString(body, "cover_image_url", ""))
        .setLogoUrl(GrpcGatewayUtils.getJsonString(body, "logo_url", ""))
        .setShortDescription(GrpcGatewayUtils.getJsonString(body, "short_description", ""))
        .setWebsiteUrl(GrpcGatewayUtils.getJsonString(body, "website_url", ""))
        .build();

    commandClient.create(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 201))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void update(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantDetailCommand.UpdateMerchantDetailRequest.newBuilder()
        .setMerchantDetailId(id)
        .setDisplayName(GrpcGatewayUtils.getJsonString(body, "display_name", ""))
        .setCoverImageUrl(GrpcGatewayUtils.getJsonString(body, "cover_image_url", ""))
        .setLogoUrl(GrpcGatewayUtils.getJsonString(body, "logo_url", ""))
        .setShortDescription(GrpcGatewayUtils.getJsonString(body, "short_description", ""))
        .setWebsiteUrl(GrpcGatewayUtils.getJsonString(body, "website_url", ""))
        .build();

    commandClient.update(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void createSocial(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    var req = CreateMerchantSocialRequest.newBuilder()
        .setMerchantDetailId(GrpcGatewayUtils.getJsonInteger(body, "merchant_detail_id", 0))
        .setPlatform(GrpcGatewayUtils.getJsonString(body, "platform", ""))
        .setUrl(GrpcGatewayUtils.getJsonString(body, "url", ""))
        .build();

    socialClient.create(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 201))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void updateSocial(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    JsonObject body = ctx.body().asJsonObject();
    var req = UpdateMerchantSocialRequest.newBuilder()
        .setId(id)
        .setMerchantDetailId(GrpcGatewayUtils.getJsonInteger(body, "merchant_detail_id", 0))
        .setPlatform(GrpcGatewayUtils.getJsonString(body, "platform", ""))
        .setUrl(GrpcGatewayUtils.getJsonString(body, "url", ""))
        .build();

    socialClient.update(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void trash(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    var req = MerchantDetailCommon.FindByIdMerchantDetailRequest.newBuilder().setId(id).build();

    commandClient.trashedMerchantDetail(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void restore(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    var req = MerchantDetailCommon.FindByIdMerchantDetailRequest.newBuilder().setId(id).build();

    commandClient.restoreMerchantDetail(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void deletePermanent(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    var req = MerchantDetailCommon.FindByIdMerchantDetailRequest.newBuilder().setId(id).build();

    commandClient.deleteMerchantDetailPermanent(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void restoreAll(RoutingContext ctx) {
    commandClient.restoreAllMerchantDetail(com.google.protobuf.Empty.getDefaultInstance())
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void deleteAllPermanent(RoutingContext ctx) {
    commandClient.deleteAllMerchantDetailPermanent(com.google.protobuf.Empty.getDefaultInstance())
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }
}