package io.example.merchant_award.service;

import io.example.merchant_award.domain.requests.CreateMerchantAwardRequest;
import io.example.merchant_award.domain.requests.UpdateMerchantAwardRequest;
import io.example.merchant_award.model.MerchantAwardResponse;
import io.example.merchant_award.model.MerchantAwardResponseDeleteAt;
import io.vertx.core.Future;

public interface MerchantAwardCommandService {
  Future<MerchantAwardResponse> create(CreateMerchantAwardRequest req);

  Future<MerchantAwardResponse> update(UpdateMerchantAwardRequest req);

  Future<MerchantAwardResponseDeleteAt> trash(Long id);

  Future<MerchantAwardResponseDeleteAt> restore(Long id);

  Future<Void> deletePermanent(Long id);

  Future<Void> restoreAll();

  Future<Void> deleteAllPermanent();
}