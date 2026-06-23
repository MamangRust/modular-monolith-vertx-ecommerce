package io.example.merchant.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.merchant.service.MerchantDocumentCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.merchant_document.MerchantDocumentCommand.CreateMerchantDocumentRequest;
import pb.merchant_document.MerchantDocumentCommand.DeleteMerchantDocumentPermanentRequest;
import pb.merchant_document.MerchantDocumentCommand.RestoreMerchantDocumentRequest;
import pb.merchant_document.MerchantDocumentCommand.TrashedMerchantDocumentRequest;
import pb.merchant_document.MerchantDocumentCommand.UpdateMerchantDocumentRequest;
import pb.merchant_document.MerchantDocumentCommand.UpdateMerchantDocumentStatusRequest;
import pb.merchant_document.MerchantDocumentCommon.ApiResponseMerchantDocument;
import pb.merchant_document.MerchantDocumentCommon.ApiResponseMerchantDocumentAll;
import pb.merchant_document.MerchantDocumentCommon.ApiResponseMerchantDocumentDelete;
import pb.merchant_document.VertxMerchantDocumentCommandServiceGrpcServer.MerchantDocumentCommandServiceApi;

@RequiredArgsConstructor
public class MerchantDocumentCommandHandler implements MerchantDocumentCommandServiceApi {
  private final MerchantDocumentCommandService service;

  @Override
  public Future<ApiResponseMerchantDocument> create(CreateMerchantDocumentRequest req) {
    var domainReq = io.example.merchant.domain.requests.CreateMerchantDocumentRequest.builder()
        .merchantId(req.getMerchantId())
        .documentType(req.getDocumentType())
        .documentUrl(req.getDocumentUrl())
        .build();

    return service.createDocument(domainReq)
        .map(data -> ApiResponseMerchantDocument.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.fromDocumentResponse(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantDocument> update(UpdateMerchantDocumentRequest req) {
    var domainReq = io.example.merchant.domain.requests.UpdateMerchantDocumentRequest.builder()
        .documentId(req.getDocumentId())
        .merchantId(req.getMerchantId())
        .documentType(req.getDocumentType())
        .documentUrl(req.getDocumentUrl())
        .note(req.getNote())
        .status(req.getStatus())
        .build();

    return service.updateDocument(domainReq)
        .map(data -> ApiResponseMerchantDocument.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.fromDocumentResponse(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantDocument> updateStatus(UpdateMerchantDocumentStatusRequest req) {
    var domainReq = io.example.merchant.domain.requests.UpdateMerchantDocumentStatusRequest.builder()
        .documentId(req.getDocumentId())
        .note(req.getNote())
        .status(req.getStatus())
        .build();

    return service.updateStatus(domainReq)
        .map(data -> ApiResponseMerchantDocument.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.fromDocumentResponse(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantDocument> trashed(TrashedMerchantDocumentRequest req) {
    return service.trashDocument((long) req.getDocumentId())
        .map(data -> ApiResponseMerchantDocument.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.fromDocumentResponse(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantDocument> restore(RestoreMerchantDocumentRequest req) {
    return service.restoreDocument((long) req.getDocumentId())
        .map(data -> ApiResponseMerchantDocument.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.fromDocumentResponse(data))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantDocumentDelete> deletePermanent(DeleteMerchantDocumentPermanentRequest req) {
    return service.deleteDocumentPermanently((long) req.getDocumentId())
        .map(v -> ApiResponseMerchantDocumentDelete.newBuilder()
            .setStatus("success")
            .setMessage("Document deleted permanently")
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantDocumentAll> restoreAll(Empty req) {
    return service.restoreAllDocuments()
        .map(v -> ApiResponseMerchantDocumentAll.newBuilder()
            .setStatus("success")
            .setMessage("All documents restored successfully")
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantDocumentAll> deleteAllPermanent(Empty req) {
    return service.deleteAllPermanentDocuments()
        .map(v -> ApiResponseMerchantDocumentAll.newBuilder()
            .setStatus("success")
            .setMessage("All documents permanently deleted")
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }
}