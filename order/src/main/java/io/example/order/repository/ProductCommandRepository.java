package io.example.order.repository;

import io.vertx.core.Future;

public interface ProductCommandRepository {
    Future<Boolean> updateProductCountStock(Integer productId, Integer stock);
}
