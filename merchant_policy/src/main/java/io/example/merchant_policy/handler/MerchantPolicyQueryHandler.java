package io.example.merchant_policy.handler;

import io.example.merchant_policy.service.MerchantPoliciesQueryService;
import io.example.merchant_policy.domain.requests.FindAllMerchantPoliciesRequest;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.merchant.MerchantQuery.FindAllMerchantRequest;
import pb.merchant_policy.MerchantPolicyCommon.ApiResponseMerchantPolicies;
import pb.merchant_policy.MerchantPolicyCommon.ApiResponsePaginationMerchantPolicies;
import pb.merchant_policy.MerchantPolicyCommon.ApiResponsePaginationMerchantPoliciesDeleteAt;
import pb.merchant_policy.MerchantPolicyCommon.FindByIdMerchantPoliciesRequest;

@RequiredArgsConstructor
public class MerchantPolicyQueryHandler
    implements pb.merchant_policy.VertxMerchantPolicyQueryServiceGrpcServer.MerchantPolicyQueryServiceApi {
  private final MerchantPoliciesQueryService service;

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
  public Future<ApiResponsePaginationMerchantPolicies> findAll(FindAllMerchantRequest req) {
    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    var domainReq = FindAllMerchantPoliciesRequest.builder()
        .page(page)
        .pageSize(pageSize)
        .search(req.getSearch())
        .build();

    return service.getMerchantPolicies(domainReq)
        .map(result -> ApiResponsePaginationMerchantPolicies.newBuilder()
            .setStatus("success")
            .setMessage("Data fetched successfully")
            .addAllData(result.getData().stream().map(ProtoConverter::toProto).toList())
            .setPagination(buildMeta(page, pageSize, result.getTotalRecords()))
            .build());
  }

  @Override
  public Future<ApiResponseMerchantPolicies> findById(FindByIdMerchantPoliciesRequest req) {
    return service.getMerchantPolicy((long) req.getId())
        .map(resp -> {
          var builder = ApiResponseMerchantPolicies.newBuilder()
              .setStatus("success")
              .setMessage("Data fetched successfully");
          if (resp != null) {
            builder.setData(ProtoConverter.toProto(resp));
          }
          return builder.build();
        });
  }

  @Override
  public Future<ApiResponsePaginationMerchantPoliciesDeleteAt> findByActive(FindAllMerchantRequest req) {
    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    var domainReq = FindAllMerchantPoliciesRequest.builder()
        .page(page)
        .pageSize(pageSize)
        .search(req.getSearch())
        .build();

    return service.getMerchantPoliciesActive(domainReq)
        .map(result -> ApiResponsePaginationMerchantPoliciesDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("Data fetched successfully")
            .addAllData(result.getData().stream().map(ProtoConverter::toProto).toList())
            .setPagination(buildMeta(page, pageSize, result.getTotalRecords()))
            .build());
  }

  @Override
  public Future<ApiResponsePaginationMerchantPoliciesDeleteAt> findByTrashed(FindAllMerchantRequest req) {
    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    var domainReq = FindAllMerchantPoliciesRequest.builder()
        .page(page)
        .pageSize(pageSize)
        .search(req.getSearch())
        .build();

    return service.getMerchantPoliciesTrashed(domainReq)
        .map(result -> ApiResponsePaginationMerchantPoliciesDeleteAt.newBuilder()
            .setStatus("success")
            .setMessage("Data fetched successfully")
            .addAllData(result.getData().stream().map(ProtoConverter::toProto).toList())
            .setPagination(buildMeta(page, pageSize, result.getTotalRecords()))
            .build());
  }
}
