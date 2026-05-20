package io.example.merchant_detail.handler;

import io.example.merchant_detail.service.MerchantDetailQueryService;
import io.vertx.core.Future;
import pb.merchant.MerchantQuery.FindAllMerchantRequest;
import pb.merchant_detail.MerchantDetailCommon.ApiResponseMerchantDetail;
import pb.merchant_detail.MerchantDetailCommon.ApiResponsePaginationMerchantDetail;
import pb.merchant_detail.MerchantDetailCommon.ApiResponsePaginationMerchantDetailDeleteAt;
import pb.merchant_detail.MerchantDetailCommon.FindByIdMerchantDetailRequest;

public class MerchantDetailQueryHandler implements pb.merchant_detail.VertxMerchantDetailQueryServiceGrpcServer.MerchantDetailQueryServiceApi {
  private final MerchantDetailQueryService service;

  public MerchantDetailQueryHandler(MerchantDetailQueryService service) {
    this.service = service;
  }

  private pb.Api.PaginationMeta buildMeta(int page, int pageSize, int totalRecords) {
    int totalPages = (int) Math.ceil((double) totalRecords / (pageSize > 0 ? pageSize : 10));
    return pb.Api.PaginationMeta.newBuilder()
        .setCurrentPage(page > 0 ? page : 1)
        .setPageSize(pageSize > 0 ? pageSize : 10)
        .setTotalPages(totalPages)
        .setTotalRecords(totalRecords)
        .build();
  }

  @Override
  public Future<ApiResponsePaginationMerchantDetail> findAll(FindAllMerchantRequest req) {
    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;

    return service.getMerchantDetails(req.getSearch(), page, pageSize)
        .map(result -> ApiResponsePaginationMerchantDetail.newBuilder()
            .setStatus("success")
            .setMessage("Data fetched successfully")
            .addAllData(result.getData().stream().map(ProtoConverter::toProtoResponse).toList())
            .setPagination(buildMeta(page, pageSize, result.getTotalRecords()))
            .build());
  }

  @Override
  public Future<ApiResponseMerchantDetail> findById(FindByIdMerchantDetailRequest req) {
    return service.getMerchantDetail((long) req.getId())
        .map(resp -> {
          var builder = ApiResponseMerchantDetail.newBuilder()
              .setStatus("success")
              .setMessage("Data fetched successfully");
          if (resp != null) {
            builder.setData(ProtoConverter.toProtoResponse(resp));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponsePaginationMerchantDetailDeleteAt> findByActive(FindAllMerchantRequest req) {
    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;

    return service.getMerchantDetailsActive(req.getSearch(), page, pageSize)
        .map(result -> ApiResponsePaginationMerchantDetailDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("Data fetched successfully")
            .addAllData(result.getData().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList())
            .setPagination(buildMeta(page, pageSize, result.getTotalRecords()))
            .build());
  }

  @Override
  public Future<ApiResponsePaginationMerchantDetailDeleteAt> findByTrashed(FindAllMerchantRequest req) {
    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;

    return service.getMerchantDetailsTrashed(req.getSearch(), page, pageSize)
        .map(result -> ApiResponsePaginationMerchantDetailDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("Data fetched successfully")
            .addAllData(result.getData().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList())
            .setPagination(buildMeta(page, pageSize, result.getTotalRecords()))
            .build());
  }
}
