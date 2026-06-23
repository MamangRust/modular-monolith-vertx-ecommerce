package io.example.review.service;

import io.example.common.domain.PagedResult;
import io.example.review.domain.requests.FindAllReview;
import io.example.review.domain.requests.FindAllReviewByMerchant;
import io.example.review.domain.requests.FindAllReviewByProduct;
import io.example.review.model.ReviewRelationsDetailResponse;
import io.example.review.model.ReviewResponse;
import io.example.review.model.ReviewResponseDeleteAt;
import io.vertx.core.Future;

public interface ReviewQueryService {
    Future<PagedResult<ReviewResponse>> getAllReviews(FindAllReview req);

    Future<PagedResult<ReviewResponseDeleteAt>> getActiveReviews(FindAllReview req);

    Future<PagedResult<ReviewResponseDeleteAt>> getTrashedReviews(FindAllReview req);

    Future<PagedResult<ReviewRelationsDetailResponse>> getReviewByProduct(FindAllReviewByProduct req);

    Future<PagedResult<ReviewRelationsDetailResponse>> getReviewByMerchant(FindAllReviewByMerchant req);

    Future<ReviewResponse> getReviewById(Long reviewId);
}