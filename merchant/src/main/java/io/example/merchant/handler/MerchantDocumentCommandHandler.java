package io.example.merchant.handler;

import com.google.protobuf.Empty;

import io.example.merchant.service.MerchantDocumentCommandService;
import io.vertx.core.Future;
import pb.merchant_document.MerchantDocumentCommand.*;
import pb.merchant_document.MerchantDocumentCommon.*;

public class MerchantDocumentCommandHandler implements pb.merchant_document.VertxMerchantDocumentCommandServiceGrpcServer.MerchantDocumentCommandServiceApi {
  private final MerchantDocumentCommandService service;

  public MerchantDocumentCommandHandler(MerchantDocumentCommandService service) {
    this.service = service;
  }

  @Override
  public Future<ApiResponseMerchantDocument> create(CreateMerchantDocumentRequest req) {
    return service.createDocument(req)
        .map(resp -> {
          var builder = ApiResponseMerchantDocument.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.fromDocumentResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantDocument> update(UpdateMerchantDocumentRequest req) {
    return service.updateDocument(req)
        .map(resp -> {
          var builder = ApiResponseMerchantDocument.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.fromDocumentResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantDocument> updateStatus(UpdateMerchantDocumentStatusRequest req) {
    return service.updateStatus(req)
        .map(resp -> {
          var builder = ApiResponseMerchantDocument.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.fromDocumentResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantDocument> trashed(TrashedMerchantDocumentRequest req) {
    return service.trashDocument(req.getDocumentId())
        .map(resp -> {
          var builder = ApiResponseMerchantDocument.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.fromDocumentResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantDocument> restore(RestoreMerchantDocumentRequest req) {
    return service.restoreDocument(req.getDocumentId())
        .map(resp -> {
          var builder = ApiResponseMerchantDocument.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.fromDocumentResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponseMerchantDocumentDelete> deletePermanent(DeleteMerchantDocumentPermanentRequest req) {
    return service.deleteDocumentPermanently(req.getDocumentId())
        .map(resp -> ApiResponseMerchantDocumentDelete.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }

  @Override
  public Future<ApiResponseMerchantDocumentAll> restoreAll(Empty req) {
    return service.restoreAllDocuments()
        .map(resp -> ApiResponseMerchantDocumentAll.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }

  @Override
  public Future<ApiResponseMerchantDocumentAll> deleteAllPermanent(Empty req) {
    return service.deleteAllPermanentDocuments()
        .map(resp -> ApiResponseMerchantDocumentAll.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .build());
  }
}
