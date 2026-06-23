package io.example.merchant_award.service;

import io.example.common.domain.PagedResult;
import io.example.merchant_award.domain.requests.FindAllMerchantAwardsRequest;
import io.example.merchant_award.model.MerchantAwardResponse;
import io.example.merchant_award.model.MerchantAwardResponseDeleteAt;
import io.vertx.core.Future;

public interface MerchantAwardQueryService {
  Future<PagedResult<MerchantAwardResponse>> getAll(FindAllMerchantAwardsRequest req);

  Future<PagedResult<MerchantAwardResponseDeleteAt>> getActive(FindAllMerchantAwardsRequest req);

  Future<PagedResult<MerchantAwardResponseDeleteAt>> getTrashed(FindAllMerchantAwardsRequest req);

  Future<MerchantAwardResponse> getById(Long id);
}