package io.example.review.service.impl;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.domain.PagedResult;
import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.common.model.PaginationMeta;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.review.model.Review;
import io.example.review.model.ReviewResponse;
import io.example.review.model.ReviewResponseDeleteAt;
import io.example.review.model.ReviewRelationsDetail;
import io.example.review.model.ReviewRelationsDetailResponse;
import io.example.review.model.FindAllReview;
import io.example.review.model.FindAllReviewByProduct;
import io.example.review.model.FindAllReviewByMerchant;
import io.example.review.repository.ReviewQueryRepository;
import io.example.review.service.ReviewQueryService;

import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ReviewQueryServiceImpl implements ReviewQueryService {
    private static final Logger log = LoggerFactory.getLogger(ReviewQueryServiceImpl.class);
    private final ReviewQueryRepository repository;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "review:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    public ReviewQueryServiceImpl(ReviewQueryRepository repository, RedisService redis, TracingMetrics metrics) {
        this.repository = repository;
        this.redis = redis;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponsePagination<List<ReviewResponse>>> getAllReviews(FindAllReview req) {
        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String search = req.getSearch();

        String cacheKey = CACHE_PREFIX + "list:all:" + (search != null ? search : "") + ":" + page + ":" + pageSize;
        var ctx = metrics.startSpan("ReviewQueryService.getAllReviews");

        return redis.get(cacheKey)
                .compose(cached -> {
                    if (cached != null) {
                        JsonObject json = new JsonObject(cached);
                        List<ReviewResponse> data = json.getJsonArray("data").stream()
                                .map(o -> ((JsonObject) o).mapTo(ReviewResponse.class)).toList();
                        PaginationMeta meta = json.getJsonObject("pagination").mapTo(PaginationMeta.class);
                        metrics.completeSpanSuccess(ctx, "getAllReviews", "Success (from cache)");
                        return Future.succeededFuture(
                                new ApiResponsePagination<>("success", "Reviews fetched successfully (from cache)", data, meta));
                    }
                    FindAllReview repoReq = FindAllReview.builder()
                            .search(search)
                            .page(page)
                            .pageSize(pageSize)
                            .build();

                    return repository.getReviews(repoReq)
                            .map(res -> {
                                ApiResponsePagination<List<ReviewResponse>> response = mapPagination(res, page, pageSize);
                                redis.setJson(cacheKey, JsonObject.mapFrom(response), CACHE_TTL);
                                return response;
                            });
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getAllReviews", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getAllReviews", e.getMessage()))
                .recover(e -> Future.succeededFuture(ApiResponsePagination.error(e.getMessage())));
    }

    @Override
    public Future<ApiResponsePagination<List<ReviewResponseDeleteAt>>> getActiveReviews(FindAllReview req) {
        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String search = req.getSearch();

        String cacheKey = CACHE_PREFIX + "list:active:" + (search != null ? search : "") + ":" + page + ":" + pageSize;
        var ctx = metrics.startSpan("ReviewQueryService.getActiveReviews");

        return redis.get(cacheKey)
                .compose(cached -> {
                    if (cached != null) {
                        JsonObject json = new JsonObject(cached);
                        List<ReviewResponseDeleteAt> data = json.getJsonArray("data").stream()
                                .map(o -> ((JsonObject) o).mapTo(ReviewResponseDeleteAt.class)).toList();
                        PaginationMeta meta = json.getJsonObject("pagination").mapTo(PaginationMeta.class);
                        metrics.completeSpanSuccess(ctx, "getActiveReviews", "Success (from cache)");
                        return Future.succeededFuture(
                                new ApiResponsePagination<>("success", "Active reviews fetched successfully (from cache)", data, meta));
                    }
                    FindAllReview repoReq = FindAllReview.builder()
                            .search(search)
                            .page(page)
                            .pageSize(pageSize)
                            .build();

                    return repository.getReviewsActive(repoReq)
                            .map(res -> {
                                ApiResponsePagination<List<ReviewResponseDeleteAt>> response = mapPaginationDeleteAt(res, page, pageSize);
                                redis.setJson(cacheKey, JsonObject.mapFrom(response), CACHE_TTL);
                                return response;
                            });
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getActiveReviews", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getActiveReviews", e.getMessage()))
                .recover(e -> Future.succeededFuture(ApiResponsePagination.error(e.getMessage())));
    }

    @Override
    public Future<ApiResponsePagination<List<ReviewResponseDeleteAt>>> getTrashedReviews(FindAllReview req) {
        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String search = req.getSearch();

        String cacheKey = CACHE_PREFIX + "list:trashed:" + (search != null ? search : "") + ":" + page + ":" + pageSize;
        var ctx = metrics.startSpan("ReviewQueryService.getTrashedReviews");

        return redis.get(cacheKey)
                .compose(cached -> {
                    if (cached != null) {
                        JsonObject json = new JsonObject(cached);
                        List<ReviewResponseDeleteAt> data = json.getJsonArray("data").stream()
                                .map(o -> ((JsonObject) o).mapTo(ReviewResponseDeleteAt.class)).toList();
                        PaginationMeta meta = json.getJsonObject("pagination").mapTo(PaginationMeta.class);
                        metrics.completeSpanSuccess(ctx, "getTrashedReviews", "Success (from cache)");
                        return Future.succeededFuture(
                                new ApiResponsePagination<>("success", "Trashed reviews fetched successfully (from cache)", data, meta));
                    }
                    FindAllReview repoReq = FindAllReview.builder()
                            .search(search)
                            .page(page)
                            .pageSize(pageSize)
                            .build();

                    return repository.getReviewsTrashed(repoReq)
                            .map(res -> {
                                ApiResponsePagination<List<ReviewResponseDeleteAt>> response = mapPaginationDeleteAt(res, page, pageSize);
                                redis.setJson(cacheKey, JsonObject.mapFrom(response), CACHE_TTL);
                                return response;
                            });
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTrashedReviews", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getTrashedReviews", e.getMessage()))
                .recover(e -> Future.succeededFuture(ApiResponsePagination.error(e.getMessage())));
    }

    @Override
    public Future<ApiResponsePagination<List<ReviewRelationsDetailResponse>>> getReviewByProduct(FindAllReviewByProduct req) {
        int page = req.getPage() != null && req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() != null && req.getPageSize() > 0 ? req.getPageSize() : 10;

        String cacheKey = CACHE_PREFIX + "product:" + req.getProductId() + ":" + (req.getRating() != null ? req.getRating() : "all") + ":" + page + ":" + pageSize;
        var ctx = metrics.startSpan("ReviewQueryService.getReviewByProduct");

        return redis.get(cacheKey)
                .compose(cached -> {
                    if (cached != null) {
                        JsonObject json = new JsonObject(cached);
                        List<ReviewRelationsDetailResponse> data = json.getJsonArray("data").stream()
                                .map(o -> ((JsonObject) o).mapTo(ReviewRelationsDetailResponse.class)).toList();
                        PaginationMeta meta = json.getJsonObject("pagination").mapTo(PaginationMeta.class);
                        metrics.completeSpanSuccess(ctx, "getReviewByProduct", "Success (from cache)");
                        return Future.succeededFuture(
                                new ApiResponsePagination<>("success", "Product reviews fetched successfully (from cache)", data, meta));
                    }
                    FindAllReviewByProduct repoReq = FindAllReviewByProduct.builder()
                            .productId(req.getProductId())
                            .rating(req.getRating())
                            .page(page)
                            .pageSize(pageSize)
                            .build();

                    return repository.getReviewByProduct(repoReq)
                            .map(res -> {
                                ApiResponsePagination<List<ReviewRelationsDetailResponse>> response = mapPaginationRelations(res, page, pageSize);
                                redis.setJson(cacheKey, JsonObject.mapFrom(response), CACHE_TTL);
                                return response;
                            });
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getReviewByProduct", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getReviewByProduct", e.getMessage()))
                .recover(e -> Future.succeededFuture(ApiResponsePagination.error(e.getMessage())));
    }

    @Override
    public Future<ApiResponsePagination<List<ReviewRelationsDetailResponse>>> getReviewByMerchant(FindAllReviewByMerchant req) {
        int page = req.getPage() != null && req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() != null && req.getPageSize() > 0 ? req.getPageSize() : 10;

        String cacheKey = CACHE_PREFIX + "merchant:" + req.getMerchantId() + ":" + (req.getRating() != null ? req.getRating() : "all") + ":" + page + ":" + pageSize;
        var ctx = metrics.startSpan("ReviewQueryService.getReviewByMerchant");

        return redis.get(cacheKey)
                .compose(cached -> {
                    if (cached != null) {
                        JsonObject json = new JsonObject(cached);
                        List<ReviewRelationsDetailResponse> data = json.getJsonArray("data").stream()
                                .map(o -> ((JsonObject) o).mapTo(ReviewRelationsDetailResponse.class)).toList();
                        PaginationMeta meta = json.getJsonObject("pagination").mapTo(PaginationMeta.class);
                        metrics.completeSpanSuccess(ctx, "getReviewByMerchant", "Success (from cache)");
                        return Future.succeededFuture(
                                new ApiResponsePagination<>("success", "Merchant reviews fetched successfully (from cache)", data, meta));
                    }
                    FindAllReviewByMerchant repoReq = FindAllReviewByMerchant.builder()
                            .merchantId(req.getMerchantId())
                            .rating(req.getRating())
                            .page(page)
                            .pageSize(pageSize)
                            .build();

                    return repository.getReviewByMerchantId(repoReq)
                            .map(res -> {
                                ApiResponsePagination<List<ReviewRelationsDetailResponse>> response = mapPaginationRelations(res, page, pageSize);
                                redis.setJson(cacheKey, JsonObject.mapFrom(response), CACHE_TTL);
                                return response;
                            });
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getReviewByMerchant", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getReviewByMerchant", e.getMessage()))
                .recover(e -> Future.succeededFuture(ApiResponsePagination.error(e.getMessage())));
    }

    @Override
    public Future<ApiResponse<ReviewResponse>> getReviewById(Long reviewId) {
        var ctx = metrics.startSpan("ReviewQueryService.getReviewById", Attributes.builder().put("review.id", reviewId).build());
        String key = CACHE_PREFIX + reviewId;

        return redis.get(key)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        try {
                            Review review = Review.fromJson(new JsonObject(cached));
                            metrics.completeSpanSuccess(ctx, "getReviewById", "Success (from cache)");
                            return Future.succeededFuture(
                                    ApiResponse.success("Review fetched successfully (from cache)", ReviewResponse.from(review)));
                        } catch (Exception ex) {
                            log.warn("Failed parsing cached review {}", reviewId, ex);
                        }
                    }
                    return repository.getReviewById(reviewId)
                            .compose(db -> {
                                if (db == null)
                                    return Future.failedFuture("Review not found");
                                redis.setJson(key, db.toJson(), CACHE_TTL);
                                return Future.succeededFuture(ApiResponse.success("Review fetched successfully", ReviewResponse.from(db)));
                            });
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getReviewById", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getReviewById", e.getMessage()))
                .recover(e -> Future.succeededFuture(ApiResponse.error(e.getMessage())));
    }

    private ApiResponsePagination<List<ReviewResponse>> mapPagination(PagedResult<Review> res, int page, int pageSize) {
        int totalRecords = res.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<ReviewResponse> data = res.getData().stream().map(ReviewResponse::from).toList();
        return ApiResponsePagination.success("Reviews fetched successfully", data,
                new PaginationMeta(page, pageSize, totalPages, totalRecords));
    }

    private ApiResponsePagination<List<ReviewResponseDeleteAt>> mapPaginationDeleteAt(PagedResult<Review> res, int page, int pageSize) {
        int totalRecords = res.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<ReviewResponseDeleteAt> data = res.getData().stream().map(ReviewResponseDeleteAt::from).toList();
        return ApiResponsePagination.success("Reviews fetched successfully", data,
                new PaginationMeta(page, pageSize, totalPages, totalRecords));
    }

    private ApiResponsePagination<List<ReviewRelationsDetailResponse>> mapPaginationRelations(PagedResult<ReviewRelationsDetail> res, int page, int pageSize) {
        int totalRecords = res.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<ReviewRelationsDetailResponse> data = res.getData().stream().map(ReviewRelationsDetailResponse::from).toList();
        return ApiResponsePagination.success("Reviews fetched successfully", data,
                new PaginationMeta(page, pageSize, totalPages, totalRecords));
    }
}
