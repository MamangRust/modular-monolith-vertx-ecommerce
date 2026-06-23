package io.example.merchant.repository;

import io.example.merchant.domain.requests.CreateMerchantRequest;
import io.example.merchant.domain.requests.UpdateMerchantRequest;
import io.example.merchant.domain.requests.UpdateMerchantStatusRequest;
import io.example.merchant.model.Merchant;
import io.vertx.core.Future;

public interface MerchantCommandRepository {
  Future<Merchant> createMerchant(CreateMerchantRequest request);

  Future<Merchant> updateMerchant(UpdateMerchantRequest request);

  Future<Merchant> updateStatus(UpdateMerchantStatusRequest request);

  Future<Merchant> trashMerchant(Long merchantId);

  Future<Merchant> restoreMerchant(Long merchantId);

  Future<Boolean> deleteMerchantPermanently(Long merchantId);

  Future<Integer> restoreAllMerchants();

  Future<Integer> deleteAllPermanentMerchants();
}
