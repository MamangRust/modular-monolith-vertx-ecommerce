package io.example.merchant_detail.repository;

import io.example.common.domain.PagedResult;
import io.example.merchant_detail.model.MerchantDetailsRelation;
import io.vertx.core.Future;

public interface MerchantDetailQueryRepository {
  Future<PagedResult<MerchantDetailsRelation>> getMerchantDetails(String search, int page, int pageSize);
  Future<PagedResult<MerchantDetailsRelation>> getMerchantDetailsActive(String search, int page, int pageSize);
  Future<PagedResult<MerchantDetailsRelation>> getMerchantDetailsTrashed(String search, int page, int pageSize);
  Future<MerchantDetailsRelation> getMerchantDetail(Long merchantDetailId);
}
