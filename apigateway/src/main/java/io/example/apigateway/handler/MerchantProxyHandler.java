package io.example.apigateway.handler;

import static io.example.apigateway.utils.GrpcGatewayUtils.sendResponse;

import io.example.apigateway.utils.GrpcGatewayUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pb.merchant.MerchantCommon;
import pb.merchant.MerchantQuery;
import pb.merchant.MerchantCommand;
import pb.merchant.VertxMerchantCommandServiceGrpcClient;
import pb.merchant.VertxMerchantQueryServiceGrpcClient;

@Slf4j
@RequiredArgsConstructor
public class MerchantProxyHandler {
  private final VertxMerchantQueryServiceGrpcClient queryClient;
  private final VertxMerchantCommandServiceGrpcClient commandClient;

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
    var req = MerchantCommon.FindByIdMerchantRequest.newBuilder().setId(id).build();

    queryClient.findById(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void create(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantCommand.CreateMerchantRequest.newBuilder()
        .setUserId(GrpcGatewayUtils.getJsonInteger(body, "user_id", 0))
        .setName(GrpcGatewayUtils.getJsonString(body, "name", ""))
        .setDescription(GrpcGatewayUtils.getJsonString(body, "description", ""))
        .setAddress(GrpcGatewayUtils.getJsonString(body, "address", ""))
        .setContactEmail(GrpcGatewayUtils.getJsonString(body, "contact_email", ""))
        .setContactPhone(GrpcGatewayUtils.getJsonString(body, "contact_phone", ""))
        .setStatus(GrpcGatewayUtils.getJsonString(body, "status", "ACTIVE"))
        .build();

    commandClient.create(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 201))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void update(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantCommand.UpdateMerchantRequest.newBuilder()
        .setMerchantId(id)
        .setUserId(GrpcGatewayUtils.getJsonInteger(body, "user_id", 0))
        .setName(GrpcGatewayUtils.getJsonString(body, "name", ""))
        .setDescription(GrpcGatewayUtils.getJsonString(body, "description", ""))
        .setAddress(GrpcGatewayUtils.getJsonString(body, "address", ""))
        .setContactEmail(GrpcGatewayUtils.getJsonString(body, "contact_email", ""))
        .setContactPhone(GrpcGatewayUtils.getJsonString(body, "contact_phone", ""))
        .setStatus(GrpcGatewayUtils.getJsonString(body, "status", ""))
        .build();

    commandClient.update(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void updateStatus(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    JsonObject body = ctx.body().asJsonObject();
    var req = MerchantCommand.UpdateMerchantStatusRequest.newBuilder()
        .setMerchantId(id)
        .setStatus(GrpcGatewayUtils.getJsonString(body, "status", ""))
        .build();

    commandClient.updateStatus(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void trash(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    var req = MerchantCommon.FindByIdMerchantRequest.newBuilder().setId(id).build();

    commandClient.trashedMerchant(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void restore(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    var req = MerchantCommon.FindByIdMerchantRequest.newBuilder().setId(id).build();

    commandClient.restoreMerchant(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void deletePermanent(RoutingContext ctx) {
    int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
    var req = MerchantCommon.FindByIdMerchantRequest.newBuilder().setId(id).build();

    commandClient.deleteMerchantPermanent(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void restoreAll(RoutingContext ctx) {
    commandClient.restoreAllMerchant(com.google.protobuf.Empty.getDefaultInstance())
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void deleteAllPermanent(RoutingContext ctx) {
    commandClient.deleteAllMerchantPermanent(com.google.protobuf.Empty.getDefaultInstance())
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }
}