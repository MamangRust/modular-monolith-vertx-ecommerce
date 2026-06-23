package io.example.review_detail.service.impl;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.review_detail.domain.requests.CreateReviewDetailRequest;
import io.example.review_detail.domain.requests.UpdateReviewDetailRequest;
import io.example.review_detail.model.ReviewDetailResponse;
import io.example.review_detail.model.ReviewDetailResponseDeleteAt;
import io.example.review_detail.repository.ReviewDetailCommandRepository;
import io.example.review_detail.repository.ReviewDetailQueryRepository;
import io.example.review_detail.repository.ReviewQueryRepository;
import io.example.review_detail.service.ReviewDetailCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewDetailCommandServiceImpl implements ReviewDetailCommandService {
        private final ReviewDetailCommandRepository repo;
        private final ReviewDetailQueryRepository queryRepository;
        private final ReviewQueryRepository reviewRepo;
        private final RedisService redis;
        private final TracingMetrics metrics;

        private static final String CACHE_PREFIX = "review_detail:";

        private Future<Void> evict(Integer id) {
                return redis.delete(CACHE_PREFIX + id).<Void>mapEmpty();
        }

        private Future<Void> evictAll() {
                return redis.deleteByPattern(CACHE_PREFIX + "*").<Void>mapEmpty();
        }

        @Override
        public Future<ReviewDetailResponse> createReviewDetail(CreateReviewDetailRequest req) {
                var ctx = metrics.startSpan("ReviewDetailCommandService.createReviewDetail",
                                Attributes.builder().put("review_detail.review_id", req.getReviewId()).build());

                return reviewRepo.exists(req.getReviewId().intValue())
                                .compose(exists -> {
                                        if (!exists) {
                                                return Future.failedFuture(new NotFoundException(
                                                                "Review not found with id: " + req.getReviewId()));
                                        }
                                        return repo.createReviewDetail(req);
                                })
                                .map(ReviewDetailResponse::from)
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "create",
                                                "Review detail created successfully"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "create", e.getMessage()));
        }

        @Override
        public Future<ReviewDetailResponse> updateReviewDetail(UpdateReviewDetailRequest req) {
                Integer detailId = req.getReviewDetailId().intValue();
                var ctx = metrics.startSpan("ReviewDetailCommandService.updateReviewDetail",
                                Attributes.builder().put("review_detail.id", detailId).build());

                return repo.updateReviewDetail(req)
                                .compose(updated -> {
                                        if (updated == null) {
                                                return Future.failedFuture(
                                                                new NotFoundException("Review detail not found"));
                                        }
                                        return evict(detailId).map(v -> updated);
                                })
                                .map(ReviewDetailResponse::from)
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "update",
                                                "Review detail updated successfully"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "update", e.getMessage()));
        }

        @Override
        public Future<ReviewDetailResponseDeleteAt> trashReviewDetail(Long reviewDetailId) {
                var ctx = metrics.startSpan("ReviewDetailCommandService.trashReviewDetail",
                                Attributes.builder().put("review_detail.id", reviewDetailId).build());

                return repo.trashReviewDetail(reviewDetailId)
                                .compose(trashed -> {
                                        if (trashed == null) {
                                                return Future.failedFuture(
                                                                new NotFoundException("Review detail not found"));
                                        }
                                        return evict(reviewDetailId.intValue()).map(v -> trashed);
                                })
                                .map(ReviewDetailResponseDeleteAt::from)
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "trash",
                                                "Review detail trashed successfully"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "trash", e.getMessage()));
        }

        @Override
        public Future<ReviewDetailResponseDeleteAt> restoreReviewDetail(Long reviewDetailId) {
                var ctx = metrics.startSpan("ReviewDetailCommandService.restoreReviewDetail",
                                Attributes.builder().put("review_detail.id", reviewDetailId).build());

                return queryRepository.findByTrashedId(reviewDetailId)
                                .compose(trashed -> {
                                        if (trashed == null) {
                                                return Future.failedFuture(new NotFoundException(
                                                                "Review detail not found or not in trashed state"));
                                        }
                                        return repo.restoreReviewDetail(reviewDetailId)
                                                        .compose(restored -> {
                                                                if (restored == null) {
                                                                        return Future.failedFuture(
                                                                                        new NotFoundException(
                                                                                                        "Review detail not found or not trashed"));
                                                                }
                                                                return evict(reviewDetailId.intValue())
                                                                                .map(v -> restored);
                                                        });
                                })
                                .map(ReviewDetailResponseDeleteAt::from)
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore",
                                                "Review detail restored successfully"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "restore", e.getMessage()));
        }

        @Override
        public Future<Void> deleteReviewDetailPermanently(Long reviewDetailId) {
                var ctx = metrics.startSpan("ReviewDetailCommandService.deletePermanent",
                                Attributes.builder().put("review_detail.id", reviewDetailId).build());

                return queryRepository.findByTrashedId(reviewDetailId)
                                .compose(trashed -> {
                                        if (trashed == null) {
                                                return Future.<Void>failedFuture(
                                                                new BadRequestException(
                                                                                "Review detail not found or must be trashed before permanent deletion"));
                                        }
                                        return repo.deletePermanentReviewDetail(reviewDetailId)
                                                        .compose(v -> evictAll());
                                })
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deletePermanent",
                                                "Review detail deleted permanently"))
                                .onFailure(err -> metrics.completeSpanError(ctx, "deletePermanent", err.getMessage()));
        }

        @Override
        public Future<Void> restoreAllReviewDetails() {
                var ctx = metrics.startSpan("ReviewDetailCommandService.restoreAll");

                return repo.restoreAllReviewDetails()
                                .compose(count -> {
                                        if (count == 0) {
                                                return Future.<Void>failedFuture(new NotFoundException(
                                                                "No trashed review details found"));
                                        }
                                        return evictAll();
                                })
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore_all",
                                                "All review details restored"))
                                .onFailure(err -> metrics.completeSpanError(ctx, "restore_all", err.getMessage()));
        }

        @Override
        public Future<Void> deleteAllPermanentReviewDetails() {
                var ctx = metrics.startSpan("ReviewDetailCommandService.deleteAllPermanent");

                return repo.deleteAllPermanentReviewDetails()
                                .compose(count -> {
                                        if (count == 0) {
                                                return Future.<Void>failedFuture(new NotFoundException(
                                                                "No trashed review details found"));
                                        }
                                        return evictAll();
                                })
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "delete_all_permanent",
                                                "All review details deleted permanently"))
                                .onFailure(err -> metrics.completeSpanError(ctx, "delete_all_permanent",
                                                err.getMessage()));
        }
}