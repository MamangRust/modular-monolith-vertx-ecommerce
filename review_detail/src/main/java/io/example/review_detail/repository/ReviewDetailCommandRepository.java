package io.example.review_detail.repository;

import io.example.review_detail.domain.requests.CreateReviewDetailRequest;
import io.example.review_detail.model.ReviewDetail;
import io.example.review_detail.domain.requests.UpdateReviewDetailRequest;
import io.vertx.core.Future;

public interface ReviewDetailCommandRepository {
    Future<ReviewDetail> createReviewDetail(CreateReviewDetailRequest req);

    Future<ReviewDetail> updateReviewDetail(UpdateReviewDetailRequest req);

    Future<ReviewDetail> trashReviewDetail(Long reviewDetailId);

    Future<ReviewDetail> restoreReviewDetail(Long reviewDetailId);

    Future<Boolean> deletePermanentReviewDetail(Long reviewDetailId);

    Future<Integer> restoreAllReviewDetails();

    Future<Integer> deleteAllPermanentReviewDetails();
}
