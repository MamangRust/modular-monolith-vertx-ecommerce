package io.example.merchant_policy.repository;

import io.vertx.core.Future;

public interface MerchantQueryRepository {
  Future<Boolean> existsById(int userId);
}
