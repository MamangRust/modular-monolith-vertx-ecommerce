package io.example.merchant_business.handler;

import com.google.protobuf.Empty;

import io.example.merchant_business.service.MerchantBusinessCommandService;
import io.vertx.core.Future;
import pb.merchant.MerchantCommon.ApiResponseMerchantAll;
import pb.merchant.MerchantCommon.ApiResponseMerchantDelete;
import pb.merchant_business.MerchantBusinessCommand.CreateMerchantBusinessRequest;
import pb.merchant_business.MerchantBusinessCommand.UpdateMerchantBusinessRequest;
import pb.merchant_business.MerchantBusinessCommon.ApiResponseMerchantBusiness;
import pb.merchant_business.MerchantBusinessCommon.ApiResponseMerchantBusinessDeleteAt;
import pb.merchant_business.MerchantBusinessCommon.FindByIdMerchantBusinessRequest;

public class MerchantBusinessCommandHandler implements pb.merchant_business.VertxMerchantBusinessCommandServiceGrpcServer.MerchantBusinessCommandServiceApi {
  private final MerchantBusinessCommandService service;

  public MerchantBusinessCommandHandler(MerchantBusinessCommandService service) {
    this.service = service;
  }

  @Override
  public Future<ApiResponseMerchantBusiness> create(CreateMerchantBusinessRequest req) {
    return service.create(req)
        .map(resp -> {
          var builder = ApiResponseMerchantBusiness.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantBusiness> update(UpdateMerchantBusinessRequest req) {
    return service.update(req)
        .map(resp -> {
          var builder = ApiResponseMerchantBusiness.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantBusinessDeleteAt> trashedMerchantBusiness(FindByIdMerchantBusinessRequest req) {
    return service.trash((long) req.getId())
        .map(resp -> {
          var builder = ApiResponseMerchantBusinessDeleteAt.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoResponseDeleteAt(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantBusinessDeleteAt> restoreMerchantBusiness(FindByIdMerchantBusinessRequest req) {
    return service.restore((long) req.getId())
        .map(resp -> {
          var builder = ApiResponseMerchantBusinessDeleteAt.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoResponseDeleteAt(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantDelete> deleteMerchantBusinessPermanent(FindByIdMerchantBusinessRequest req) {
    return service.deletePermanent((long) req.getId())
        .map(resp -> ApiResponseMerchantDelete.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }

  @Override
  public Future<ApiResponseMerchantAll> restoreAllMerchantBusiness(Empty req) {
    return service.restoreAll()
        .map(resp -> ApiResponseMerchantAll.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }

  @Override
  public Future<ApiResponseMerchantAll> deleteAllMerchantBusinessPermanent(Empty req) {
    return service.deleteAllPermanent()
        .map(resp -> ApiResponseMerchantAll.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }
}
