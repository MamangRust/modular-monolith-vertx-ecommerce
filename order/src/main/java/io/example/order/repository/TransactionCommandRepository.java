package io.example.order.repository;

import io.vertx.core.Future;

public interface TransactionCommandRepository {
    Future<Boolean> deleteByOrderIDPermanent(Integer orderId);
    Future<Boolean> deleteAll();
}
