package io.example.review_detail.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.review_detail.model.CreateReviewDetailRequest;
import io.example.review_detail.model.ReviewDetailResponse;
import io.example.review_detail.model.ReviewDetailResponseDeleteAt;
import io.example.review_detail.model.UpdateReviewDetailRequest;
import io.example.review_detail.repository.ReviewDetailCommandRepository;
import io.example.review_detail.repository.ReviewQueryRepository;
import io.example.review_detail.service.ReviewDetailCommandService;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;

public class ReviewDetailCommandServiceImpl implements ReviewDetailCommandService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewDetailCommandServiceImpl.class);
    private final ReviewDetailCommandRepository repo;
    private final ReviewQueryRepository reviewRepo;
    private final RedisService redisService;
    private final TracingMetrics tracingMetrics;

    public ReviewDetailCommandServiceImpl(
            ReviewDetailCommandRepository repo,
            ReviewQueryRepository reviewRepo,
            RedisService redisService,
            TracingMetrics tracingMetrics) {
        this.repo = repo;
        this.reviewRepo = reviewRepo;
        this.redisService = redisService;
        this.tracingMetrics = tracingMetrics;
    }

    @Override
    public Future<ApiResponse<ReviewDetailResponse>> createReviewDetail(CreateReviewDetailRequest req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ReviewDetailCommandService.createReviewDetail",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("review_detail.review_id", req.getReviewId())
                        .build());
        Span span = Span.fromContext(tracingContext.getContext());

        logger.info("Creating review detail for review: {}", req.getReviewId());

        return reviewRepo.exists(req.getReviewId().intValue())
                .compose(exists -> {
                    if (!exists) {
                        return Future.failedFuture(new RuntimeException("Review not found with id: " + req.getReviewId()));
                    }
                    return repo.createReviewDetail(req);
                })
                .map(created -> {
                    span.setAttribute("review_detail.id", created.getReviewDetailId());
                    tracingMetrics.completeSpanSuccess(tracingContext, "create", "Review detail created successfully");
                    return ApiResponse.success("Review detail created successfully", ReviewDetailResponse.from(created));
                })
                .recover(err -> {
                    logger.error("Failed to create review detail", err);
                    tracingMetrics.completeSpanError(tracingContext, "create", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to create review detail: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<ReviewDetailResponse>> updateReviewDetail(UpdateReviewDetailRequest req) {
        Integer detailId = req.getReviewDetailId().intValue();
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ReviewDetailCommandService.updateReviewDetail",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("review_detail.id", detailId)
                        .build());

        logger.info("Updating review detail: {}", detailId);

        return repo.updateReviewDetail(req)
                .compose(updated -> {
                    if (updated == null) {
                        return Future.failedFuture(new RuntimeException("Review detail not found or already deleted"));
                    }
                    String cacheKey = "review_detail:" + detailId;
                    return redisService.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) logger.debug("Review detail {} cache invalidated", detailId);
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for review detail {}: {}", detailId, err.getMessage()))
                            .map(updated);
                })
                .map(updated -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "update", "Review detail updated successfully");
                    return ApiResponse.success("Review detail updated successfully", ReviewDetailResponse.from(updated));
                })
                .recover(err -> {
                    logger.error("Failed to update review detail: {}", detailId, err);
                    tracingMetrics.completeSpanError(tracingContext, "update", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to update review detail: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<ReviewDetailResponseDeleteAt>> trashReviewDetail(Integer reviewDetailId) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ReviewDetailCommandService.trashReviewDetail",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("review_detail.id", reviewDetailId)
                        .build());

        logger.info("Trashing review detail: {}", reviewDetailId);

        return repo.trashReviewDetail(reviewDetailId)
                .compose(trashed -> {
                    if (trashed == null) {
                         return Future.failedFuture(new RuntimeException("Review detail not found or already trashed"));
                    }
                    String cacheKey = "review_detail:" + reviewDetailId;
                    return redisService.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) logger.debug("Review detail {} cache invalidated on trash", reviewDetailId);
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for trashed review detail {}: {}", reviewDetailId, err.getMessage()))
                            .map(trashed);
                })
                .map(trashed -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "trashed", "Review detail trashed successfully");
                    return ApiResponse.success("Review detail trashed successfully", ReviewDetailResponseDeleteAt.from(trashed));
                })
                .recover(err -> {
                    logger.error("Failed to trash review detail: {}", reviewDetailId, err);
                    tracingMetrics.completeSpanError(tracingContext, "trashed", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to trash review detail: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<ReviewDetailResponseDeleteAt>> restoreReviewDetail(Integer reviewDetailId) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ReviewDetailCommandService.restoreReviewDetail",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("review_detail.id", reviewDetailId)
                        .build());

        logger.info("Restoring review detail: {}", reviewDetailId);

        return repo.restoreReviewDetail(reviewDetailId)
                .compose(restored -> {
                    if (restored == null) {
                        return Future.failedFuture(new RuntimeException("Review detail not found or not trashed"));
                    }
                    String cacheKey = "review_detail:" + reviewDetailId;
                    return redisService.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) logger.debug("Review detail {} cache invalidated on restore", reviewDetailId);
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for restored review detail {}: {}", reviewDetailId, err.getMessage()))
                            .map(restored);
                })
                .map(restored -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "restore", "Review detail restored successfully");
                    return ApiResponse.success("Review detail restored successfully", ReviewDetailResponseDeleteAt.from(restored));
                })
                .recover(err -> {
                    logger.error("Failed to restore review detail: {}", reviewDetailId, err);
                    tracingMetrics.completeSpanError(tracingContext, "restore", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to restore review detail: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deleteReviewDetailPermanently(Integer reviewDetailId) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ReviewDetailCommandService.deleteReviewDetailPermanently",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("review_detail.id", reviewDetailId)
                        .build());

        logger.info("Permanently deleting review detail: {}", reviewDetailId);

        return repo.deletePermanentReviewDetail(reviewDetailId)
                .compose(v -> {
                    String cacheKey = "review_detail:" + reviewDetailId;
                    return redisService.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) logger.debug("Review detail {} cache invalidated on permanent delete", reviewDetailId);
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for deleted review detail {}: {}", reviewDetailId, err.getMessage()))
                            .map(v);
                })
                .map(v -> {
                    logger.info("Review detail deleted successfully: {}", reviewDetailId);
                    tracingMetrics.completeSpanSuccess(tracingContext, "deletePermanent", "Review detail deleted permanently");
                    return ApiResponse.<Void>success("success", null);
                })
                .recover(throwable -> {
                    logger.error("Failed to deletePermanent review detail: {}", reviewDetailId, throwable);
                    tracingMetrics.completeSpanError(tracingContext, "deletePermanent", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to delete review detail: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> restoreAllReviewDetails() {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ReviewDetailCommandService.restoreAllReviewDetails");

        logger.info("Restoring all trashed review details");

        return repo.restoreAllReviewDetails()
                .map(v -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "restore_all", "All review details restored successfully");
                    return ApiResponse.<Void>success("All review details restored successfully", null);
                })
                .recover(throwable -> {
                    logger.error("Failed to restore all review details", throwable);
                    tracingMetrics.completeSpanError(tracingContext, "restore_all", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to restore all review details: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deleteAllPermanentReviewDetails() {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ReviewDetailCommandService.deleteAllPermanentReviewDetails");

        logger.info("Permanently deleting all trashed review details");

        return repo.deleteAllPermanentReviewDetails()
                .map(v -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "delete_all_permanent", "All trashed review details deleted permanently");
                    return ApiResponse.<Void>success("All trashed review details deleted permanently", null);
                })
                .recover(throwable -> {
                    logger.error("Failed to delete all permanent review details", throwable);
                    tracingMetrics.completeSpanError(tracingContext, "delete_all_permanent", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to delete all review details: " + throwable.getMessage()));
                });
    }
}
