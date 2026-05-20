package io.example.merchant_award.service;

import io.example.common.model.ApiResponse;
import io.example.merchant_award.model.MerchantAwardResponse;
import io.example.merchant_award.model.MerchantAwardResponseDeleteAt;
import io.vertx.core.Future;
import pb.merchant_award.MerchantAwardCommand.CreateMerchantAwardRequest;
import pb.merchant_award.MerchantAwardCommand.UpdateMerchantAwardRequest;

public interface MerchantAwardCommandService {
  Future<ApiResponse<MerchantAwardResponse>> create(CreateMerchantAwardRequest req);
  Future<ApiResponse<MerchantAwardResponse>> update(UpdateMerchantAwardRequest req);
  Future<ApiResponse<MerchantAwardResponseDeleteAt>> trash(Long id);
  Future<ApiResponse<MerchantAwardResponseDeleteAt>> restore(Long id);
  Future<ApiResponse<Boolean>> deletePermanent(Long id);
  Future<ApiResponse<Integer>> restoreAll();
  Future<ApiResponse<Integer>> deleteAllPermanent();
}
