package io.example.merchant.handler;

import io.example.merchant.service.MerchantQueryService;
import io.vertx.core.Future;
import pb.merchant.MerchantCommon.*;
import pb.merchant.MerchantQuery.*;

public class MerchantQueryHandler implements pb.merchant.VertxMerchantQueryServiceGrpcServer.MerchantQueryServiceApi {
  private final MerchantQueryService service;

  public MerchantQueryHandler(MerchantQueryService service) {
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
  public Future<ApiResponsePaginationMerchant> findAll(FindAllMerchantRequest req) {
    return service.getAllMerchants(req)
        .map(resp -> ApiResponsePaginationMerchant.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .addAllData(resp.data().stream().map(ProtoConverter::fromMerchantResponse).toList())
            .setPagination(toMeta(resp.pagination()))
            .build());
  }

  @Override
  public Future<ApiResponseMerchant> findById(FindByIdMerchantRequest req) {
    return service.getMerchantById(req.getId())
        .map(resp -> {
          var builder = ApiResponseMerchant.newBuilder()
              .setStatus(resp.status())
              .setMessage(resp.message());
          if (resp.data() != null) {
            builder.setData(ProtoConverter.fromMerchantResponse(resp.data()));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponsePaginationMerchantDeleteAt> findByActive(FindAllMerchantRequest req) {
    return service.getActiveMerchants(req)
        .map(resp -> ApiResponsePaginationMerchantDeleteAt.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .addAllData(resp.data().stream().map(ProtoConverter::fromMerchantResponseDeleteAt).toList())
            .setPagination(toMeta(resp.pagination()))
            .build());
  }

  @Override
  public Future<ApiResponsePaginationMerchantDeleteAt> findByTrashed(FindAllMerchantRequest req) {
    return service.getTrashedMerchants(req)
        .map(resp -> ApiResponsePaginationMerchantDeleteAt.newBuilder()
            .setStatus(resp.status())
            .setMessage(resp.message())
            .addAllData(resp.data().stream().map(ProtoConverter::fromMerchantResponseDeleteAt).toList())
            .setPagination(toMeta(resp.pagination()))
            .build());
  }
}
