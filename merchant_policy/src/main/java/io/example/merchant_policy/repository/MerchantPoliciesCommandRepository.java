package io.example.merchant_policy.repository;

import io.example.merchant_policy.domain.requests.CreateMerchantPoliciesRequest;
import io.example.merchant_policy.domain.requests.UpdateMerchantPoliciesRequest;
import io.example.merchant_policy.model.MerchantPolicy;
import io.vertx.core.Future;

public interface MerchantPoliciesCommandRepository {
  Future<MerchantPolicy> create(CreateMerchantPoliciesRequest req);

  Future<MerchantPolicy> update(UpdateMerchantPoliciesRequest req);

  Future<MerchantPolicy> trash(Long id);

  Future<MerchantPolicy> restore(Long id);

  Future<Boolean> deletePermanent(Long id);

  Future<Integer> restoreAll();

  Future<Integer> deleteAllPermanent();
}
