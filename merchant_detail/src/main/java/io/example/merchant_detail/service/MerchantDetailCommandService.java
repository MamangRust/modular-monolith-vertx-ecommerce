package io.example.merchant_detail.service;

import io.example.merchant_detail.domain.requests.CreateMerchantDetailRequest;
import io.example.merchant_detail.domain.requests.UpdateMerchantDetailRequest;
import io.example.merchant_detail.model.MerchantDetailResponse;
import io.example.merchant_detail.model.MerchantDetailResponseDeleteAt;
import io.vertx.core.Future;

public interface MerchantDetailCommandService {
  Future<MerchantDetailResponse> create(CreateMerchantDetailRequest req);

  Future<MerchantDetailResponse> update(UpdateMerchantDetailRequest req);

  Future<MerchantDetailResponseDeleteAt> trash(Long id);

  Future<MerchantDetailResponseDeleteAt> restore(Long id);

  Future<Void> deletePermanent(Long id);

  Future<Void> restoreAll();

  Future<Void> deleteAllPermanent();
}