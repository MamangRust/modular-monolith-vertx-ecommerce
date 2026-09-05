package io.example.merchant.handler;

import io.example.common.domain.PagedResult;
import io.example.common.grpc.GrpcExceptionMapper;
import io.example.merchant.domain.requests.FindAllMerchantDocumentsRequest;
import io.example.merchant.model.MerchantDocumentResponse;
import io.example.merchant.model.MerchantDocumentResponseDeleteAt;
import io.example.merchant.service.MerchantDocumentQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.merchant_document.MerchantDocumentCommon.ApiResponseMerchantDocument;
import pb.merchant_document.MerchantDocumentCommon.ApiResponsePaginationMerchantDocument;
import pb.merchant_document.MerchantDocumentCommon.ApiResponsePaginationMerchantDocumentAt;
import pb.merchant_document.MerchantDocumentQuery.FindMerchantDocumentByIdRequest;
import pb.merchant_document.VertxMerchantDocumentQueryServiceGrpcServer.MerchantDocumentQueryServiceApi;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class MerchantDocumentQueryHandler implements MerchantDocumentQueryServiceApi {
  private final MerchantDocumentQueryService service;

  private FindAllMerchantDocumentsRequest toDomainReq(
      pb.merchant_document.MerchantDocumentQuery.FindAllMerchantDocumentsRequest req) {
    return FindAllMerchantDocumentsRequest.builder()
        .search(req.getSearch())
        .page(req.getPage() > 0 ? req.getPage() : 1)
        .pageSize(req.getPageSize() > 0 ? req.getPageSize() : 10)
        .build();
  }

  private pb.Api.PaginationMeta toMeta(int totalRecords, int page, int pageSize) {
    int currentPage = page > 0 ? page : 1;
    int size = pageSize > 0 ? pageSize : 10;
    int totalPages = size > 0 ? (int) Math.ceil((double) totalRecords / size) : 0;
    return pb.Api.PaginationMeta.newBuilder()
        .setCurrentPage(currentPage)
        .setPageSize(size)
        .setTotalPages(totalPages)
        .setTotalRecords(totalRecords)
        .build();
  }

  @Override
  public Future<ApiResponsePaginationMerchantDocument> findAll(
      pb.merchant_document.MerchantDocumentQuery.FindAllMerchantDocumentsRequest req) {
    FindAllMerchantDocumentsRequest domainReq = toDomainReq(req);
    Future<PagedResult<MerchantDocumentResponse>> documentsFuture = service.getAllDocuments(domainReq);
    return documentsFuture
        .map(res -> ApiResponsePaginationMerchantDocument.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::fromDocumentResponse).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponsePaginationMerchantDocument> findAllActive(
      pb.merchant_document.MerchantDocumentQuery.FindAllMerchantDocumentsRequest req) {
    FindAllMerchantDocumentsRequest domainReq = toDomainReq(req);
    Future<PagedResult<MerchantDocumentResponse>> activeFuture = service.getActiveDocuments(domainReq);
    return activeFuture
        .map(res -> ApiResponsePaginationMerchantDocument.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::fromDocumentResponse).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponsePaginationMerchantDocumentAt> findAllTrashed(
      pb.merchant_document.MerchantDocumentQuery.FindAllMerchantDocumentsRequest req) {
    FindAllMerchantDocumentsRequest domainReq = toDomainReq(req);
    Future<PagedResult<MerchantDocumentResponseDeleteAt>> trashedFuture = service.getTrashedDocuments(domainReq);
    return trashedFuture
        .map(res -> ApiResponsePaginationMerchantDocumentAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::fromDocumentResponseDeleteAt).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantDocument> findById(FindMerchantDocumentByIdRequest req) {
    return service.getDocumentById((long) req.getDocumentId())
        .map(res -> ApiResponseMerchantDocument.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.fromDocumentResponse(res))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public pb.merchant_document.VertxMerchantDocumentQueryServiceGrpcServer.MerchantDocumentQueryServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.merchant_document.VertxMerchantDocumentQueryServiceGrpcServer.FindAll, this::findAll);
    GrpcServerBinder.bind(server, pb.merchant_document.VertxMerchantDocumentQueryServiceGrpcServer.FindAllActive, this::findAllActive);
    GrpcServerBinder.bind(server, pb.merchant_document.VertxMerchantDocumentQueryServiceGrpcServer.FindAllTrashed, this::findAllTrashed);
    GrpcServerBinder.bind(server, pb.merchant_document.VertxMerchantDocumentQueryServiceGrpcServer.FindById, this::findById);
    return this;
  }
}