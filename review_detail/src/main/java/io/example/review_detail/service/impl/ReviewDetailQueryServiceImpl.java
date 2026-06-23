package io.example.review_detail.service.impl;

import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.domain.PagedResult;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.review_detail.domain.requests.FindAllReview;
import io.example.review_detail.model.ReviewDetail;
import io.example.review_detail.model.ReviewDetailResponse;
import io.example.review_detail.model.ReviewDetailResponseDeleteAt;
import io.example.review_detail.repository.ReviewDetailQueryRepository;
import io.example.review_detail.service.ReviewDetailQueryService;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReviewDetailQueryServiceImpl implements ReviewDetailQueryService {
        private final ReviewDetailQueryRepository repo;
        private final RedisService redis;
        private final TracingMetrics metrics;
        private static final ObjectMapper mapper = new ObjectMapper();
        private static final Duration CACHE_TTL = Duration.ofMinutes(10);

        private PagedResult<ReviewDetailResponse> mapPagination(PagedResult<ReviewDetail> res) {
                List<ReviewDetailResponse> data = res.getData().stream().map(ReviewDetailResponse::from).toList();
                return new PagedResult<>(data, res.getTotalRecords());
        }

        private PagedResult<ReviewDetailResponseDeleteAt> mapPaginationDeleteAt(PagedResult<ReviewDetail> res) {
                List<ReviewDetailResponseDeleteAt> data = res.getData().stream().map(ReviewDetailResponseDeleteAt::from)
                                .toList();
                return new PagedResult<>(data, res.getTotalRecords());
        }

        @Override
        public Future<PagedResult<ReviewDetailResponse>> getAllReviewDetails(FindAllReview req) {
                var ctx = metrics.startSpan("ReviewDetailQueryService.getAllReviewDetails");
                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                req.setPage(page);
                req.setPageSize(pageSize);
                req.setSearch(keyword);

                String cacheKey = String.format("review_details:page:%d:search:%s", page, keyword);

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<ReviewDetail> result = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<ReviewDetail>>() {
                                                                        });
                                                        return Future.succeededFuture(mapPagination(result));
                                                } catch (Exception e) {
                                                }
                                        }
                                        return repo.getReviewDetails(req)
                                                        .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL)
                                                                        .map(v -> res))
                                                        .map(this::mapPagination);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getAll", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getAll", e.getMessage()));
        }

        @Override
        public Future<PagedResult<ReviewDetailResponseDeleteAt>> getActiveReviewDetails(FindAllReview req) {
                var ctx = metrics.startSpan("ReviewDetailQueryService.getActiveReviewDetails");
                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                req.setPage(page);
                req.setPageSize(pageSize);
                req.setSearch(keyword);

                String cacheKey = String.format("review_details:active:page:%d:search:%s", page, keyword);

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<ReviewDetail> result = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<ReviewDetail>>() {
                                                                        });
                                                        return Future.succeededFuture(mapPaginationDeleteAt(result));
                                                } catch (Exception e) {
                                                }
                                        }
                                        return repo.getReviewDetailsActive(req)
                                                        .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL)
                                                                        .map(v -> res))
                                                        .map(this::mapPaginationDeleteAt);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getActive", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getActive", e.getMessage()));
        }

        @Override
        public Future<PagedResult<ReviewDetailResponseDeleteAt>> getTrashedReviewDetails(FindAllReview req) {
                var ctx = metrics.startSpan("ReviewDetailQueryService.getTrashedReviewDetails");
                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                req.setPage(page);
                req.setPageSize(pageSize);
                req.setSearch(keyword);

                String cacheKey = String.format("review_details:trashed:page:%d:search:%s", page, keyword);

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<ReviewDetail> result = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<ReviewDetail>>() {
                                                                        });
                                                        return Future.succeededFuture(mapPaginationDeleteAt(result));
                                                } catch (Exception e) {
                                                }
                                        }
                                        return repo.getReviewDetailsTrashed(req)
                                                        .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL)
                                                                        .map(v -> res))
                                                        .map(this::mapPaginationDeleteAt);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTrashed", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getTrashed", e.getMessage()));
        }

        @Override
        public Future<ReviewDetailResponse> getReviewDetailById(Long reviewDetailId) {
                var ctx = metrics.startSpan("ReviewDetailQueryService.getReviewDetailById");
                String cacheKey = "review_detail:" + reviewDetailId;

                return redis.getJson(cacheKey, ReviewDetail.class)
                                .compose(cached -> {
                                        if (cached != null) {
                                                return Future.succeededFuture(ReviewDetailResponse.from(cached));
                                        }
                                        return repo.getReviewDetail(reviewDetailId)
                                                        .compose(db -> {
                                                                if (db == null) {
                                                                        return Future.<ReviewDetail>failedFuture(
                                                                                        new NotFoundException(
                                                                                                        "Review detail not found"));
                                                                }
                                                                return redis.setJson(cacheKey, db,
                                                                                Duration.ofMinutes(60))
                                                                                .<ReviewDetail>map(v -> db);
                                                        })
                                                        .map(ReviewDetailResponse::from);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getById", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getById", e.getMessage()));
        }
}