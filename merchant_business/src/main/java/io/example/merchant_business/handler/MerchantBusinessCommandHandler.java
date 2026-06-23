package io.example.merchant_business.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.merchant_business.service.MerchantBusinessCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.merchant.MerchantCommon.ApiResponseMerchantAll;
import pb.merchant.MerchantCommon.ApiResponseMerchantDelete;
import pb.merchant_business.MerchantBusinessCommand.CreateMerchantBusinessRequest;
import pb.merchant_business.MerchantBusinessCommand.UpdateMerchantBusinessRequest;
import pb.merchant_business.MerchantBusinessCommon.ApiResponseMerchantBusiness;
import pb.merchant_business.MerchantBusinessCommon.ApiResponseMerchantBusinessDeleteAt;
import pb.merchant_business.MerchantBusinessCommon.FindByIdMerchantBusinessRequest;

@RequiredArgsConstructor
public class MerchantBusinessCommandHandler
    implements pb.merchant_business.VertxMerchantBusinessCommandServiceGrpcServer.MerchantBusinessCommandServiceApi {
  private final MerchantBusinessCommandService service;

  @Override
  public Future<ApiResponseMerchantBusiness> create(CreateMerchantBusinessRequest req) {
    var domainReq = io.example.merchant_business.domain.requests.CreateMerchantBusinessRequest.builder()
        .merchantId(req.getMerchantId())
        .businessType(req.getBusinessType())
        .taxId(req.getTaxId())
        .establishedYear(req.getEstablishedYear())
        .numberOfEmployees(req.getNumberOfEmployees())
        .websiteUrl(req.getWebsiteUrl())
        .build();

    return service.create(domainReq)
        .map(data -> ApiResponseMerchantBusiness.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.toProtoResponse(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantBusiness> update(UpdateMerchantBusinessRequest req) {
    var domainReq = io.example.merchant_business.domain.requests.UpdateMerchantBusinessRequest.builder()
        .merchantBusinessInfoId(req.getMerchantBusinessInfoId())
        .businessType(req.getBusinessType())
        .taxId(req.getTaxId())
        .establishedYear(req.getEstablishedYear())
        .numberOfEmployees(req.getNumberOfEmployees())
        .websiteUrl(req.getWebsiteUrl())
        .build();

    return service.update(domainReq)
        .map(data -> ApiResponseMerchantBusiness.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.toProtoResponse(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantBusinessDeleteAt> trashedMerchantBusiness(FindByIdMerchantBusinessRequest req) {
    return service.trash((long) req.getId())
        .map(data -> ApiResponseMerchantBusinessDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.toProtoResponseDeleteAt(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantBusinessDeleteAt> restoreMerchantBusiness(FindByIdMerchantBusinessRequest req) {
    return service.restore((long) req.getId())
        .map(data -> ApiResponseMerchantBusinessDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.toProtoResponseDeleteAt(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantDelete> deleteMerchantBusinessPermanent(FindByIdMerchantBusinessRequest req) {
    return service.deletePermanent((long) req.getId())
        .map(v -> ApiResponseMerchantDelete.newBuilder()
            .setStatus("success")
            .setMessage("Merchant business info deleted permanently")
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantAll> restoreAllMerchantBusiness(Empty req) {
    return service.restoreAll()
        .map(v -> ApiResponseMerchantAll.newBuilder()
            .setStatus("success")
            .setMessage("All merchant business info restored successfully")
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantAll> deleteAllMerchantBusinessPermanent(Empty req) {
    return service.deleteAllPermanent()
        .map(v -> ApiResponseMerchantAll.newBuilder()
            .setStatus("success")
            .setMessage("All merchant business info permanently deleted")
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }
}