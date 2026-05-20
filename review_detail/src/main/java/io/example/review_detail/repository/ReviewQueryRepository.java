package io.example.review_detail.repository;

import io.vertx.core.Future;

public interface ReviewQueryRepository {
    Future<Boolean> exists(int reviewId);
}
