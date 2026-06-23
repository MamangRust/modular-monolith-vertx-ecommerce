package io.example.merchant_detail.repository;

import io.example.common.domain.PagedResult;
import io.example.merchant_detail.domain.requests.FindAllMerchantDetailRequest;
import io.example.merchant_detail.model.MerchantDetail;
import io.example.merchant_detail.model.MerchantDetailsRelation;
import io.vertx.core.Future;

public interface MerchantDetailQueryRepository {
  Future<PagedResult<MerchantDetailsRelation>> getMerchantDetails(FindAllMerchantDetailRequest req);

  Future<PagedResult<MerchantDetailsRelation>> getMerchantDetailsActive(FindAllMerchantDetailRequest req);

  Future<PagedResult<MerchantDetailsRelation>> getMerchantDetailsTrashed(FindAllMerchantDetailRequest req);

  Future<MerchantDetailsRelation> getMerchantDetail(Long merchantDetailId);

  Future<MerchantDetail> findByTrashedId(Long merchantDetailId);

}
