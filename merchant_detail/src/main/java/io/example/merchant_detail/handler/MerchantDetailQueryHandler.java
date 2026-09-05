package io.example.merchant_detail.handler;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.merchant_detail.domain.requests.FindAllMerchantDetailRequest;
import io.example.merchant_detail.service.MerchantDetailQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.merchant.MerchantQuery.FindAllMerchantRequest;
import pb.merchant_detail.MerchantDetailCommon.ApiResponseMerchantDetail;
import pb.merchant_detail.MerchantDetailCommon.ApiResponsePaginationMerchantDetail;
import pb.merchant_detail.MerchantDetailCommon.ApiResponsePaginationMerchantDetailDeleteAt;
import pb.merchant_detail.MerchantDetailCommon.FindByIdMerchantDetailRequest;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class MerchantDetailQueryHandler
    implements pb.merchant_detail.VertxMerchantDetailQueryServiceGrpcServer.MerchantDetailQueryServiceApi {
  private final MerchantDetailQueryService service;

  private FindAllMerchantDetailRequest toDomainReq(FindAllMerchantRequest req) {
    return FindAllMerchantDetailRequest.builder()
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
  public Future<ApiResponsePaginationMerchantDetail> findAll(FindAllMerchantRequest req) {
    FindAllMerchantDetailRequest domainReq = toDomainReq(req);
    return service.getMerchantDetails(domainReq)
        .map(res -> ApiResponsePaginationMerchantDetail.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponse).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantDetail> findById(FindByIdMerchantDetailRequest req) {
    return service.getMerchantDetail((long) req.getId())
        .map(res -> ApiResponseMerchantDetail.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.toProtoResponse(res))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponsePaginationMerchantDetailDeleteAt> findByActive(FindAllMerchantRequest req) {
    FindAllMerchantDetailRequest domainReq = toDomainReq(req);
    return service.getMerchantDetailsActive(domainReq)
        .map(res -> ApiResponsePaginationMerchantDetailDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponsePaginationMerchantDetailDeleteAt> findByTrashed(FindAllMerchantRequest req) {
    FindAllMerchantDetailRequest domainReq = toDomainReq(req);
    return service.getMerchantDetailsTrashed(domainReq)
        .map(res -> ApiResponsePaginationMerchantDetailDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public pb.merchant_detail.VertxMerchantDetailQueryServiceGrpcServer.MerchantDetailQueryServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.merchant_detail.VertxMerchantDetailQueryServiceGrpcServer.FindAll, this::findAll);
    GrpcServerBinder.bind(server, pb.merchant_detail.VertxMerchantDetailQueryServiceGrpcServer.FindById, this::findById);
    GrpcServerBinder.bind(server, pb.merchant_detail.VertxMerchantDetailQueryServiceGrpcServer.FindByActive, this::findByActive);
    GrpcServerBinder.bind(server, pb.merchant_detail.VertxMerchantDetailQueryServiceGrpcServer.FindByTrashed, this::findByTrashed);
    return this;
  }
}