package io.example.merchant_detail.repository;

import io.vertx.core.Future;

public interface MerchantQueryRepository {
  Future<Boolean> findById(Integer merchantId);
}
