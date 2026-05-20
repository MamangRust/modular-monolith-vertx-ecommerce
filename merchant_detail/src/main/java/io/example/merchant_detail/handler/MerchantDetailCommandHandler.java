package io.example.merchant_detail.handler;

import com.google.protobuf.Empty;
import io.example.merchant_detail.service.MerchantDetailCommandService;
import io.vertx.core.Future;
import pb.merchant.MerchantCommon.ApiResponseMerchantAll;
import pb.merchant.MerchantCommon.ApiResponseMerchantDelete;
import pb.merchant_detail.MerchantDetailCommand.CreateMerchantDetailRequest;
import pb.merchant_detail.MerchantDetailCommand.UpdateMerchantDetailRequest;
import pb.merchant_detail.MerchantDetailCommon.ApiResponseMerchantDetail;
import pb.merchant_detail.MerchantDetailCommon.ApiResponseMerchantDetailDeleteAt;
import pb.merchant_detail.MerchantDetailCommon.FindByIdMerchantDetailRequest;

public class MerchantDetailCommandHandler implements pb.merchant_detail.VertxMerchantDetailCommandServiceGrpcServer.MerchantDetailCommandServiceApi {
  private final MerchantDetailCommandService service;

  public MerchantDetailCommandHandler(MerchantDetailCommandService service) {
    this.service = service;
  }

  @Override
  public Future<ApiResponseMerchantDetail> create(CreateMerchantDetailRequest req) {
    return service.create(req)
        .map(resp -> {
          var builder = ApiResponseMerchantDetail.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantDetail> update(UpdateMerchantDetailRequest req) {
    return service.update(req)
        .map(resp -> {
          var builder = ApiResponseMerchantDetail.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantDetailDeleteAt> trashedMerchantDetail(FindByIdMerchantDetailRequest req) {
    return service.trash((long) req.getId())
        .map(resp -> {
          var builder = ApiResponseMerchantDetailDeleteAt.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoResponseDeleteAt(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantDetailDeleteAt> restoreMerchantDetail(FindByIdMerchantDetailRequest req) {
    return service.restore((long) req.getId())
        .map(resp -> {
          var builder = ApiResponseMerchantDetailDeleteAt.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoResponseDeleteAt(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantDelete> deleteMerchantDetailPermanent(FindByIdMerchantDetailRequest req) {
    return service.deletePermanent((long) req.getId())
        .map(resp -> ApiResponseMerchantDelete.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }

  @Override
  public Future<ApiResponseMerchantAll> restoreAllMerchantDetail(Empty req) {
    return service.restoreAll()
        .map(resp -> ApiResponseMerchantAll.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }

  @Override
  public Future<ApiResponseMerchantAll> deleteAllMerchantDetailPermanent(Empty req) {
    return service.deleteAllPermanent()
        .map(resp -> ApiResponseMerchantAll.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }
}
