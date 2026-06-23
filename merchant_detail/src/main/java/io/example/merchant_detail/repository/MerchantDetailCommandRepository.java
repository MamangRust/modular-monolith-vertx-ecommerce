package io.example.merchant_detail.repository;

import io.example.merchant_detail.domain.requests.CreateMerchantDetailRequest;
import io.example.merchant_detail.domain.requests.UpdateMerchantDetailRequest;
import io.example.merchant_detail.model.MerchantDetail;
import io.vertx.core.Future;

public interface MerchantDetailCommandRepository {
  Future<MerchantDetail> create(CreateMerchantDetailRequest req);

  Future<MerchantDetail> update(UpdateMerchantDetailRequest req);

  Future<MerchantDetail> trash(Long id);

  Future<MerchantDetail> restore(Long id);

  Future<Boolean> deletePermanent(Long id);

  Future<Integer> restoreAll();

  Future<Integer> deleteAll();
}
