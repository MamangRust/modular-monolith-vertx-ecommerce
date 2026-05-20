package io.example.review_detail.service;

import io.example.common.model.ApiResponse;
import io.example.review_detail.model.CreateReviewDetailRequest;
import io.example.review_detail.model.ReviewDetailResponse;
import io.example.review_detail.model.ReviewDetailResponseDeleteAt;
import io.example.review_detail.model.UpdateReviewDetailRequest;
import io.vertx.core.Future;

public interface ReviewDetailCommandService {
    Future<ApiResponse<ReviewDetailResponse>> createReviewDetail(CreateReviewDetailRequest req);
    Future<ApiResponse<ReviewDetailResponse>> updateReviewDetail(UpdateReviewDetailRequest req);
    Future<ApiResponse<ReviewDetailResponseDeleteAt>> trashReviewDetail(Integer reviewDetailId);
    Future<ApiResponse<ReviewDetailResponseDeleteAt>> restoreReviewDetail(Integer reviewDetailId);
    Future<ApiResponse<Void>> deleteReviewDetailPermanently(Integer reviewDetailId);
    Future<ApiResponse<Void>> restoreAllReviewDetails();
    Future<ApiResponse<Void>> deleteAllPermanentReviewDetails();
}
