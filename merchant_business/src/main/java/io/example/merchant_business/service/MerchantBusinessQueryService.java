package io.example.merchant_business.service;

import io.example.common.domain.PagedResult;
import io.example.merchant_business.domain.requests.FindAllMerchantBusinessRequest;
import io.example.merchant_business.model.MerchantBusinessResponse;
import io.example.merchant_business.model.MerchantBusinessResponseDeleteAt;
import io.vertx.core.Future;

public interface MerchantBusinessQueryService {
  Future<PagedResult<MerchantBusinessResponse>> getAll(FindAllMerchantBusinessRequest req);

  Future<PagedResult<MerchantBusinessResponseDeleteAt>> getActive(FindAllMerchantBusinessRequest req);

  Future<PagedResult<MerchantBusinessResponseDeleteAt>> getTrashed(FindAllMerchantBusinessRequest req);

  Future<MerchantBusinessResponse> getById(Long id);
}