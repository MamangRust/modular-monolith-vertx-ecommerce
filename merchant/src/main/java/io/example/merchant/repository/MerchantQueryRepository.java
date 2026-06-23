package io.example.merchant.repository;

import io.example.common.domain.PagedResult;
import io.example.merchant.domain.requests.FindAllMerchantRequest;
import io.example.merchant.model.Merchant;
import io.vertx.core.Future;

public interface MerchantQueryRepository {
  Future<PagedResult<Merchant>> getMerchants(FindAllMerchantRequest req);

  Future<PagedResult<Merchant>> getActiveMerchants(FindAllMerchantRequest req);

  Future<PagedResult<Merchant>> getTrashedMerchants(FindAllMerchantRequest req);

  Future<Merchant> getMerchantById(Long merchantId);

  Future<Merchant> findByTrashedId(Long merchantId);
}
