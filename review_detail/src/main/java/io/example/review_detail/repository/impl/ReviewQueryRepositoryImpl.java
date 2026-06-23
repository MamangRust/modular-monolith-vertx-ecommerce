package io.example.review_detail.repository.impl;

import io.example.review_detail.repository.ReviewQueryRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewQueryRepositoryImpl implements ReviewQueryRepository {
    private final pb.review.VertxReviewQueryServiceGrpcClient client;

    @Override
    public Future<Boolean> exists(int reviewId) {
        return client.findById(pb.review.ReviewCommon.FindByIdReviewRequest.newBuilder().setId(reviewId).build())
                .map(res -> "success".equalsIgnoreCase(res.getStatus()))
                .recover(err -> Future.succeededFuture(false));
    }
}
