package io.example.merchant_policy.service;

import io.example.common.domain.ApiResponse;
import io.example.merchant_policy.model.MerchantPoliciesResponse;
import io.example.merchant_policy.model.MerchantPoliciesResponseDeleteAt;
import io.vertx.core.Future;

public interface MerchantPoliciesCommandService {
  Future<ApiResponse<MerchantPoliciesResponse>> create(pb.merchant_policy.MerchantPolicyCommand.CreateMerchantPoliciesRequest req);
  Future<ApiResponse<MerchantPoliciesResponse>> update(pb.merchant_policy.MerchantPolicyCommand.UpdateMerchantPoliciesRequest req);
  Future<ApiResponse<MerchantPoliciesResponseDeleteAt>> trash(Long id);
  Future<ApiResponse<MerchantPoliciesResponseDeleteAt>> restore(Long id);
  Future<ApiResponse<Boolean>> deletePermanent(Long id);
  Future<ApiResponse<Integer>> restoreAll();
  Future<ApiResponse<Integer>> deleteAllPermanent();
}
