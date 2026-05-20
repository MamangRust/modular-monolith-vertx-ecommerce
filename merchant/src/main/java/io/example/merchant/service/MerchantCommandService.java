package io.example.merchant.service;

import io.example.common.model.ApiResponse;
import io.example.merchant.model.MerchantResponse;
import io.example.merchant.model.MerchantResponseDeleteAt;
import io.vertx.core.Future;
import pb.merchant.MerchantCommand.CreateMerchantRequest;
import pb.merchant.MerchantCommand.UpdateMerchantRequest;
import pb.merchant.MerchantCommand.UpdateMerchantStatusRequest;

public interface MerchantCommandService {
  Future<ApiResponse<MerchantResponse>> createMerchant(CreateMerchantRequest req);
  Future<ApiResponse<MerchantResponse>> updateMerchant(UpdateMerchantRequest req);
  Future<ApiResponse<MerchantResponse>> updateStatus(UpdateMerchantStatusRequest req);
  Future<ApiResponse<MerchantResponseDeleteAt>> trashMerchant(Integer merchantId);
  Future<ApiResponse<MerchantResponse>> restoreMerchant(Integer merchantId);
  Future<ApiResponse<Void>> deleteMerchantPermanently(Integer merchantId);
  Future<ApiResponse<Void>> restoreAllMerchants();
  Future<ApiResponse<Void>> deleteAllPermanentMerchants();
}
