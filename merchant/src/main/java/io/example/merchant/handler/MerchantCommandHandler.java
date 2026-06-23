package io.example.merchant.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.merchant.service.MerchantCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.merchant.MerchantCommand.CreateMerchantRequest;
import pb.merchant.MerchantCommand.UpdateMerchantRequest;
import pb.merchant.MerchantCommand.UpdateMerchantStatusRequest;
import pb.merchant.MerchantCommon.ApiResponseMerchant;
import pb.merchant.MerchantCommon.ApiResponseMerchantAll;
import pb.merchant.MerchantCommon.ApiResponseMerchantDelete;
import pb.merchant.MerchantCommon.ApiResponseMerchantDeleteAt;
import pb.merchant.MerchantCommon.FindByIdMerchantRequest;

@RequiredArgsConstructor
public class MerchantCommandHandler
        implements pb.merchant.VertxMerchantCommandServiceGrpcServer.MerchantCommandServiceApi {
    private final MerchantCommandService service;

    @Override
    public Future<ApiResponseMerchant> create(CreateMerchantRequest req) {
        var domainReq = io.example.merchant.domain.requests.CreateMerchantRequest.builder()
                .userId(req.getUserId())
                .name(req.getName())
                .status(req.getStatus())
                .build();

        return service.createMerchant(domainReq)
                .map(data -> ApiResponseMerchant.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.fromMerchantResponse(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchant> update(UpdateMerchantRequest req) {
        var domainReq = io.example.merchant.domain.requests.UpdateMerchantRequest.builder()
                .merchantId(req.getMerchantId())
                .name(req.getName())
                .status(req.getStatus())
                .build();

        return service.updateMerchant(domainReq)
                .map(data -> ApiResponseMerchant.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.fromMerchantResponse(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchant> updateStatus(UpdateMerchantStatusRequest req) {
        var domainReq = io.example.merchant.domain.requests.UpdateMerchantStatusRequest.builder()
                .merchantId(req.getMerchantId())
                .status(req.getStatus())
                .build();

        return service.updateStatus(domainReq)
                .map(data -> ApiResponseMerchant.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.fromMerchantResponse(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantDeleteAt> trashedMerchant(FindByIdMerchantRequest req) {
        return service.trashMerchant((long) req.getId())
                .map(data -> ApiResponseMerchantDeleteAt.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.fromMerchantResponseDeleteAt(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchant> restoreMerchant(FindByIdMerchantRequest req) {
        return service.restoreMerchant((long) req.getId())
                .map(data -> ApiResponseMerchant.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.fromMerchantResponse(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantDelete> deleteMerchantPermanent(FindByIdMerchantRequest req) {
        return service.deleteMerchantPermanently((long) req.getId())
                .map(v -> ApiResponseMerchantDelete.newBuilder()
                        .setStatus("success")
                        .setMessage("Merchant deleted permanently")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantAll> restoreAllMerchant(Empty req) {
        return service.restoreAllMerchants()
                .map(v -> ApiResponseMerchantAll.newBuilder()
                        .setStatus("success")
                        .setMessage("All merchants restored successfully")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseMerchantAll> deleteAllMerchantPermanent(Empty req) {
        return service.deleteAllPermanentMerchants()
                .map(v -> ApiResponseMerchantAll.newBuilder()
                        .setStatus("success")
                        .setMessage("All merchants permanently deleted")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }
}