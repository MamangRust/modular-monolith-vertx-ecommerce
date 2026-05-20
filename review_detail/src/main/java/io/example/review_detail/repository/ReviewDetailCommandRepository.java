package io.example.review_detail.repository;

import io.example.review_detail.model.CreateReviewDetailRequest;
import io.example.review_detail.model.ReviewDetail;
import io.example.review_detail.model.UpdateReviewDetailRequest;
import io.vertx.core.Future;

public interface ReviewDetailCommandRepository {
    Future<ReviewDetail> createReviewDetail(CreateReviewDetailRequest req);
    Future<ReviewDetail> updateReviewDetail(UpdateReviewDetailRequest req);
    Future<ReviewDetail> trashReviewDetail(Integer reviewDetailId);
    Future<ReviewDetail> restoreReviewDetail(Integer reviewDetailId);
    Future<Void> deletePermanentReviewDetail(Integer reviewDetailId);
    Future<Void> restoreAllReviewDetails();
    Future<Void> deleteAllPermanentReviewDetails();
}
