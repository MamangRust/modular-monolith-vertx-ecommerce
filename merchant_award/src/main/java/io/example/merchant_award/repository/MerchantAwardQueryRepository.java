package io.example.merchant_award.repository;

import io.example.common.domain.PagedResult;
import io.example.merchant_award.domain.requests.FindAllMerchantAwardsRequest;
import io.example.merchant_award.model.MerchantAward;
import io.vertx.core.Future;

public interface MerchantAwardQueryRepository {
  Future<PagedResult<MerchantAward>> getMerchantCertificationsAndAwards(FindAllMerchantAwardsRequest req);

  Future<PagedResult<MerchantAward>> getMerchantCertificationsAndAwardsActive(FindAllMerchantAwardsRequest req);

  Future<PagedResult<MerchantAward>> getTrashedCertificationsAndAwards(FindAllMerchantAwardsRequest req);

  Future<MerchantAward> getMerchantCertificationOrAward(Long id);

  Future<MerchantAward> findByTrashedId(Long id);
}
