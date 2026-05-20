package io.example.review_detail.service;

import java.util.List;
import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.review_detail.model.FindAllReview;
import io.example.review_detail.model.ReviewDetailResponse;
import io.example.review_detail.model.ReviewDetailResponseDeleteAt;
import io.vertx.core.Future;

public interface ReviewDetailQueryService {
    Future<ApiResponsePagination<List<ReviewDetailResponse>>> getAllReviewDetails(FindAllReview req);
    Future<ApiResponsePagination<List<ReviewDetailResponseDeleteAt>>> getActiveReviewDetails(FindAllReview req);
    Future<ApiResponsePagination<List<ReviewDetailResponseDeleteAt>>> getTrashedReviewDetails(FindAllReview req);
    Future<ApiResponse<ReviewDetailResponse>> getReviewDetailById(Integer reviewDetailId);
}
