package io.example.merchant_policy.repository;

import io.example.common.domain.PagedResult;
import io.example.merchant_policy.domain.requests.FindAllMerchantPoliciesRequest;
import io.example.merchant_policy.model.MerchantPolicy;
import io.example.merchant_policy.model.MerchantPolicyRelation;
import io.vertx.core.Future;

public interface MerchantPoliciesQueryRepository {
  Future<PagedResult<MerchantPolicyRelation>> getMerchantPolicies(FindAllMerchantPoliciesRequest req);

  Future<PagedResult<MerchantPolicyRelation>> getMerchantPoliciesActive(FindAllMerchantPoliciesRequest req);

  Future<PagedResult<MerchantPolicyRelation>> getMerchantPoliciesTrashed(FindAllMerchantPoliciesRequest req);

  Future<MerchantPolicy> getMerchantPolicy(Long id);

  Future<MerchantPolicy> findByTrashedId(Long id);
}
