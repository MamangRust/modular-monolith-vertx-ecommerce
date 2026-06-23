package io.example.apigateway.handler;

import static io.example.apigateway.utils.GrpcGatewayUtils.sendResponse;

import io.example.apigateway.utils.GrpcGatewayUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pb.merchant_business.MerchantBusinessCommon;
import pb.merchant_business.MerchantBusinessCommand;
import pb.merchant_business.VertxMerchantBusinessCommandServiceGrpcClient;
import pb.merchant_business.VertxMerchantBusinessQueryServiceGrpcClient;
import pb.merchant.MerchantQuery;

@Slf4j
@RequiredArgsConstructor
public class MerchantBusinessProxyHandler {
  private final VertxMerchantBusinessQueryServiceGrpcClient queryClient;
  private final VertxMerchantBusinessCommandServiceGrpcClient commandClient;

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
    var req = MerchantBusinessCommon.FindByIdMerchantBusinessRequest.newBuilder().setId(id).build();

    queryClient.findById(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void create(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantBusinessCommand.CreateMerchantBusinessRequest.newBuilder()
        .setMerchantId(GrpcGatewayUtils.getJsonInteger(body, "merchant_id", 0))
        .setBusinessType(GrpcGatewayUtils.getJsonString(body, "business_type", ""))
        .setTaxId(GrpcGatewayUtils.getJsonString(body, "tax_id", ""))
        .setEstablishedYear(GrpcGatewayUtils.getJsonInteger(body, "established_year", 0))
        .setNumberOfEmployees(GrpcGatewayUtils.getJsonInteger(body, "number_of_employees", 0))
        .setWebsiteUrl(GrpcGatewayUtils.getJsonString(body, "website_url", ""))
        .build();

    commandClient.create(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 201))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void update(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantBusinessCommand.UpdateMerchantBusinessRequest.newBuilder()
        .setMerchantBusinessInfoId(id)
        .setBusinessType(GrpcGatewayUtils.getJsonString(body, "business_type", ""))
        .setTaxId(GrpcGatewayUtils.getJsonString(body, "tax_id", ""))
        .setEstablishedYear(GrpcGatewayUtils.getJsonInteger(body, "established_year", 0))
        .setNumberOfEmployees(GrpcGatewayUtils.getJsonInteger(body, "number_of_employees", 0))
        .setWebsiteUrl(GrpcGatewayUtils.getJsonString(body, "website_url", ""))
        .build();

    commandClient.update(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void trash(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    var req = MerchantBusinessCommon.FindByIdMerchantBusinessRequest.newBuilder().setId(id).build();

    commandClient.trashedMerchantBusiness(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void restore(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    var req = MerchantBusinessCommon.FindByIdMerchantBusinessRequest.newBuilder().setId(id).build();

    commandClient.restoreMerchantBusiness(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void deletePermanent(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    var req = MerchantBusinessCommon.FindByIdMerchantBusinessRequest.newBuilder().setId(id).build();

    commandClient.deleteMerchantBusinessPermanent(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void restoreAll(RoutingContext ctx) {
    commandClient.restoreAllMerchantBusiness(com.google.protobuf.Empty.getDefaultInstance())
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void deleteAllPermanent(RoutingContext ctx) {
    commandClient.deleteAllMerchantBusinessPermanent(com.google.protobuf.Empty.getDefaultInstance())
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }
}