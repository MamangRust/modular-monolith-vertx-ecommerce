package io.example.merchant_policy.service;

import io.example.common.domain.PagedResult;
import io.example.merchant_policy.domain.requests.FindAllMerchantPoliciesRequest;
import io.example.merchant_policy.model.MerchantPoliciesRelationResponse;
import io.example.merchant_policy.model.MerchantPoliciesRelationResponseDeleteAt;
import io.example.merchant_policy.model.MerchantPoliciesResponse;
import io.vertx.core.Future;

public interface MerchantPoliciesQueryService {
  Future<PagedResult<MerchantPoliciesRelationResponse>> getMerchantPolicies(FindAllMerchantPoliciesRequest req);

  Future<PagedResult<MerchantPoliciesRelationResponseDeleteAt>> getMerchantPoliciesActive(
      FindAllMerchantPoliciesRequest req);

  Future<PagedResult<MerchantPoliciesRelationResponseDeleteAt>> getMerchantPoliciesTrashed(
      FindAllMerchantPoliciesRequest req);

  Future<MerchantPoliciesResponse> getMerchantPolicy(Long id);
}
