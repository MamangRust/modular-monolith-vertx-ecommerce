package io.example.transaction.repository;

import io.vertx.core.Future;

public interface MerchantQueryRepository {
    Future<Boolean> findById(Integer merchantId);
}
