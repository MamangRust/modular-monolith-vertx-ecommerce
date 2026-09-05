package io.example.merchant.handler;

import io.example.common.domain.PagedResult;
import io.example.common.grpc.GrpcExceptionMapper;
import io.example.merchant.domain.requests.FindAllMerchantRequest;
import io.example.merchant.model.MerchantResponse;
import io.example.merchant.model.MerchantResponseDeleteAt;
import io.example.merchant.service.MerchantQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.merchant.MerchantCommon.ApiResponseMerchant;
import pb.merchant.MerchantCommon.ApiResponsePaginationMerchant;
import pb.merchant.MerchantCommon.ApiResponsePaginationMerchantDeleteAt;
import pb.merchant.MerchantCommon.FindByIdMerchantRequest;
import pb.merchant.VertxMerchantQueryServiceGrpcServer.MerchantQueryServiceApi;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class MerchantQueryHandler implements MerchantQueryServiceApi {
  private final MerchantQueryService service;

  private FindAllMerchantRequest toDomainReq(pb.merchant.MerchantQuery.FindAllMerchantRequest req) {
    return FindAllMerchantRequest.builder()
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
  public Future<ApiResponsePaginationMerchant> findAll(pb.merchant.MerchantQuery.FindAllMerchantRequest req) {
    FindAllMerchantRequest domainReq = toDomainReq(req);
    Future<PagedResult<MerchantResponse>> merchantsFuture = service.getAllMerchants(domainReq);
    return merchantsFuture
        .map(res -> ApiResponsePaginationMerchant.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::fromMerchantResponse).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchant> findById(FindByIdMerchantRequest req) {
    return service.getMerchantById((long) req.getId())
        .map(res -> ApiResponseMerchant.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.fromMerchantResponse(res))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponsePaginationMerchantDeleteAt> findByActive(
      pb.merchant.MerchantQuery.FindAllMerchantRequest req) {
    FindAllMerchantRequest domainReq = toDomainReq(req);
    Future<PagedResult<MerchantResponseDeleteAt>> activeFuture = service.getActiveMerchants(domainReq);
    return activeFuture
        .map(res -> ApiResponsePaginationMerchantDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::fromMerchantResponseDeleteAt).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponsePaginationMerchantDeleteAt> findByTrashed(
      pb.merchant.MerchantQuery.FindAllMerchantRequest req) {
    FindAllMerchantRequest domainReq = toDomainReq(req);
    Future<PagedResult<MerchantResponseDeleteAt>> trashedFuture = service.getTrashedMerchants(domainReq);
    return trashedFuture
        .map(res -> ApiResponsePaginationMerchantDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::fromMerchantResponseDeleteAt).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public pb.merchant.VertxMerchantQueryServiceGrpcServer.MerchantQueryServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.merchant.VertxMerchantQueryServiceGrpcServer.FindAll, this::findAll);
    GrpcServerBinder.bind(server, pb.merchant.VertxMerchantQueryServiceGrpcServer.FindById, this::findById);
    GrpcServerBinder.bind(server, pb.merchant.VertxMerchantQueryServiceGrpcServer.FindByActive, this::findByActive);
    GrpcServerBinder.bind(server, pb.merchant.VertxMerchantQueryServiceGrpcServer.FindByTrashed, this::findByTrashed);
    return this;
  }
}