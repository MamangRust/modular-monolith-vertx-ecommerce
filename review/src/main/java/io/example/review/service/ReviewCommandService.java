package io.example.review.service;

import io.example.review.domain.requests.CreateReviewRequest;
import io.example.review.domain.requests.UpdateReviewRequest;
import io.example.review.model.ReviewResponse;
import io.example.review.model.ReviewResponseDeleteAt;
import io.vertx.core.Future;

public interface ReviewCommandService {
    Future<ReviewResponse> createReview(CreateReviewRequest req);

    Future<ReviewResponse> updateReview(UpdateReviewRequest req);

    Future<ReviewResponseDeleteAt> trashReview(Long reviewId);

    Future<ReviewResponseDeleteAt> restoreReview(Long reviewId);

    Future<Void> deleteReviewPermanently(Long reviewId);

    Future<Void> restoreAllReviews();

    Future<Void> deleteAllPermanentReviews();
}