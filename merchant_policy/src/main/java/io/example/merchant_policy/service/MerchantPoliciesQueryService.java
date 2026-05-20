package io.example.merchant_policy.service;

import io.example.common.domain.PagedResult;
import io.example.merchant_policy.model.MerchantPoliciesRelationResponse;
import io.example.merchant_policy.model.MerchantPoliciesRelationResponseDeleteAt;
import io.example.merchant_policy.model.MerchantPoliciesResponse;
import io.vertx.core.Future;

public interface MerchantPoliciesQueryService {
  Future<PagedResult<MerchantPoliciesRelationResponse>> getMerchantPolicies(String search, int page, int pageSize);
  Future<PagedResult<MerchantPoliciesRelationResponseDeleteAt>> getMerchantPoliciesActive(String search, int page, int pageSize);
  Future<PagedResult<MerchantPoliciesRelationResponseDeleteAt>> getMerchantPoliciesTrashed(String search, int page, int pageSize);
  Future<MerchantPoliciesResponse> getMerchantPolicy(Long id);
}
