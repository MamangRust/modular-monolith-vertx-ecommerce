package io.example.category.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.common.model.PagedResult;
import io.example.common.model.PaginationMeta;
import io.example.common.exception.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.category.model.Category;
import io.example.category.model.CategoryResponse;
import io.example.category.model.CategoryResponseDeleteAt;
import io.example.category.repository.CategoryQueryRepository;
import io.example.category.service.CategoryQueryService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import pb.category.CategoryQuery;

public class CategoryQueryServiceImpl implements CategoryQueryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryQueryServiceImpl.class);

    private final CategoryQueryRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "category:";
    private static final Duration CACHE_TTL_LIST = Duration.ofMinutes(10);
    private static final Duration CACHE_TTL_ITEM = Duration.ofMinutes(60);

    public CategoryQueryServiceImpl(
            CategoryQueryRepository repo,
            RedisService redis,
            TracingMetrics metrics) {
        this.repo = repo;
        this.redis = redis;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponsePagination<List<CategoryResponse>>> getAll(CategoryQuery.FindAllCategoryRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryQueryService.getAll");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        String cacheKey = String.format("%sall:p:%d:s:%d:k:%s", CACHE_PREFIX, page, pageSize, keyword);

        return redis.getJson(cacheKey, ApiResponsePagination.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("category.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_all", "Categories fetched from cache");
                        @SuppressWarnings("unchecked")
                        ApiResponsePagination<List<CategoryResponse>> typedCached = (ApiResponsePagination<List<CategoryResponse>>) cached;
                        return Future.succeededFuture(typedCached);
                    }
                    span.setAttribute("category.cache_hit", false);
                    return repo.getCategories(req)
                            .map(result -> mapCategoryPagination(result, page, pageSize))
                            .compose(response -> redis.setJson(cacheKey, response, CACHE_TTL_LIST).map(response));
                })
                .onSuccess(response -> {
                    span.setAttribute("categories.count", (long) response.data().size());
                    span.setAttribute("categories.total_records", (long) response.pagination().totalRecords());
                    metrics.completeSpanSuccess(tracingContext, "get_all", "Categories fetched successfully");
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch categories", throwable);
                    metrics.completeSpanError(tracingContext, "get_all", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.<List<CategoryResponse>>error("Failed to fetch categories: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<CategoryResponse>>> getActive(CategoryQuery.FindAllCategoryRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryQueryService.getActive");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        String cacheKey = String.format("%sactive:p:%d:s:%d:k:%s", CACHE_PREFIX, page, pageSize, keyword);

        return redis.getJson(cacheKey, ApiResponsePagination.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("category.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_active", "Active categories fetched from cache");
                        @SuppressWarnings("unchecked")
                        ApiResponsePagination<List<CategoryResponse>> typedCached = (ApiResponsePagination<List<CategoryResponse>>) cached;
                        return Future.succeededFuture(typedCached);
                    }
                    span.setAttribute("category.cache_hit", false);
                    return repo.getCategoriesActive(req)
                            .map(result -> mapCategoryPagination(result, page, pageSize))
                            .compose(response -> redis.setJson(cacheKey, response, CACHE_TTL_LIST).map(response));
                })
                .onSuccess(response -> {
                    span.setAttribute("categories.count", (long) response.data().size());
                    span.setAttribute("categories.total_records", (long) response.pagination().totalRecords());
                    metrics.completeSpanSuccess(tracingContext, "get_active", "Active categories fetched successfully");
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch active categories", throwable);
                    metrics.completeSpanError(tracingContext, "get_active", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.<List<CategoryResponse>>error("Failed to fetch active categories: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<CategoryResponseDeleteAt>>> getTrashed(CategoryQuery.FindAllCategoryRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryQueryService.getTrashed");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        String cacheKey = String.format("%strashed:p:%d:s:%d:k:%s", CACHE_PREFIX, page, pageSize, keyword);

        return redis.getJson(cacheKey, ApiResponsePagination.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("category.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_trashed", "Trashed categories fetched from cache");
                        @SuppressWarnings("unchecked")
                        ApiResponsePagination<List<CategoryResponseDeleteAt>> typedCached = (ApiResponsePagination<List<CategoryResponseDeleteAt>>) cached;
                        return Future.succeededFuture(typedCached);
                    }
                    span.setAttribute("category.cache_hit", false);
                    return repo.getCategoriesTrashed(req)
                            .map(result -> mapCategoryPaginationDeleteAt(result, page, pageSize))
                            .compose(response -> redis.setJson(cacheKey, response, CACHE_TTL_LIST).map(response));
                })
                .onSuccess(response -> {
                    span.setAttribute("categories.count", (long) response.data().size());
                    span.setAttribute("categories.total_records", (long) response.pagination().totalRecords());
                    metrics.completeSpanSuccess(tracingContext, "get_trashed", "Trashed categories fetched successfully");
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch trashed categories", throwable);
                    metrics.completeSpanError(tracingContext, "get_trashed", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.<List<CategoryResponseDeleteAt>>error("Failed to fetch trashed categories: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<CategoryResponse>> getById(Long id) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "CategoryQueryService.getById",
                Attributes.builder().put("category.id", id).build());
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        logger.info("Fetching category by id: {}", id);
        String cacheKey = CACHE_PREFIX + "id:" + id;

        return redis.getJson(cacheKey, Category.class)
                .compose(cached -> {
                    if (cached != null) {
                        logger.info("Category {} found in cache", id);
                        span.setAttribute("category.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_by_id", "Category fetched from cache");
                        return Future.succeededFuture(ApiResponse.success("Category fetched successfully (from cache)", CategoryResponse.from(cached)));
                    } else {
                        span.setAttribute("category.cache_hit", false);
                        return repo.getCategoryById(id)
                                .compose(data -> {
                                    if (data == null) {
                                        return Future.failedFuture(new NotFoundException("Category not found"));
                                    }
                                    return redis.setJson(cacheKey, data, CACHE_TTL_ITEM).map(data);
                                })
                                .map(data -> {
                                    metrics.completeSpanSuccess(tracingContext, "get_by_id", "Category fetched from database");
                                    return ApiResponse.success("Category fetched successfully", CategoryResponse.from(data));
                                });
                    }
                })
                .recover(err -> {
                    logger.error("Failed to fetch category by id: {}", id, err);
                    metrics.completeSpanError(tracingContext, "get_by_id", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<CategoryResponse>error("Failed to fetch category: " + err.getMessage()));
                });
    }

    private ApiResponsePagination<List<CategoryResponse>> mapCategoryPagination(
            PagedResult<Category> result,
            int page,
            int pageSize) {
        int totalRecords = result.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<CategoryResponse> data = result.getData()
                .stream()
                .map(CategoryResponse::from)
                .toList();

        return new ApiResponsePagination<>(
                "success",
                "Categories found",
                data,
                new PaginationMeta(page, pageSize, totalPages, totalRecords));
    }

    private ApiResponsePagination<List<CategoryResponseDeleteAt>> mapCategoryPaginationDeleteAt(
            PagedResult<Category> result,
            int page,
            int pageSize) {
        int totalRecords = result.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<CategoryResponseDeleteAt> data = result.getData()
                .stream()
                .map(CategoryResponseDeleteAt::from)
                .toList();

        return new ApiResponsePagination<>(
                "success",
                "Categories found",
                data,
                new PaginationMeta(page, pageSize, totalPages, totalRecords));
    }
}
