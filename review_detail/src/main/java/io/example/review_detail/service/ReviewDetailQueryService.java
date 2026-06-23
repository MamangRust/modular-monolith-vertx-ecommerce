package io.example.review_detail.service;

import io.example.common.domain.PagedResult;
import io.example.review_detail.domain.requests.FindAllReview;
import io.example.review_detail.model.ReviewDetailResponse;
import io.example.review_detail.model.ReviewDetailResponseDeleteAt;
import io.vertx.core.Future;

public interface ReviewDetailQueryService {
    Future<PagedResult<ReviewDetailResponse>> getAllReviewDetails(FindAllReview req);

    Future<PagedResult<ReviewDetailResponseDeleteAt>> getActiveReviewDetails(FindAllReview req);

    Future<PagedResult<ReviewDetailResponseDeleteAt>> getTrashedReviewDetails(FindAllReview req);

    Future<ReviewDetailResponse> getReviewDetailById(Long reviewDetailId);
}