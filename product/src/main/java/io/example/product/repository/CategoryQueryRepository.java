package io.example.product.repository;

import io.vertx.core.Future;

public interface CategoryQueryRepository {
    Future<Boolean> findById(Integer categoryId);
}
