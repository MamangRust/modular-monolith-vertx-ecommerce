package io.example.review.service.impl;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.review.domain.requests.CreateReviewRequest;
import io.example.review.domain.requests.UpdateReviewRequest;
import io.example.review.model.ReviewResponse;
import io.example.review.model.ReviewResponseDeleteAt;
import io.example.review.repository.ReviewCommandRepository;
import io.example.review.repository.ReviewQueryRepository;
import io.example.review.repository.UserQueryRepository;
import io.example.review.repository.ProductQueryRepository;
import io.example.review.service.ReviewCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewCommandServiceImpl implements ReviewCommandService {
    private final ReviewCommandRepository repository;
    private final ReviewQueryRepository queryRepository;
    private final UserQueryRepository userRepository;
    private final ProductQueryRepository productRepository;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "review:";

    private Future<Void> evict(Long id) {
        return redis.delete(CACHE_PREFIX + id).<Void>mapEmpty();
    }

    private Future<Void> evictAll() {
        return redis.deleteByPattern(CACHE_PREFIX + "list:*").<Void>mapEmpty();
    }

    @Override
    public Future<ReviewResponse> createReview(CreateReviewRequest req) {
        var ctx = metrics.startSpan("ReviewCommandService.createReview",
                Attributes.builder()
                        .put("review.user_id", (long) req.getUserId())
                        .put("review.product_id", req.getProductId())
                        .build());

        return userRepository.findById(req.getUserId())
                .compose(userExists -> {
                    if (!userExists) {
                        return Future.failedFuture(new NotFoundException("User not found"));
                    }
                    return productRepository.findById(req.getProductId().intValue());
                })
                .compose(productExists -> {
                    if (!productExists) {
                        return Future.failedFuture(new NotFoundException("Product not found"));
                    }
                    return repository.createReview(req);
                })
                .map(ReviewResponse::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "createReview", "Review created successfully"))
                .onFailure(e -> metrics.completeSpanError(ctx, "createReview", e.getMessage()));
    }

    @Override
    public Future<ReviewResponse> updateReview(UpdateReviewRequest req) {
        Long reviewId = req.getReviewId();
        var ctx = metrics.startSpan("ReviewCommandService.updateReview",
                Attributes.builder().put("review.id", reviewId).build());

        return repository.updateReview(req)
                .compose(updated -> {
                    if (updated == null) {
                        return Future.failedFuture(new NotFoundException("Review not found"));
                    }
                    return evict(reviewId).map(v -> updated);
                })
                .map(ReviewResponse::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "updateReview", "Review updated successfully"))
                .onFailure(e -> metrics.completeSpanError(ctx, "updateReview", e.getMessage()));
    }

    @Override
    public Future<ReviewResponseDeleteAt> trashReview(Long reviewId) {
        var ctx = metrics.startSpan("ReviewCommandService.trashReview",
                Attributes.builder().put("review.id", reviewId).build());

        return repository.trashReview(reviewId)
                .compose(trashed -> {
                    if (trashed == null) {
                        return Future.failedFuture(new NotFoundException("Review not found"));
                    }
                    return evict(reviewId).map(v -> trashed);
                })
                .map(ReviewResponseDeleteAt::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "trashReview", "Review trashed successfully"))
                .onFailure(e -> metrics.completeSpanError(ctx, "trashReview", e.getMessage()));
    }

    @Override
    public Future<ReviewResponseDeleteAt> restoreReview(Long reviewId) {
        var ctx = metrics.startSpan("ReviewCommandService.restoreReview",
                Attributes.builder().put("review.id", reviewId).build());

        return queryRepository.findByIdTrashed(reviewId)
                .compose(trashed -> {
                    if (trashed == null) {
                        return Future.failedFuture(new NotFoundException("Review not found or not in trashed state"));
                    }
                    return repository.restoreReview(reviewId)
                            .compose(restored -> {
                                if (restored == null) {
                                    return Future.failedFuture(new NotFoundException("Review not found"));
                                }
                                return evict(reviewId).map(v -> restored);
                            });
                })
                .map(ReviewResponseDeleteAt::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restoreReview", "Review restored successfully"))
                .onFailure(e -> metrics.completeSpanError(ctx, "restoreReview", e.getMessage()));
    }

    @Override
    public Future<Void> deleteReviewPermanently(Long reviewId) {
        var ctx = metrics.startSpan("ReviewCommandService.deleteReviewPermanently",
                Attributes.builder().put("review.id", reviewId).build());

        return queryRepository.findByIdTrashed(reviewId)
                .compose(trashed -> {
                    if (trashed == null) {
                        return Future.<Void>failedFuture(
                                new BadRequestException(
                                        "Review not found or must be trashed before permanent deletion"));
                    }
                    return repository.deleteReviewPermanently(reviewId)
                            .compose(v -> evictAll());
                })
                .onSuccess(
                        v -> metrics.completeSpanSuccess(ctx, "deleteReviewPermanently", "Review permanently deleted"))
                .onFailure(e -> metrics.completeSpanError(ctx, "deleteReviewPermanently", e.getMessage()));
    }

    @Override
    public Future<Void> restoreAllReviews() {
        var ctx = metrics.startSpan("ReviewCommandService.restoreAllReviews");

        return repository.restoreAllReviews()
                .compose(count -> {
                    if (count == 0) {
                        return Future.<Void>failedFuture(new NotFoundException("No trashed reviews found"));
                    }
                    return evictAll();
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore_all", "All reviews restored"))
                .onFailure(e -> metrics.completeSpanError(ctx, "restore_all", e.getMessage()));
    }

    @Override
    public Future<Void> deleteAllPermanentReviews() {
        var ctx = metrics.startSpan("ReviewCommandService.deleteAllPermanentReviews");

        return repository.deleteAllPermanentReviews()
                .compose(count -> {
                    if (count == 0) {
                        return Future.<Void>failedFuture(new NotFoundException("No trashed reviews found"));
                    }
                    return evictAll();
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "delete_all_permanent",
                        "All trashed reviews deleted permanently"))
                .onFailure(e -> metrics.completeSpanError(ctx, "delete_all_permanent", e.getMessage()));
    }
}