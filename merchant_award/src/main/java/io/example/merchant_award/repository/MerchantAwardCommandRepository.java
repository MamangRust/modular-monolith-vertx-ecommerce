package io.example.merchant_award.repository;

import io.example.merchant_award.domain.requests.CreateMerchantAwardRequest;
import io.example.merchant_award.domain.requests.UpdateMerchantAwardRequest;
import io.example.merchant_award.model.MerchantAward;
import io.vertx.core.Future;

public interface MerchantAwardCommandRepository {
  Future<MerchantAward> create(CreateMerchantAwardRequest req);
  Future<MerchantAward> update(UpdateMerchantAwardRequest req);
  Future<MerchantAward> trash(Long id);
  Future<MerchantAward> restore(Long id);
  Future<Boolean> deletePermanent(Long id);
  Future<Integer> restoreAll();
  Future<Integer> deleteAllPermanent();
}
