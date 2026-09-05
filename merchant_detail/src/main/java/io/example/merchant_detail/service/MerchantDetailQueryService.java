package io.example.merchant_detail.service;

import io.example.common.domain.PagedResult;
import io.example.merchant_detail.domain.requests.FindAllMerchantDetailRequest;
import io.example.merchant_detail.model.MerchantDetailResponse;
import io.example.merchant_detail.model.MerchantDetailResponseDeleteAt;
import io.vertx.core.Future;

public interface MerchantDetailQueryService {
  Future<PagedResult<MerchantDetailResponse>> getMerchantDetails(FindAllMerchantDetailRequest req);

  Future<PagedResult<MerchantDetailResponseDeleteAt>> getMerchantDetailsActive(FindAllMerchantDetailRequest req);

  Future<PagedResult<MerchantDetailResponseDeleteAt>> getMerchantDetailsTrashed(FindAllMerchantDetailRequest req);

  Future<MerchantDetailResponse> getMerchantDetail(Long merchantDetailId);
}
