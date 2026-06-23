package io.example.merchant_policy.service;

import io.example.merchant_policy.domain.requests.CreateMerchantPoliciesRequest;
import io.example.merchant_policy.domain.requests.UpdateMerchantPoliciesRequest;
import io.example.merchant_policy.model.MerchantPoliciesResponse;
import io.example.merchant_policy.model.MerchantPoliciesResponseDeleteAt;
import io.vertx.core.Future;

public interface MerchantPoliciesCommandService {
  Future<MerchantPoliciesResponse> create(CreateMerchantPoliciesRequest req);

  Future<MerchantPoliciesResponse> update(UpdateMerchantPoliciesRequest req);

  Future<MerchantPoliciesResponseDeleteAt> trash(Long id);

  Future<MerchantPoliciesResponseDeleteAt> restore(Long id);

  Future<Void> deletePermanent(Long id);

  Future<Void> restoreAll();

  Future<Void> deleteAllPermanent();
}