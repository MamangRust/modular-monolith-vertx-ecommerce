package io.example.review.service;

import java.util.List;
import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.review.model.ReviewResponse;
import io.example.review.model.ReviewResponseDeleteAt;
import io.example.review.model.ReviewRelationsDetailResponse;
import io.example.review.model.FindAllReview;
import io.example.review.model.FindAllReviewByProduct;
import io.example.review.model.FindAllReviewByMerchant;
import io.vertx.core.Future;

public interface ReviewQueryService {
    Future<ApiResponsePagination<List<ReviewResponse>>> getAllReviews(FindAllReview req);
    Future<ApiResponsePagination<List<ReviewResponseDeleteAt>>> getActiveReviews(FindAllReview req);
    Future<ApiResponsePagination<List<ReviewResponseDeleteAt>>> getTrashedReviews(FindAllReview req);
    Future<ApiResponsePagination<List<ReviewRelationsDetailResponse>>> getReviewByProduct(FindAllReviewByProduct req);
    Future<ApiResponsePagination<List<ReviewRelationsDetailResponse>>> getReviewByMerchant(FindAllReviewByMerchant req);
    Future<ApiResponse<ReviewResponse>> getReviewById(Long reviewId);
}
