package io.example.merchant_business.repository;

import io.vertx.core.Future;

public interface MerchantQueryRepository {
  Future<Boolean> findById(Integer id);
}
