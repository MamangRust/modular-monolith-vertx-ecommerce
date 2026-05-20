package io.example.merchant_policy.repository;

import io.example.merchant_policy.model.MerchantPolicy;
import io.vertx.core.Future;

public interface MerchantPoliciesCommandRepository {
  Future<MerchantPolicy> create(pb.merchant_policy.MerchantPolicyCommand.CreateMerchantPoliciesRequest req);
  Future<MerchantPolicy> update(pb.merchant_policy.MerchantPolicyCommand.UpdateMerchantPoliciesRequest req);
  Future<MerchantPolicy> trash(Long id);
  Future<MerchantPolicy> restore(Long id);
  Future<Void> deletePermanent(Long id);
  Future<Integer> restoreAll();
  Future<Integer> deleteAllPermanent();
}
