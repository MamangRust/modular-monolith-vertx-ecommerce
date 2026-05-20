package io.example.merchant_award.handler;

import io.example.merchant_award.service.MerchantAwardQueryService;
import io.vertx.core.Future;
import pb.merchant.MerchantQuery.FindAllMerchantRequest;
import pb.merchant_award.MerchantAwardCommon.ApiResponseMerchantAward;
import pb.merchant_award.MerchantAwardCommon.ApiResponsePaginationMerchantAward;
import pb.merchant_award.MerchantAwardCommon.ApiResponsePaginationMerchantAwardDeleteAt;
import pb.merchant_award.MerchantAwardCommon.FindByIdMerchantAwardRequest;

public class MerchantAwardQueryHandler implements pb.merchant_award.VertxMerchantAwardQueryServiceGrpcServer.MerchantAwardQueryServiceApi {
  private final MerchantAwardQueryService service;

  public MerchantAwardQueryHandler(MerchantAwardQueryService service) {
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
  public Future<ApiResponsePaginationMerchantAward> findAll(FindAllMerchantRequest req) {
    return service.getAll(req)
        .map(resp -> ApiResponsePaginationMerchantAward.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .addAllData(resp.data().stream().map(ProtoConverter::toProtoResponse).toList())
            .setPagination(toMeta(resp.pagination()))
            .build());
  }

  @Override
  public Future<ApiResponseMerchantAward> findById(FindByIdMerchantAwardRequest req) {
    return service.getById((long) req.getId())
        .map(resp -> {
          var builder = ApiResponseMerchantAward.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.toProtoResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponsePaginationMerchantAwardDeleteAt> findByActive(FindAllMerchantRequest req) {
    return service.getActive(req)
        .map(resp -> ApiResponsePaginationMerchantAwardDeleteAt.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .addAllData(resp.data().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList())
            .setPagination(toMeta(resp.pagination()))
            .build());
  }

  @Override
  public Future<ApiResponsePaginationMerchantAwardDeleteAt> findByTrashed(FindAllMerchantRequest req) {
    return service.getTrashed(req)
        .map(resp -> ApiResponsePaginationMerchantAwardDeleteAt.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .addAllData(resp.data().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList())
            .setPagination(toMeta(resp.pagination()))
            .build());
  }
}
