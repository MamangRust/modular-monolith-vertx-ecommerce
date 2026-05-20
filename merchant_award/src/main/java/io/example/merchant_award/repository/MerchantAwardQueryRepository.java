package io.example.merchant_award.repository;

import io.example.common.domain.PagedResult;
import io.example.merchant_award.model.MerchantAward;
import io.vertx.core.Future;

public interface MerchantAwardQueryRepository {
  Future<PagedResult<MerchantAward>> getMerchantCertificationsAndAwards(String search, int page, int pageSize);
  Future<PagedResult<MerchantAward>> getMerchantCertificationsAndAwardsActive(String search, int page, int pageSize);
  Future<PagedResult<MerchantAward>> getTrashedCertificationsAndAwards(String search, int page, int pageSize);
  Future<MerchantAward> getMerchantCertificationOrAward(Long id);
}
