package io.example.review.service.impl;

import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.domain.PagedResult;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.review.domain.requests.FindAllReview;
import io.example.review.domain.requests.FindAllReviewByMerchant;
import io.example.review.domain.requests.FindAllReviewByProduct;
import io.example.review.model.Review;
import io.example.review.model.ReviewRelationsDetail;
import io.example.review.model.ReviewRelationsDetailResponse;
import io.example.review.model.ReviewResponse;
import io.example.review.model.ReviewResponseDeleteAt;
import io.example.review.repository.ReviewQueryRepository;
import io.example.review.service.ReviewQueryService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewQueryServiceImpl implements ReviewQueryService {
        private final ReviewQueryRepository repository;
        private final RedisService redis;
        private final TracingMetrics metrics;
        private static final ObjectMapper mapper = new ObjectMapper();
        private static final Duration CACHE_TTL = Duration.ofMinutes(10);

        private PagedResult<ReviewResponse> mapPagination(PagedResult<Review> res) {
                List<ReviewResponse> data = res.getData().stream().map(ReviewResponse::from).toList();
                return new PagedResult<>(data, res.getTotalRecords());
        }

        private PagedResult<ReviewResponseDeleteAt> mapPaginationDeleteAt(PagedResult<Review> res) {
                List<ReviewResponseDeleteAt> data = res.getData().stream().map(ReviewResponseDeleteAt::from).toList();
                return new PagedResult<>(data, res.getTotalRecords());
        }

        private PagedResult<ReviewRelationsDetailResponse> mapPaginationRelations(
                        PagedResult<ReviewRelationsDetail> res) {
                List<ReviewRelationsDetailResponse> data = res.getData().stream()
                                .map(ReviewRelationsDetailResponse::from).toList();
                return new PagedResult<>(data, res.getTotalRecords());
        }

        @Override
        public Future<PagedResult<ReviewResponse>> getAllReviews(FindAllReview req) {
                var ctx = metrics.startSpan("ReviewQueryService.getAllReviews");
                int page = req.getPage() > 0 ? req.getPage() : 1;
                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                String search = req.getSearch() != null ? req.getSearch() : "";

                String cacheKey = "review:list:all:" + search + ":" + page + ":" + pageSize;

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<Review> result = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<Review>>() {
                                                                        });
                                                        return Future.succeededFuture(mapPagination(result));
                                                } catch (Exception e) {
                                                }
                                        }
                                        FindAllReview repoReq = FindAllReview.builder().search(search).page(page)
                                                        .pageSize(pageSize).build();
                                        return repository.getReviews(repoReq)
                                                        .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL)
                                                                        .map(v -> res))
                                                        .map(this::mapPagination);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getAllReviews", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getAllReviews", e.getMessage()));
        }

        @Override
        public Future<PagedResult<ReviewResponseDeleteAt>> getActiveReviews(FindAllReview req) {
                var ctx = metrics.startSpan("ReviewQueryService.getActiveReviews");
                int page = req.getPage() > 0 ? req.getPage() : 1;
                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                String search = req.getSearch() != null ? req.getSearch() : "";

                String cacheKey = "review:list:active:" + search + ":" + page + ":" + pageSize;

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<Review> result = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<Review>>() {
                                                                        });
                                                        return Future.succeededFuture(mapPaginationDeleteAt(result));
                                                } catch (Exception e) {
                                                }
                                        }
                                        FindAllReview repoReq = FindAllReview.builder().search(search).page(page)
                                                        .pageSize(pageSize).build();
                                        return repository.getReviewsActive(repoReq)
                                                        .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL)
                                                                        .map(v -> res))
                                                        .map(this::mapPaginationDeleteAt);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getActiveReviews", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getActiveReviews", e.getMessage()));
        }

        @Override
        public Future<PagedResult<ReviewResponseDeleteAt>> getTrashedReviews(FindAllReview req) {
                var ctx = metrics.startSpan("ReviewQueryService.getTrashedReviews");
                int page = req.getPage() > 0 ? req.getPage() : 1;
                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                String search = req.getSearch() != null ? req.getSearch() : "";

                String cacheKey = "review:list:trashed:" + search + ":" + page + ":" + pageSize;

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<Review> result = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<Review>>() {
                                                                        });
                                                        return Future.succeededFuture(mapPaginationDeleteAt(result));
                                                } catch (Exception e) {
                                                }
                                        }
                                        FindAllReview repoReq = FindAllReview.builder().search(search).page(page)
                                                        .pageSize(pageSize).build();
                                        return repository.getReviewsTrashed(repoReq)
                                                        .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL)
                                                                        .map(v -> res))
                                                        .map(this::mapPaginationDeleteAt);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTrashedReviews", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getTrashedReviews", e.getMessage()));
        }

        @Override
        public Future<PagedResult<ReviewRelationsDetailResponse>> getReviewByProduct(FindAllReviewByProduct req) {
                var ctx = metrics.startSpan("ReviewQueryService.getReviewByProduct");
                int page = req.getPage() != null && req.getPage() > 0 ? req.getPage() : 1;
                int pageSize = req.getPageSize() != null && req.getPageSize() > 0 ? req.getPageSize() : 10;

                String cacheKey = "review:product:" + req.getProductId() + ":"
                                + (req.getRating() != null ? req.getRating() : "all") + ":" + page + ":" + pageSize;

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<ReviewRelationsDetail> result = mapper.readValue(
                                                                        jsonStr,
                                                                        new TypeReference<PagedResult<ReviewRelationsDetail>>() {
                                                                        });
                                                        return Future.succeededFuture(mapPaginationRelations(result));
                                                } catch (Exception e) {
                                                }
                                        }
                                        FindAllReviewByProduct repoReq = FindAllReviewByProduct.builder()
                                                        .productId(req.getProductId()).rating(req.getRating())
                                                        .page(page).pageSize(pageSize).build();
                                        return repository.getReviewByProduct(repoReq)
                                                        .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL)
                                                                        .map(v -> res))
                                                        .map(this::mapPaginationRelations);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getReviewByProduct", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getReviewByProduct", e.getMessage()));
        }

        @Override
        public Future<PagedResult<ReviewRelationsDetailResponse>> getReviewByMerchant(FindAllReviewByMerchant req) {
                var ctx = metrics.startSpan("ReviewQueryService.getReviewByMerchant");
                int page = req.getPage() != null && req.getPage() > 0 ? req.getPage() : 1;
                int pageSize = req.getPageSize() != null && req.getPageSize() > 0 ? req.getPageSize() : 10;

                String cacheKey = "review:merchant:" + req.getMerchantId() + ":"
                                + (req.getRating() != null ? req.getRating() : "all") + ":" + page + ":" + pageSize;

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<ReviewRelationsDetail> result = mapper.readValue(
                                                                        jsonStr,
                                                                        new TypeReference<PagedResult<ReviewRelationsDetail>>() {
                                                                        });
                                                        return Future.succeededFuture(mapPaginationRelations(result));
                                                } catch (Exception e) {
                                                }
                                        }
                                        FindAllReviewByMerchant repoReq = FindAllReviewByMerchant.builder()
                                                        .merchantId(req.getMerchantId()).rating(req.getRating())
                                                        .page(page).pageSize(pageSize).build();
                                        return repository.getReviewByMerchantId(repoReq)
                                                        .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL)
                                                                        .map(v -> res))
                                                        .map(this::mapPaginationRelations);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getReviewByMerchant", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getReviewByMerchant", e.getMessage()));
        }

        @Override
        public Future<ReviewResponse> getReviewById(Long reviewId) {
                var ctx = metrics.startSpan("ReviewQueryService.getReviewById",
                                Attributes.builder().put("review.id", reviewId).build());
                String key = "review:" + reviewId;

                return redis.getJson(key, Review.class)
                                .compose(cached -> {
                                        if (cached != null) {
                                                return Future.succeededFuture(ReviewResponse.from(cached));
                                        }
                                        return repository.getReviewById(reviewId)
                                                        .compose(db -> {
                                                                if (db == null) {
                                                                        return Future.<Review>failedFuture(
                                                                                        new NotFoundException(
                                                                                                        "Review not found"));
                                                                }
                                                                return redis.setJson(key, db, Duration.ofMinutes(60))
                                                                                .<Review>map(v -> db);
                                                        })
                                                        .map(ReviewResponse::from);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getReviewById", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getReviewById", e.getMessage()));
        }
}