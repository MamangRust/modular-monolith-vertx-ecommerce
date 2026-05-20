package io.example.merchant_policy.repository;

import io.example.common.domain.PagedResult;
import io.example.merchant_policy.model.MerchantPolicy;
import io.example.merchant_policy.model.MerchantPolicyRelation;
import io.vertx.core.Future;

public interface MerchantPoliciesQueryRepository {
  Future<PagedResult<MerchantPolicyRelation>> getMerchantPolicies(String search, int page, int pageSize);
  Future<PagedResult<MerchantPolicyRelation>> getMerchantPoliciesActive(String search, int page, int pageSize);
  Future<PagedResult<MerchantPolicyRelation>> getMerchantPoliciesTrashed(String search, int page, int pageSize);
  Future<MerchantPolicy> getMerchantPolicy(Long id);
}
