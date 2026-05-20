package io.example.merchant_award.repository;

import io.vertx.core.Future;

public interface MerchantQueryRepository {
  Future<Boolean> findById(Integer merchantId);
}
