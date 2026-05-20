package io.example.merchant.handler;

import io.example.merchant.service.MerchantDocumentQueryService;
import io.vertx.core.Future;
import pb.merchant_document.MerchantDocumentCommon.*;
import pb.merchant_document.MerchantDocumentQuery.*;

public class MerchantDocumentQueryHandler implements pb.merchant_document.VertxMerchantDocumentQueryServiceGrpcServer.MerchantDocumentQueryServiceApi {
  private final MerchantDocumentQueryService service;

  public MerchantDocumentQueryHandler(MerchantDocumentQueryService service) {
    this.service = service;
  }

  private pb.Api.PaginationMeta toMeta(io.example.common.model.PaginationMeta meta) {
    if (meta == null) {
      return pb.Api.PaginationMeta.getDefaultInstance();
    }
    return pb.Api.PaginationMeta.newBuilder()
        .setCurrentPage(meta.currentPage())
        .setPageSize(meta.pageSize())
        .setTotalPages(meta.totalPages())
        .setTotalRecords(meta.totalRecords())
        .build();
  }

  @Override
  public Future<ApiResponsePaginationMerchantDocument> findAll(FindAllMerchantDocumentsRequest req) {
    return service.getAllDocuments(req)
        .map(resp -> ApiResponsePaginationMerchantDocument.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .addAllData(resp.data().stream().map(ProtoConverter::fromDocumentResponse).toList())
            .setPagination(toMeta(resp.pagination()))
            .build());
  }

  @Override
  public Future<ApiResponsePaginationMerchantDocument> findAllActive(FindAllMerchantDocumentsRequest req) {
    return service.getActiveDocuments(req)
        .map(resp -> ApiResponsePaginationMerchantDocument.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .addAllData(resp.data().stream().map(ProtoConverter::fromDocumentResponse).toList())
            .setPagination(toMeta(resp.pagination()))
            .build());
  }

  @Override
  public Future<ApiResponsePaginationMerchantDocumentAt> findAllTrashed(FindAllMerchantDocumentsRequest req) {
    return service.getTrashedDocuments(req)
        .map(resp -> ApiResponsePaginationMerchantDocumentAt.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .addAllData(resp.data().stream().map(ProtoConverter::fromDocumentResponseDeleteAt).toList())
            .setPagination(toMeta(resp.pagination()))
            .build());
  }

  @Override
  public Future<ApiResponseMerchantDocument> findById(FindMerchantDocumentByIdRequest req) {
    return service.getDocumentById(req.getDocumentId())
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
}
