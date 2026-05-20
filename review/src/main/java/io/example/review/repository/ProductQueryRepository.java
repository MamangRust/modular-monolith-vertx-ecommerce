package io.example.review.repository;

import io.vertx.core.Future;

public interface ProductQueryRepository {
    Future<Boolean> findById(Integer productId);
}
