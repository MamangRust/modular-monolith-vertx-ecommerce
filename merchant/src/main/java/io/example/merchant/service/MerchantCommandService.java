package io.example.merchant.service;

import io.example.merchant.domain.requests.CreateMerchantRequest;
import io.example.merchant.domain.requests.UpdateMerchantRequest;
import io.example.merchant.domain.requests.UpdateMerchantStatusRequest;
import io.example.merchant.model.MerchantResponse;
import io.example.merchant.model.MerchantResponseDeleteAt;
import io.vertx.core.Future;

public interface MerchantCommandService {
  Future<MerchantResponse> createMerchant(CreateMerchantRequest request);

  Future<MerchantResponse> updateMerchant(UpdateMerchantRequest request);

  Future<MerchantResponse> updateStatus(UpdateMerchantStatusRequest request);

  Future<MerchantResponseDeleteAt> trashMerchant(Long merchantId);

  Future<MerchantResponse> restoreMerchant(Long merchantId);

  Future<Void> deleteMerchantPermanently(Long merchantId);

  Future<Void> restoreAllMerchants();

  Future<Void> deleteAllPermanentMerchants();
}