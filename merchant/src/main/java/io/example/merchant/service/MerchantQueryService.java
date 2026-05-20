package io.example.merchant.service;

import java.util.List;

import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.merchant.model.MerchantResponse;
import io.example.merchant.model.MerchantResponseDeleteAt;
import io.vertx.core.Future;
import pb.merchant.MerchantQuery.FindAllMerchantRequest;

public interface MerchantQueryService {
  Future<ApiResponsePagination<List<MerchantResponse>>> getAllMerchants(FindAllMerchantRequest req);
  Future<ApiResponsePagination<List<MerchantResponseDeleteAt>>> getActiveMerchants(FindAllMerchantRequest req);
  Future<ApiResponsePagination<List<MerchantResponseDeleteAt>>> getTrashedMerchants(FindAllMerchantRequest req);
  Future<ApiResponse<MerchantResponse>> getMerchantById(Integer merchantId);
}
