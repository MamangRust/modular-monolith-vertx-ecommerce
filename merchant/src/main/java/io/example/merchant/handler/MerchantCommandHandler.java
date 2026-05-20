package io.example.merchant.handler;

import com.google.protobuf.Empty;

import io.example.merchant.service.MerchantCommandService;
import io.vertx.core.Future;
import pb.merchant.MerchantCommand.*;
import pb.merchant.MerchantCommon.*;

public class MerchantCommandHandler implements pb.merchant.VertxMerchantCommandServiceGrpcServer.MerchantCommandServiceApi {
  private final MerchantCommandService service;

  public MerchantCommandHandler(MerchantCommandService service) {
    this.service = service;
  }

  @Override
  public Future<ApiResponseMerchant> create(CreateMerchantRequest req) {
    return service.createMerchant(req)
        .map(resp -> {
          var builder = ApiResponseMerchant.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.fromMerchantResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchant> update(UpdateMerchantRequest req) {
    return service.updateMerchant(req)
        .map(resp -> {
          var builder = ApiResponseMerchant.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.fromMerchantResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchant> updateStatus(UpdateMerchantStatusRequest req) {
    return service.updateStatus(req)
        .map(resp -> {
          var builder = ApiResponseMerchant.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.fromMerchantResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantDeleteAt> trashedMerchant(FindByIdMerchantRequest req) {
    return service.trashMerchant(req.getId())
        .map(resp -> {
          var builder = ApiResponseMerchantDeleteAt.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.fromMerchantResponseDeleteAt(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchant> restoreMerchant(FindByIdMerchantRequest req) {
    return service.restoreMerchant(req.getId())
        .map(resp -> {
          var builder = ApiResponseMerchant.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.fromMerchantResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantDelete> deleteMerchantPermanent(FindByIdMerchantRequest req) {
    return service.deleteMerchantPermanently(req.getId())
        .map(resp -> ApiResponseMerchantDelete.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }

  @Override
  public Future<ApiResponseMerchantAll> restoreAllMerchant(Empty req) {
    return service.restoreAllMerchants()
        .map(resp -> ApiResponseMerchantAll.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }

  @Override
  public Future<ApiResponseMerchantAll> deleteAllMerchantPermanent(Empty req) {
    return service.deleteAllPermanentMerchants()
        .map(resp -> ApiResponseMerchantAll.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }
}
