package io.example.cart.repository;

import io.vertx.core.Future;

public interface UserQueryRepository {
    Future<Boolean> findById(Integer userId);
}
