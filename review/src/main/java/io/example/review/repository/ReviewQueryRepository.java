package io.example.review.repository;

import io.example.common.domain.PagedResult;
import io.example.review.model.Review;
import io.example.review.model.ReviewRelationsDetail;
import io.example.review.model.FindAllReview;
import io.example.review.model.FindAllReviewByProduct;
import io.example.review.model.FindAllReviewByMerchant;
import io.vertx.core.Future;

public interface ReviewQueryRepository {
    Future<PagedResult<Review>> getReviews(FindAllReview req);
    Future<PagedResult<Review>> getReviewsActive(FindAllReview req);
    Future<PagedResult<Review>> getReviewsTrashed(FindAllReview req);
    Future<PagedResult<ReviewRelationsDetail>> getReviewByProduct(FindAllReviewByProduct req);
    Future<PagedResult<ReviewRelationsDetail>> getReviewByMerchantId(FindAllReviewByMerchant req);
    Future<Review> getReviewById(Long reviewId);
}
