package io.example.merchant_detail.service;

import io.example.common.domain.PagedResult;
import io.example.merchant_detail.model.MerchantDetailResponse;
import io.example.merchant_detail.model.MerchantDetailResponseDeleteAt;
import io.vertx.core.Future;

public interface MerchantDetailQueryService {
  Future<PagedResult<MerchantDetailResponse>> getMerchantDetails(String search, int page, int pageSize);
  Future<PagedResult<MerchantDetailResponse>> getMerchantDetailsActive(String search, int page, int pageSize);
  Future<PagedResult<MerchantDetailResponseDeleteAt>> getMerchantDetailsTrashed(String search, int page, int pageSize);
  Future<MerchantDetailResponse> getMerchantDetail(Long merchantDetailId);
}
