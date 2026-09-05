package io.example.order.repository;

import io.vertx.core.Future;

public interface ProductCommandRepository {
    Future<Boolean> updateProductCountStock(Integer productId, Integer stock);

    /**
     * Atomically decrement product stock via gRPC. Returns a successful
     * Future if stock was sufficient, or fails with gRPC-status error if
     * stock was insufficient or product not found.
     */
    Future<Void> decrementStock(Integer productId, Integer quantity);

    Future<Void> incrementStock(Integer productId, Integer quantity);
}
