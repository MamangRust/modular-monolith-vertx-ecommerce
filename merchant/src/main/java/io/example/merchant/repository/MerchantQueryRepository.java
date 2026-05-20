package io.example.merchant.repository;

import io.example.common.domain.PagedResult;
import io.example.merchant.model.Merchant;
import io.vertx.core.Future;

public interface MerchantQueryRepository {
  Future<PagedResult<Merchant>> getMerchants(String search, int page, int pageSize);
  Future<PagedResult<Merchant>> getActiveMerchants(String search, int page, int pageSize);
  Future<PagedResult<Merchant>> getTrashedMerchants(String search, int page, int pageSize);
  Future<Merchant> getMerchantById(Integer merchantId);
}
