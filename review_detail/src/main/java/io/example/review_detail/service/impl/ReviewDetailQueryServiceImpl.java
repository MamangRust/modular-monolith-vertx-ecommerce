package io.example.review_detail.service.impl;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.example.common.domain.PagedResult;
import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.common.model.PaginationMeta;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.review_detail.model.FindAllReview;
import io.example.review_detail.model.ReviewDetail;
import io.example.review_detail.model.ReviewDetailResponse;
import io.example.review_detail.model.ReviewDetailResponseDeleteAt;
import io.example.review_detail.repository.ReviewDetailQueryRepository;
import io.example.review_detail.service.ReviewDetailQueryService;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class ReviewDetailQueryServiceImpl implements ReviewDetailQueryService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewDetailQueryServiceImpl.class);
    private final ReviewDetailQueryRepository repo;
    private final RedisService redisService;
    private final TracingMetrics tracingMetrics;
    private final ObjectMapper mapper = new ObjectMapper();

    public ReviewDetailQueryServiceImpl(
            ReviewDetailQueryRepository repo,
            RedisService redisService,
            TracingMetrics tracingMetrics) {
        this.repo = repo;
        this.redisService = redisService;
        this.tracingMetrics = tracingMetrics;
    }

    @Override
    public Future<ApiResponsePagination<List<ReviewDetailResponse>>> getAllReviewDetails(FindAllReview req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ReviewDetailQueryService.getAllReviewDetails");
        Span span = Span.fromContext(tracingContext.getContext());

        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        logger.info("Fetching review details | search={}, page={}, pageSize={}", keyword, page, pageSize);

        String cacheKey = String.format("review_details:page:%d:search:%s", page, keyword);

        return redisService.get(cacheKey)
                .<ApiResponsePagination<List<ReviewDetailResponse>>>compose(cachedResult -> {
                    if (cachedResult != null && !cachedResult.isEmpty()) {
                        logger.info("Review details cache hit for key: {}", cacheKey);
                        span.setAttribute("cache.hit", true);
                        try {
                            PagedResult<ReviewDetail> result = mapper.readValue(
                                    cachedResult,
                                    new TypeReference<PagedResult<ReviewDetail>>() {
                                    });

                            ApiResponsePagination<List<ReviewDetailResponse>> response = mapReviewDetailPagination(result, req, "Review details fetched successfully (from cache)");
                            return Future.succeededFuture(response);
                        } catch (Exception e) {
                            logger.warn("Failed to parse review details cache: {}", e.getMessage());
                        }
                    }

                    span.setAttribute("cache.hit", false);
                    return repo.getReviewDetails(req)
                            .map(result -> {
                                redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                                        .onFailure(err -> logger.warn("Failed to set review details cache: {}", err.getMessage()));

                                return mapReviewDetailPagination(result, req, "Review details fetched successfully");
                            });
                })
                .map(response -> {
                    span.setAttribute("review_details.count", response.data().size());
                    span.setAttribute("review_details.total_records", response.pagination().totalRecords());
                    tracingMetrics.completeSpanSuccess(tracingContext, "get_all", "Review details fetched successfully");
                    return response;
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch review details", throwable);
                    tracingMetrics.completeSpanError(tracingContext, "get_all", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch review details: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<ReviewDetailResponseDeleteAt>>> getActiveReviewDetails(FindAllReview req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ReviewDetailQueryService.getActiveReviewDetails");
        Span span = Span.fromContext(tracingContext.getContext());

        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        logger.info("Fetching active review details | search={}, page={}, pageSize={}", keyword, page, pageSize);

        String cacheKey = String.format("review_details:active:page:%d:search:%s", page, keyword);

        return redisService.get(cacheKey)
                .<ApiResponsePagination<List<ReviewDetailResponseDeleteAt>>>compose(cachedResult -> {
                    if (cachedResult != null && !cachedResult.isEmpty()) {
                        logger.info("Active review details cache hit for key: {}", cacheKey);
                        span.setAttribute("cache.hit", true);
                        try {
                            PagedResult<ReviewDetail> result = mapper.readValue(
                                    cachedResult,
                                    new TypeReference<PagedResult<ReviewDetail>>() {
                                    });

                            ApiResponsePagination<List<ReviewDetailResponseDeleteAt>> response = mapReviewDetailPaginationDeleteAt(result, req, "Active review details fetched successfully (from cache)");
                            return Future.succeededFuture(response);
                        } catch (Exception e) {
                            logger.warn("Failed to parse active review details cache: {}", e.getMessage());
                        }
                    }

                    span.setAttribute("cache.hit", false);
                    return repo.getReviewDetailsActive(req)
                            .map(result -> {
                                redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                                        .onFailure(err -> logger.warn("Failed to set active review details cache: {}", err.getMessage()));

                                return mapReviewDetailPaginationDeleteAt(result, req, "Active review details fetched successfully");
                            });
                })
                .map(response -> {
                    span.setAttribute("review_detail.count", response.data().size());
                    span.setAttribute("review_detail.total_records", response.pagination().totalRecords());
                    tracingMetrics.completeSpanSuccess(tracingContext, "get_active", "Active review details fetched successfully");
                    return response;
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch active review details", throwable);
                    tracingMetrics.completeSpanError(tracingContext, "get_active", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch active review details: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<ReviewDetailResponseDeleteAt>>> getTrashedReviewDetails(FindAllReview req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ReviewDetailQueryService.getTrashedReviewDetails");
        Span span = Span.fromContext(tracingContext.getContext());

        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        logger.info("Fetching trashed review details | search={}, page={}, pageSize={}", keyword, page, pageSize);

        String cacheKey = String.format("review_details:trashed:page:%d:search:%s", page, keyword);

        return redisService.get(cacheKey)
                .<ApiResponsePagination<List<ReviewDetailResponseDeleteAt>>>compose(cachedResult -> {
                    if (cachedResult != null && !cachedResult.isEmpty()) {
                        logger.info("Trashed review details cache hit for key: {}", cacheKey);
                        span.setAttribute("cache.hit", true);
                        try {
                            PagedResult<ReviewDetail> result = mapper.readValue(
                                    cachedResult,
                                    new TypeReference<PagedResult<ReviewDetail>>() {
                                    });

                            ApiResponsePagination<List<ReviewDetailResponseDeleteAt>> response = mapReviewDetailPaginationDeleteAt(result, req, "Trashed review details fetched successfully (from cache)");
                            return Future.succeededFuture(response);
                        } catch (Exception e) {
                            logger.warn("Failed to parse trashed review details cache: {}", e.getMessage());
                        }
                    }

                    span.setAttribute("cache.hit", false);
                    return repo.getReviewDetailsTrashed(req)
                            .map(result -> {
                                redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                                        .onFailure(err -> logger.warn("Failed to set trashed review details cache: {}", err.getMessage()));

                                return mapReviewDetailPaginationDeleteAt(result, req, "Trashed review details fetched successfully");
                            });
                })
                .map(response -> {
                    span.setAttribute("review_detail.count", response.data().size());
                    span.setAttribute("review_detail.total_records", response.pagination().totalRecords());
                    tracingMetrics.completeSpanSuccess(tracingContext, "get_trashed", "Trashed review details fetched successfully");
                    return response;
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch trashed review details", throwable);
                    tracingMetrics.completeSpanError(tracingContext, "get_trashed", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch trashed review details: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<ReviewDetailResponse>> getReviewDetailById(Integer reviewDetailId) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ReviewDetailQueryService.getReviewDetailById",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("review_detail.id", reviewDetailId)
                        .build());
        Span span = Span.fromContext(tracingContext.getContext());

        logger.info("Fetching review detail by id: {}", reviewDetailId);
        String cacheKey = "review_detail:" + reviewDetailId;

        return redisService.get(cacheKey)
                .compose(cachedDetail -> {
                    if (cachedDetail != null && !cachedDetail.isEmpty()) {
                        logger.info("Review detail {} found in cache", reviewDetailId);
                        span.setAttribute("review_detail.cache_hit", true);
                        try {
                            ReviewDetail detail = ReviewDetail.fromJson(new JsonObject(cachedDetail));
                            tracingMetrics.completeSpanSuccess(tracingContext, "get_by_id", "Review detail fetched from cache");
                            return Future.succeededFuture(ApiResponse.success(
                                    "Review detail fetched successfully (from cache)",
                                    ReviewDetailResponse.from(detail)));
                        } catch (Exception e) {
                            logger.warn("Failed to parse cached review detail data for {}: {}", reviewDetailId, e.getMessage());
                            return fetchReviewDetailFromDatabase(reviewDetailId, tracingContext);
                        }
                    } else {
                        span.setAttribute("review_detail.cache_hit", false);
                        return fetchReviewDetailFromDatabase(reviewDetailId, tracingContext);
                    }
                })
                .recover(err -> {
                    logger.error("Failed to fetch review detail by id: {}", reviewDetailId, err);
                    tracingMetrics.completeSpanError(tracingContext, "get_by_id", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to fetch review detail: " + err.getMessage()));
                });
    }

    private Future<ApiResponse<ReviewDetailResponse>> fetchReviewDetailFromDatabase(Integer reviewDetailId, TracingMetrics.TracingContext tracingContext) {
        Span span = Span.fromContext(tracingContext.getContext());

        return repo.getReviewDetail(reviewDetailId)
                .compose((ReviewDetail detail) -> {
                    if (detail == null) {
                        return Future.failedFuture(new RuntimeException("Review detail not found with id: " + reviewDetailId));
                    }

                    span.setAttribute("review_detail.type", detail.getType());

                    String cacheKey = "review_detail:" + reviewDetailId;
                    redisService.setJson(cacheKey, detail.toJson(), Duration.ofMinutes(60))
                            .onSuccess(v -> logger.debug("Review detail {} cached successfully", reviewDetailId))
                            .onFailure(err -> logger.warn("Failed to cache review detail {}: {}", reviewDetailId, err.getMessage()));

                    return Future.succeededFuture(ApiResponse.success(
                            "Review detail fetched successfully",
                            ReviewDetailResponse.from(detail)));
                });
    }

    private ApiResponsePagination<List<ReviewDetailResponse>> mapReviewDetailPagination(PagedResult<ReviewDetail> result, FindAllReview req, String message) {
        int pageSize = req.getPageSize();
        int totalRecords = result.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<ReviewDetailResponse> data = result.getData().stream().map(ReviewDetailResponse::from).toList();

        return new ApiResponsePagination<>(
                "success",
                message,
                data,
                new PaginationMeta(req.getPage() + 1, pageSize, totalPages, totalRecords));
    }

    private ApiResponsePagination<List<ReviewDetailResponseDeleteAt>> mapReviewDetailPaginationDeleteAt(PagedResult<ReviewDetail> result, FindAllReview req, String message) {
        int pageSize = req.getPageSize();
        int totalRecords = result.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<ReviewDetailResponseDeleteAt> data = result.getData().stream().map(ReviewDetailResponseDeleteAt::from).toList();

        return new ApiResponsePagination<>(
                "success",
                message,
                data,
                new PaginationMeta(req.getPage() + 1, pageSize, totalPages, totalRecords));
    }
}
