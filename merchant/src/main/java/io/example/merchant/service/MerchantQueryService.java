package io.example.merchant.service;

import io.example.common.domain.PagedResult;
import io.example.merchant.domain.requests.FindAllMerchantRequest;
import io.example.merchant.model.MerchantResponse;
import io.example.merchant.model.MerchantResponseDeleteAt;
import io.vertx.core.Future;

public interface MerchantQueryService {
  Future<PagedResult<MerchantResponse>> getAllMerchants(FindAllMerchantRequest req);

  Future<PagedResult<MerchantResponseDeleteAt>> getActiveMerchants(FindAllMerchantRequest req);

  Future<PagedResult<MerchantResponseDeleteAt>> getTrashedMerchants(FindAllMerchantRequest req);

  Future<MerchantResponse> getMerchantById(Long merchantId);
}