package io.example.review.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.review.model.Review;
import io.example.review.model.ReviewResponse;
import io.example.review.model.ReviewResponseDeleteAt;
import io.example.review.model.CreateReviewRequest;
import io.example.review.model.UpdateReviewRequest;
import io.example.review.repository.ReviewCommandRepository;
import io.example.review.repository.UserQueryRepository;
import io.example.review.repository.ProductQueryRepository;
import io.example.review.service.ReviewCommandService;

import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;

public class ReviewCommandServiceImpl implements ReviewCommandService {
    private static final Logger log = LoggerFactory.getLogger(ReviewCommandServiceImpl.class);
    private final ReviewCommandRepository repository;
    private final UserQueryRepository userRepository;
    private final ProductQueryRepository productRepository;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "review:";

    public ReviewCommandServiceImpl(
            ReviewCommandRepository repository,
            UserQueryRepository userRepository,
            ProductQueryRepository productRepository,
            RedisService redis,
            TracingMetrics metrics) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.redis = redis;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponse<ReviewResponse>> createReview(CreateReviewRequest req) {
        var ctx = metrics.startSpan("ReviewCommandService.createReview",
                Attributes.builder()
                        .put("review.user_id", (long) req.getUserId())
                        .put("review.product_id", req.getProductId())
                        .build());

        return userRepository.findById(req.getUserId())
                .compose(userExists -> {
                    if (!userExists) {
                        return Future.failedFuture("User not found");
                    }
                    return productRepository.findById(req.getProductId().intValue());
                })
                .compose(productExists -> {
                    if (!productExists) {
                        return Future.failedFuture("Product not found");
                    }
                    return repository.createReview(req);
                })
                .map(created -> {
                    metrics.completeSpanSuccess(ctx, "createReview", "Review created successfully");
                    return ApiResponse.success("Review created successfully", ReviewResponse.from(created));
                })
                .onFailure(e -> metrics.completeSpanError(ctx, "createReview", e.getMessage()))
                .recover(e -> Future.succeededFuture(ApiResponse.error(e.getMessage())));
    }

    @Override
    public Future<ApiResponse<ReviewResponse>> updateReview(UpdateReviewRequest req) {
        Long reviewId = req.getReviewId();
        var ctx = metrics.startSpan("ReviewCommandService.updateReview",
                Attributes.builder().put("review.id", reviewId).build());

        return repository.updateReview(req)
                .compose(updated -> {
                    if (updated == null) {
                        return Future.failedFuture("Review not found or already deleted");
                    }
                    String cacheKey = CACHE_PREFIX + reviewId;
                    return redis.delete(cacheKey)
                            .map(updated);
                })
                .map(updated -> {
                    metrics.completeSpanSuccess(ctx, "updateReview", "Review updated successfully");
                    return ApiResponse.success("Review updated successfully", ReviewResponse.from(updated));
                })
                .onFailure(e -> metrics.completeSpanError(ctx, "updateReview", e.getMessage()))
                .recover(e -> Future.succeededFuture(ApiResponse.error(e.getMessage())));
    }

    @Override
    public Future<ApiResponse<ReviewResponseDeleteAt>> trashReview(Long reviewId) {
        var ctx = metrics.startSpan("ReviewCommandService.trashReview",
                Attributes.builder().put("review.id", reviewId).build());

        return repository.trashReview(reviewId)
                .compose(trashed -> {
                    if (trashed == null) {
                        return Future.failedFuture("Review not found");
                    }
                    String cacheKey = CACHE_PREFIX + reviewId;
                    return redis.delete(cacheKey)
                            .map(trashed);
                })
                .map(trashed -> {
                    metrics.completeSpanSuccess(ctx, "trashReview", "Review trashed successfully");
                    return ApiResponse.success("Review trashed successfully", ReviewResponseDeleteAt.from(trashed));
                })
                .onFailure(e -> metrics.completeSpanError(ctx, "trashReview", e.getMessage()))
                .recover(e -> Future.succeededFuture(ApiResponse.error(e.getMessage())));
    }

    @Override
    public Future<ApiResponse<ReviewResponseDeleteAt>> restoreReview(Long reviewId) {
        var ctx = metrics.startSpan("ReviewCommandService.restoreReview",
                Attributes.builder().put("review.id", reviewId).build());

        return repository.restoreReview(reviewId)
                .compose(restored -> {
                    if (restored == null) {
                        return Future.failedFuture("Review not found");
                    }
                    String cacheKey = CACHE_PREFIX + reviewId;
                    return redis.delete(cacheKey)
                            .map(restored);
                })
                .map(restored -> {
                    metrics.completeSpanSuccess(ctx, "restoreReview", "Review restored successfully");
                    return ApiResponse.success("Review restored successfully", ReviewResponseDeleteAt.from(restored));
                })
                .onFailure(e -> metrics.completeSpanError(ctx, "restoreReview", e.getMessage()))
                .recover(e -> Future.succeededFuture(ApiResponse.error(e.getMessage())));
    }

    @Override
    public Future<ApiResponse<Void>> deleteReviewPermanently(Long reviewId) {
        var ctx = metrics.startSpan("ReviewCommandService.deleteReviewPermanently",
                Attributes.builder().put("review.id", reviewId).build());

        return repository.deleteReviewPermanently(reviewId)
                .compose(v -> {
                    String cacheKey = CACHE_PREFIX + reviewId;
                    return redis.delete(cacheKey);
                })
                .map(v -> {
                    metrics.completeSpanSuccess(ctx, "deleteReviewPermanently", "Review permanently deleted");
                    return ApiResponse.<Void>success("Review deleted permanently", null);
                })
                .onFailure(e -> metrics.completeSpanError(ctx, "deleteReviewPermanently", e.getMessage()))
                .recover(e -> Future.succeededFuture(ApiResponse.error(e.getMessage())));
    }

    @Override
    public Future<ApiResponse<Void>> restoreAllReviews() {
        var ctx = metrics.startSpan("ReviewCommandService.restoreAllReviews");

        return repository.restoreAllReviews()
                .map(count -> {
                    metrics.completeSpanSuccess(ctx, "restoreAllReviews", "All reviews restored successfully");
                    return ApiResponse.<Void>success("All reviews restored successfully", null);
                })
                .onFailure(e -> metrics.completeSpanError(ctx, "restoreAllReviews", e.getMessage()))
                .recover(e -> Future.succeededFuture(ApiResponse.error(e.getMessage())));
    }

    @Override
    public Future<ApiResponse<Void>> deleteAllPermanentReviews() {
        var ctx = metrics.startSpan("ReviewCommandService.deleteAllPermanentReviews");

        return repository.deleteAllPermanentReviews()
                .map(count -> {
                    metrics.completeSpanSuccess(ctx, "deleteAllPermanentReviews", "All trashed reviews deleted permanently");
                    return ApiResponse.<Void>success("All trashed reviews deleted permanently", null);
                })
                .onFailure(e -> metrics.completeSpanError(ctx, "deleteAllPermanentReviews", e.getMessage()))
                .recover(e -> Future.succeededFuture(ApiResponse.error(e.getMessage())));
    }
}
