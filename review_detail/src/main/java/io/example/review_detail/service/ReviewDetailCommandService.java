package io.example.review_detail.service;

import io.example.review_detail.domain.requests.CreateReviewDetailRequest;
import io.example.review_detail.domain.requests.UpdateReviewDetailRequest;
import io.example.review_detail.model.ReviewDetailResponse;
import io.example.review_detail.model.ReviewDetailResponseDeleteAt;
import io.vertx.core.Future;

public interface ReviewDetailCommandService {
    Future<ReviewDetailResponse> createReviewDetail(CreateReviewDetailRequest req);

    Future<ReviewDetailResponse> updateReviewDetail(UpdateReviewDetailRequest req);

    Future<ReviewDetailResponseDeleteAt> trashReviewDetail(Long reviewDetailId);

    Future<ReviewDetailResponseDeleteAt> restoreReviewDetail(Long reviewDetailId);

    Future<Void> deleteReviewDetailPermanently(Long reviewDetailId);

    Future<Void> restoreAllReviewDetails();

    Future<Void> deleteAllPermanentReviewDetails();
}