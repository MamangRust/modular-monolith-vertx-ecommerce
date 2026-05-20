package io.example.review.repository;

import io.example.review.model.Review;
import io.example.review.model.CreateReviewRequest;
import io.example.review.model.UpdateReviewRequest;
import io.vertx.core.Future;

public interface ReviewCommandRepository {
    Future<Review> createReview(CreateReviewRequest req);
    Future<Review> updateReview(UpdateReviewRequest req);
    Future<Review> trashReview(Long reviewId);
    Future<Review> restoreReview(Long reviewId);
    Future<Void> deleteReviewPermanently(Long reviewId);
    Future<Integer> restoreAllReviews();
    Future<Integer> deleteAllPermanentReviews();
}
