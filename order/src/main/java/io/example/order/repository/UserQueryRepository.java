package io.example.order.repository;

import io.vertx.core.Future;

public interface UserQueryRepository {
    Future<Boolean> findById(Integer userId);
}
