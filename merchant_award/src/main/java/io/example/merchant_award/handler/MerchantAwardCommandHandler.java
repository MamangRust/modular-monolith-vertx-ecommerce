package io.example.merchant_award.handler;

import com.google.protobuf.Empty;

import io.example.merchant_award.service.MerchantAwardCommandService;
import io.vertx.core.Future;
import pb.merchant.MerchantCommon.ApiResponseMerchantAll;
import pb.merchant.MerchantCommon.ApiResponseMerchantDelete;
import pb.merchant_award.MerchantAwardCommand.CreateMerchantAwardRequest;
import pb.merchant_award.MerchantAwardCommand.UpdateMerchantAwardRequest;
import pb.merchant_award.MerchantAwardCommon.ApiResponseMerchantAward;
import pb.merchant_award.MerchantAwardCommon.ApiResponseMerchantAwardDeleteAt;
import pb.merchant_award.MerchantAwardCommon.FindByIdMerchantAwardRequest;

public class MerchantAwardCommandHandler implements pb.merchant_award.VertxMerchantAwardCommandServiceGrpcServer.MerchantAwardCommandServiceApi {
  private final MerchantAwardCommandService service;

  public MerchantAwardCommandHandler(MerchantAwardCommandService service) {
    this.service = service;
  }

  @Override
  public Future<ApiResponseMerchantAward> create(CreateMerchantAwardRequest req) {
    return service.create(req)
        .map(resp -> {
          var builder = ApiResponseMerchantAward.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantAward> update(UpdateMerchantAwardRequest req) {
    return service.update(req)
        .map(resp -> {
          var builder = ApiResponseMerchantAward.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantAwardDeleteAt> trashedMerchantAward(FindByIdMerchantAwardRequest req) {
    return service.trash((long) req.getId())
        .map(resp -> {
          var builder = ApiResponseMerchantAwardDeleteAt.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoResponseDeleteAt(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantAwardDeleteAt> restoreMerchantAward(FindByIdMerchantAwardRequest req) {
    return service.restore((long) req.getId())
        .map(resp -> {
          var builder = ApiResponseMerchantAwardDeleteAt.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoResponseDeleteAt(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantDelete> deleteMerchantAwardPermanent(FindByIdMerchantAwardRequest req) {
    return service.deletePermanent((long) req.getId())
        .map(resp -> ApiResponseMerchantDelete.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }

  @Override
  public Future<ApiResponseMerchantAll> restoreAllMerchantAward(Empty req) {
    return service.restoreAll()
        .map(resp -> ApiResponseMerchantAll.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }

  @Override
  public Future<ApiResponseMerchantAll> deleteAllMerchantAwardPermanent(Empty req) {
    return service.deleteAllPermanent()
        .map(resp -> ApiResponseMerchantAll.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }
}
