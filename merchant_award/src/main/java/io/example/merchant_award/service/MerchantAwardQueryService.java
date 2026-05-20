package io.example.merchant_award.service;

import java.util.List;

import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.merchant_award.model.MerchantAwardResponse;
import io.example.merchant_award.model.MerchantAwardResponseDeleteAt;
import io.vertx.core.Future;
import pb.merchant.MerchantQuery.FindAllMerchantRequest;

public interface MerchantAwardQueryService {
  Future<ApiResponsePagination<List<MerchantAwardResponse>>> getAll(FindAllMerchantRequest req);
  Future<ApiResponsePagination<List<MerchantAwardResponseDeleteAt>>> getActive(FindAllMerchantRequest req);
  Future<ApiResponsePagination<List<MerchantAwardResponseDeleteAt>>> getTrashed(FindAllMerchantRequest req);
  Future<ApiResponse<MerchantAwardResponse>> getById(Long id);
}
