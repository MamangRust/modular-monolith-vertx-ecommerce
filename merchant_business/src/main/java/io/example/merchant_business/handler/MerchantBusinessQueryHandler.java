package io.example.merchant_business.handler;

import io.example.common.domain.PagedResult;
import io.example.common.grpc.GrpcExceptionMapper;
import io.example.merchant_business.domain.requests.FindAllMerchantBusinessRequest;
import io.example.merchant_business.model.MerchantBusinessResponse;
import io.example.merchant_business.model.MerchantBusinessResponseDeleteAt;
import io.example.merchant_business.service.MerchantBusinessQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.merchant.MerchantQuery.FindAllMerchantRequest;
import pb.merchant_business.MerchantBusinessCommon.ApiResponseMerchantBusiness;
import pb.merchant_business.MerchantBusinessCommon.ApiResponsePaginationMerchantBusiness;
import pb.merchant_business.MerchantBusinessCommon.ApiResponsePaginationMerchantBusinessDeleteAt;
import pb.merchant_business.MerchantBusinessCommon.FindByIdMerchantBusinessRequest;

@RequiredArgsConstructor
public class MerchantBusinessQueryHandler
    implements pb.merchant_business.VertxMerchantBusinessQueryServiceGrpcServer.MerchantBusinessQueryServiceApi {
  private final MerchantBusinessQueryService service;

  private FindAllMerchantBusinessRequest toDomainReq(FindAllMerchantRequest req) {
    return FindAllMerchantBusinessRequest.builder()
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
  public Future<ApiResponsePaginationMerchantBusiness> findAll(FindAllMerchantRequest req) {
    FindAllMerchantBusinessRequest domainReq = toDomainReq(req);
    Future<PagedResult<MerchantBusinessResponse>> businessesFuture = service.getAll(domainReq);
    return businessesFuture
        .map(res -> ApiResponsePaginationMerchantBusiness.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponse).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponseMerchantBusiness> findById(FindByIdMerchantBusinessRequest req) {
    return service.getById((long) req.getId())
        .map(res -> ApiResponseMerchantBusiness.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .setData(ProtoConverter.toProtoResponse(res))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponsePaginationMerchantBusinessDeleteAt> findByActive(FindAllMerchantRequest req) {
    FindAllMerchantBusinessRequest domainReq = toDomainReq(req);
    Future<PagedResult<MerchantBusinessResponseDeleteAt>> activeFuture = service.getActive(domainReq);
    return activeFuture
        .map(res -> ApiResponsePaginationMerchantBusinessDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }

  @Override
  public Future<ApiResponsePaginationMerchantBusinessDeleteAt> findByTrashed(FindAllMerchantRequest req) {
    FindAllMerchantBusinessRequest domainReq = toDomainReq(req);
    Future<PagedResult<MerchantBusinessResponseDeleteAt>> trashedFuture = service.getTrashed(domainReq);
    return trashedFuture
        .map(res -> ApiResponsePaginationMerchantBusinessDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("OK")
            .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList())
            .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(), domainReq.getPageSize()))
            .build())
        .recover(GrpcExceptionMapper::toFailedFuture);
  }
}