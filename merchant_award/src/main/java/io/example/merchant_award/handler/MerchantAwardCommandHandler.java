package io.example.merchant_award.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.merchant_award.domain.requests.CreateMerchantAwardRequest;
import io.example.merchant_award.domain.requests.UpdateMerchantAwardRequest;
import io.example.merchant_award.service.MerchantAwardCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.merchant.MerchantCommon.ApiResponseMerchantAll;
import pb.merchant.MerchantCommon.ApiResponseMerchantDelete;
import pb.merchant_award.MerchantAwardCommon.ApiResponseMerchantAward;
import pb.merchant_award.MerchantAwardCommon.ApiResponseMerchantAwardDeleteAt;
import pb.merchant_award.MerchantAwardCommon.FindByIdMerchantAwardRequest;

@RequiredArgsConstructor
public class MerchantAwardCommandHandler
    implements pb.merchant_award.VertxMerchantAwardCommandServiceGrpcServer.MerchantAwardCommandServiceApi {
  private final MerchantAwardCommandService service;

  @Override
  public Future<ApiResponseMerchantAward> create(
      pb.merchant_award.MerchantAwardCommand.CreateMerchantAwardRequest req) {
    CreateMerchantAwardRequest domainReq = CreateMerchantAwardRequest.builder()
        .merchantId(req.getMerchantId())
        .title(req.getTitle())
        .description(req.getDescription())
        .issuedBy(req.getIssuedBy())
        .issueDate(req.getIssueDate())
        .expiryDate(req.getExpiryDate())
        .certificateUrl(req.getCertificateUrl())
        .build();

    return service.create(domainReq)
        .map(data -> ApiResponseMerchantAward.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.toProtoResponse(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantAward> update(
      pb.merchant_award.MerchantAwardCommand.UpdateMerchantAwardRequest req) {
    UpdateMerchantAwardRequest domainReq = UpdateMerchantAwardRequest.builder()
        .merchantCertificationId((long) req.getMerchantCertificationId())
        .title(req.getTitle())
        .description(req.getDescription())
        .issuedBy(req.getIssuedBy())
        .issueDate(req.getIssueDate())
        .expiryDate(req.getExpiryDate())
        .certificateUrl(req.getCertificateUrl())
        .build();

    return service.update(domainReq)
        .map(data -> ApiResponseMerchantAward.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.toProtoResponse(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantAwardDeleteAt> trashedMerchantAward(FindByIdMerchantAwardRequest req) {
    return service.trash((long) req.getId())
        .map(data -> ApiResponseMerchantAwardDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.toProtoResponseDeleteAt(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantAwardDeleteAt> restoreMerchantAward(FindByIdMerchantAwardRequest req) {
    return service.restore((long) req.getId())
        .map(data -> ApiResponseMerchantAwardDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.toProtoResponseDeleteAt(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantDelete> deleteMerchantAwardPermanent(FindByIdMerchantAwardRequest req) {
    return service.deletePermanent((long) req.getId())
        .map(v -> ApiResponseMerchantDelete.newBuilder()
            .setStatus("success")
            .setMessage("Award deleted permanently")
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantAll> restoreAllMerchantAward(Empty req) {
    return service.restoreAll()
        .map(v -> ApiResponseMerchantAll.newBuilder()
            .setStatus("success")
            .setMessage("All awards restored successfully")
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantAll> deleteAllMerchantAwardPermanent(Empty req) {
    return service.deleteAllPermanent()
        .map(v -> ApiResponseMerchantAll.newBuilder()
            .setStatus("success")
            .setMessage("All awards permanently deleted")
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }
}