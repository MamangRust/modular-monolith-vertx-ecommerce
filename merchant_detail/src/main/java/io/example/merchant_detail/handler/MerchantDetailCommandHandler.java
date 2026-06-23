package io.example.merchant_detail.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.merchant_detail.domain.requests.CreateMerchantDetailRequest;
import io.example.merchant_detail.domain.requests.UpdateMerchantDetailRequest;
import io.example.merchant_detail.service.MerchantDetailCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.merchant.MerchantCommon.ApiResponseMerchantAll;
import pb.merchant.MerchantCommon.ApiResponseMerchantDelete;
import pb.merchant_detail.MerchantDetailCommon.ApiResponseMerchantDetail;
import pb.merchant_detail.MerchantDetailCommon.ApiResponseMerchantDetailDeleteAt;
import pb.merchant_detail.MerchantDetailCommon.FindByIdMerchantDetailRequest;

@RequiredArgsConstructor
public class MerchantDetailCommandHandler
        implements pb.merchant_detail.VertxMerchantDetailCommandServiceGrpcServer.MerchantDetailCommandServiceApi {
    private final MerchantDetailCommandService service;

    @Override
    public Future<ApiResponseMerchantDetail> create(
            pb.merchant_detail.MerchantDetailCommand.CreateMerchantDetailRequest req) {
        CreateMerchantDetailRequest domainReq = CreateMerchantDetailRequest.builder()
                .merchantId(req.getMerchantId())
                .displayName(req.getDisplayName())
                .coverImageUrl(req.getCoverImageUrl())
                .logoUrl(req.getLogoUrl())
                .shortDescription(req.getShortDescription())
                .websiteUrl(req.getWebsiteUrl())
                .build();

        return service.create(domainReq)
                .map(data -> ApiResponseMerchantDetail.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toProtoResponse(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantDetail> update(
            pb.merchant_detail.MerchantDetailCommand.UpdateMerchantDetailRequest req) {
        UpdateMerchantDetailRequest domainReq = UpdateMerchantDetailRequest.builder()
                .merchantDetailId(req.getMerchantDetailId())
                .displayName(req.getDisplayName())
                .coverImageUrl(req.getCoverImageUrl())
                .logoUrl(req.getLogoUrl())
                .shortDescription(req.getShortDescription())
                .websiteUrl(req.getWebsiteUrl())
                .build();

        return service.update(domainReq)
                .map(data -> ApiResponseMerchantDetail.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toProtoResponse(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantDetailDeleteAt> trashedMerchantDetail(FindByIdMerchantDetailRequest req) {
        return service.trash((long) req.getId())
                .map(data -> ApiResponseMerchantDetailDeleteAt.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toProtoResponseDeleteAt(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantDetailDeleteAt> restoreMerchantDetail(FindByIdMerchantDetailRequest req) {
        return service.restore((long) req.getId())
                .map(data -> ApiResponseMerchantDetailDeleteAt.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toProtoResponseDeleteAt(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantDelete> deleteMerchantDetailPermanent(FindByIdMerchantDetailRequest req) {
        return service.deletePermanent((long) req.getId())
                .map(v -> ApiResponseMerchantDelete.newBuilder()
                        .setStatus("success")
                        .setMessage("Merchant detail deleted permanently")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantAll> restoreAllMerchantDetail(Empty req) {
        return service.restoreAll()
                .map(v -> ApiResponseMerchantAll.newBuilder()
                        .setStatus("success")
                        .setMessage("All merchant details restored successfully")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantAll> deleteAllMerchantDetailPermanent(Empty req) {
        return service.deleteAllPermanent()
                .map(v -> ApiResponseMerchantAll.newBuilder()
                        .setStatus("success")
                        .setMessage("All merchant details permanently deleted")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }
}