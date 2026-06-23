package io.example.order.repository;

import io.vertx.core.Future;

public interface TransactionCommandRepository {
    Future<Boolean> deleteByOrderIDPermanent(Long orderId);
    Future<Boolean> deleteAll();
}
