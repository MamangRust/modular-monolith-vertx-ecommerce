package io.example.apigateway.handler;

import static io.example.apigateway.utils.GrpcGatewayUtils.sendResponse;

import io.example.apigateway.utils.GrpcGatewayUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pb.merchant_award.MerchantAwardCommon;
import pb.merchant_award.MerchantAwardCommand;
import pb.merchant_award.VertxMerchantAwardCommandServiceGrpcClient;
import pb.merchant_award.VertxMerchantAwardQueryServiceGrpcClient;
import pb.merchant.MerchantQuery;

@Slf4j
@RequiredArgsConstructor
public class MerchantAwardProxyHandler {
  private final VertxMerchantAwardQueryServiceGrpcClient queryClient;
  private final VertxMerchantAwardCommandServiceGrpcClient commandClient;

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
    var req = MerchantAwardCommon.FindByIdMerchantAwardRequest.newBuilder().setId(id).build();

    queryClient.findById(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void create(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();

    var req = MerchantAwardCommand.CreateMerchantAwardRequest.newBuilder()
        .setMerchantId(GrpcGatewayUtils.getJsonInteger(body, "merchant_id", 0))
        .setTitle(GrpcGatewayUtils.getJsonString(body, "title", ""))
        .setDescription(GrpcGatewayUtils.getJsonString(body, "description", ""))
        .setIssuedBy(GrpcGatewayUtils.getJsonString(body, "issued_by", ""))
        .setIssueDate(GrpcGatewayUtils.getJsonString(body, "issue_date", ""))
        .setExpiryDate(GrpcGatewayUtils.getJsonString(body, "expiry_date", ""))
        .setCertificateUrl(GrpcGatewayUtils.getJsonString(body, "certificate_url", ""))
        .build();

    commandClient.create(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 201))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void update(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    JsonObject body = ctx.body().asJsonObject();

    var req = MerchantAwardCommand.UpdateMerchantAwardRequest.newBuilder()
        .setMerchantCertificationId(id)
        .setTitle(GrpcGatewayUtils.getJsonString(body, "title", ""))
        .setDescription(GrpcGatewayUtils.getJsonString(body, "description", ""))
        .setIssuedBy(GrpcGatewayUtils.getJsonString(body, "issued_by", ""))
        .setIssueDate(GrpcGatewayUtils.getJsonString(body, "issue_date", ""))
        .setExpiryDate(GrpcGatewayUtils.getJsonString(body, "expiry_date", ""))
        .setCertificateUrl(GrpcGatewayUtils.getJsonString(body, "certificate_url", ""))
        .build();

    commandClient.update(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void trash(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    var req = MerchantAwardCommon.FindByIdMerchantAwardRequest.newBuilder().setId(id).build();

    commandClient.trashedMerchantAward(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void restore(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    var req = MerchantAwardCommon.FindByIdMerchantAwardRequest.newBuilder().setId(id).build();

    commandClient.restoreMerchantAward(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void deletePermanent(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    var req = MerchantAwardCommon.FindByIdMerchantAwardRequest.newBuilder().setId(id).build();

    commandClient.deleteMerchantAwardPermanent(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void restoreAll(RoutingContext ctx) {
    commandClient.restoreAllMerchantAward(com.google.protobuf.Empty.getDefaultInstance())
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void deleteAllPermanent(RoutingContext ctx) {
    commandClient.deleteAllMerchantAwardPermanent(com.google.protobuf.Empty.getDefaultInstance())
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }
}