package io.example.merchant_business.service;

import java.util.List;
import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.merchant_business.model.MerchantBusinessResponse;
import io.example.merchant_business.model.MerchantBusinessResponseDeleteAt;
import io.vertx.core.Future;
import pb.merchant.MerchantQuery.FindAllMerchantRequest;

public interface MerchantBusinessQueryService {
  Future<ApiResponsePagination<List<MerchantBusinessResponse>>> getAll(FindAllMerchantRequest req);
  Future<ApiResponsePagination<List<MerchantBusinessResponseDeleteAt>>> getActive(FindAllMerchantRequest req);
  Future<ApiResponsePagination<List<MerchantBusinessResponseDeleteAt>>> getTrashed(FindAllMerchantRequest req);
  Future<ApiResponse<MerchantBusinessResponse>> getById(Long id);
}
