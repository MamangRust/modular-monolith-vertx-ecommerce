package io.example.merchant_award.repository;

import io.example.merchant_award.model.MerchantAward;
import io.vertx.core.Future;
import pb.merchant_award.MerchantAwardCommand.CreateMerchantAwardRequest;
import pb.merchant_award.MerchantAwardCommand.UpdateMerchantAwardRequest;

public interface MerchantAwardCommandRepository {
  Future<MerchantAward> create(CreateMerchantAwardRequest req);
  Future<MerchantAward> update(UpdateMerchantAwardRequest req);
  Future<MerchantAward> trash(Long id);
  Future<MerchantAward> restore(Long id);
  Future<Boolean> deletePermanent(Long id);
  Future<Integer> restoreAll();
  Future<Integer> deleteAllPermanent();
}
