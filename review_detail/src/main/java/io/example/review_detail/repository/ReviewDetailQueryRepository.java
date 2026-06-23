package io.example.review_detail.repository;

import io.example.common.domain.PagedResult;
import io.example.review_detail.domain.requests.FindAllReview;
import io.example.review_detail.model.ReviewDetail;
import io.vertx.core.Future;

public interface ReviewDetailQueryRepository {
    Future<PagedResult<ReviewDetail>> getReviewDetails(FindAllReview req);

    Future<PagedResult<ReviewDetail>> getReviewDetailsActive(FindAllReview req);

    Future<PagedResult<ReviewDetail>> getReviewDetailsTrashed(FindAllReview req);

    Future<ReviewDetail> getReviewDetail(Long reviewDetailId);

    Future<ReviewDetail> findByTrashedId(Long reviewDetailId);
}
