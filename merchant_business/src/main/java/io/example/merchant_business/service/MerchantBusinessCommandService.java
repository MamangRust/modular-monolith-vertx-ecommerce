package io.example.merchant_business.service;

import io.example.merchant_business.domain.requests.CreateMerchantBusinessRequest;
import io.example.merchant_business.domain.requests.UpdateMerchantBusinessRequest;
import io.example.merchant_business.model.MerchantBusinessResponse;
import io.example.merchant_business.model.MerchantBusinessResponseDeleteAt;
import io.vertx.core.Future;

public interface MerchantBusinessCommandService {
  Future<MerchantBusinessResponse> create(CreateMerchantBusinessRequest req);

  Future<MerchantBusinessResponse> update(UpdateMerchantBusinessRequest req);

  Future<MerchantBusinessResponseDeleteAt> trash(Long id);

  Future<MerchantBusinessResponseDeleteAt> restore(Long id);

  Future<Void> deletePermanent(Long id);

  Future<Void> restoreAll();

  Future<Void> deleteAllPermanent();
}