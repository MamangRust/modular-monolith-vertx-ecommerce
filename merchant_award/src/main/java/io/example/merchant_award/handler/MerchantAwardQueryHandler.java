package io.example.merchant_award.handler;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.merchant_award.domain.requests.FindAllMerchantAwardsRequest;
import io.example.merchant_award.service.MerchantAwardQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.merchant.MerchantQuery.FindAllMerchantRequest;
import pb.merchant_award.MerchantAwardCommon.ApiResponseMerchantAward;
import pb.merchant_award.MerchantAwardCommon.ApiResponsePaginationMerchantAward;
import pb.merchant_award.MerchantAwardCommon.ApiResponsePaginationMerchantAwardDeleteAt;
import pb.merchant_award.MerchantAwardCommon.FindByIdMerchantAwardRequest;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class MerchantAwardQueryHandler
    implements pb.merchant_award.VertxMerchantAwardQueryServiceGrpcServer.MerchantAwardQueryServiceApi {
  private final MerchantAwardQueryService service;

  private FindAllMerchantAwardsRequest toDomainReq(FindAllMerchantRequest req) {
    return FindAllMerchantAwardsRequest.builder()
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
  public Future<ApiResponsePaginationMerchantAward> findAll(FindAllMerchantRequest req) {
    FindAllMerchantAwardsRequest domainReq = toDomainReq(req);
    return service.getAll(domainReq)
        .map(res -> ApiResponsePaginationMerchantAward.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponse).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantAward> findById(FindByIdMerchantAwardRequest req) {
    return service.getById((long) req.getId())
        .map(res -> ApiResponseMerchantAward.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.toProtoResponse(res))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponsePaginationMerchantAwardDeleteAt> findByActive(FindAllMerchantRequest req) {
    FindAllMerchantAwardsRequest domainReq = toDomainReq(req);
    return service.getActive(domainReq)
        .map(res -> ApiResponsePaginationMerchantAwardDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponsePaginationMerchantAwardDeleteAt> findByTrashed(FindAllMerchantRequest req) {
    FindAllMerchantAwardsRequest domainReq = toDomainReq(req);
    return service.getTrashed(domainReq)
        .map(res -> ApiResponsePaginationMerchantAwardDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public pb.merchant_award.VertxMerchantAwardQueryServiceGrpcServer.MerchantAwardQueryServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.merchant_award.VertxMerchantAwardQueryServiceGrpcServer.FindAll, this::findAll);
    GrpcServerBinder.bind(server, pb.merchant_award.VertxMerchantAwardQueryServiceGrpcServer.FindById, this::findById);
    GrpcServerBinder.bind(server, pb.merchant_award.VertxMerchantAwardQueryServiceGrpcServer.FindByActive, this::findByActive);
    GrpcServerBinder.bind(server, pb.merchant_award.VertxMerchantAwardQueryServiceGrpcServer.FindByTrashed, this::findByTrashed);
    return this;
  }
}