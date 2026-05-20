package io.example.review.service;

import io.example.common.model.ApiResponse;
import io.example.review.model.ReviewResponse;
import io.example.review.model.ReviewResponseDeleteAt;
import io.example.review.model.CreateReviewRequest;
import io.example.review.model.UpdateReviewRequest;
import io.vertx.core.Future;

public interface ReviewCommandService {
    Future<ApiResponse<ReviewResponse>> createReview(CreateReviewRequest req);
    Future<ApiResponse<ReviewResponse>> updateReview(UpdateReviewRequest req);
    Future<ApiResponse<ReviewResponseDeleteAt>> trashReview(Long reviewId);
    Future<ApiResponse<ReviewResponseDeleteAt>> restoreReview(Long reviewId);
    Future<ApiResponse<Void>> deleteReviewPermanently(Long reviewId);
    Future<ApiResponse<Void>> restoreAllReviews();
    Future<ApiResponse<Void>> deleteAllPermanentReviews();
}
