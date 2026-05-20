package io.example.merchant_detail.repository;

import io.example.merchant_detail.model.MerchantDetail;
import io.vertx.core.Future;
import pb.merchant_detail.MerchantDetailCommand.CreateMerchantDetailRequest;
import pb.merchant_detail.MerchantDetailCommand.UpdateMerchantDetailRequest;

public interface MerchantDetailCommandRepository {
  Future<MerchantDetail> create(CreateMerchantDetailRequest req);
  Future<MerchantDetail> update(UpdateMerchantDetailRequest req);
  Future<MerchantDetail> trash(Long id);
  Future<MerchantDetail> restore(Long id);
  Future<Void> deletePermanent(Long id);
  Future<Integer> restoreAll();
  Future<Integer> deleteAll();
}
