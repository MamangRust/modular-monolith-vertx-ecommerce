package io.example.merchant.repository;

import io.example.merchant.model.Merchant;
import io.vertx.core.Future;

public interface MerchantCommandRepository {
  Future<Merchant> createMerchant(Integer userId, String name, String apiKey, String status);
  Future<Merchant> updateMerchant(Integer merchantId, String name, String status);
  Future<Merchant> updateStatus(Integer merchantId, String status);
  Future<Merchant> trashMerchant(Integer merchantId);
  Future<Merchant> restoreMerchant(Integer merchantId);
  Future<Void> deleteMerchantPermanently(Integer merchantId);
  Future<Void> restoreAllMerchants();
  Future<Void> deleteAllPermanentMerchants();
}
