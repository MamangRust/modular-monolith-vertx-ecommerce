package io.example.merchant_business.handler;

import io.example.merchant_business.service.MerchantBusinessQueryService;
import io.vertx.core.Future;
import pb.merchant.MerchantQuery.FindAllMerchantRequest;
import pb.merchant_business.MerchantBusinessCommon.ApiResponseMerchantBusiness;
import pb.merchant_business.MerchantBusinessCommon.ApiResponsePaginationMerchantBusiness;
import pb.merchant_business.MerchantBusinessCommon.ApiResponsePaginationMerchantBusinessDeleteAt;
import pb.merchant_business.MerchantBusinessCommon.FindByIdMerchantBusinessRequest;

public class MerchantBusinessQueryHandler implements pb.merchant_business.VertxMerchantBusinessQueryServiceGrpcServer.MerchantBusinessQueryServiceApi {
  private final MerchantBusinessQueryService service;

  public MerchantBusinessQueryHandler(MerchantBusinessQueryService service) {
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
  public Future<ApiResponsePaginationMerchantBusiness> findAll(FindAllMerchantRequest req) {
    return service.getAll(req)
        .map(resp -> ApiResponsePaginationMerchantBusiness.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .addAllData(resp.data().stream().map(ProtoConverter::toProtoResponse).toList())
            .setPagination(toMeta(resp.pagination()))
            .build());
  }

  @Override
  public Future<ApiResponseMerchantBusiness> findById(FindByIdMerchantBusinessRequest req) {
    return service.getById((long) req.getId())
        .map(resp -> {
          var builder = ApiResponseMerchantBusiness.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponsePaginationMerchantBusinessDeleteAt> findByActive(FindAllMerchantRequest req) {
    return service.getActive(req)
        .map(resp -> ApiResponsePaginationMerchantBusinessDeleteAt.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .addAllData(resp.data().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList())
            .setPagination(toMeta(resp.pagination()))
            .build());
  }

  @Override
  public Future<ApiResponsePaginationMerchantBusinessDeleteAt> findByTrashed(FindAllMerchantRequest req) {
    return service.getTrashed(req)
        .map(resp -> ApiResponsePaginationMerchantBusinessDeleteAt.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .addAllData(resp.data().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList())
            .setPagination(toMeta(resp.pagination()))
            .build());
  }
}
