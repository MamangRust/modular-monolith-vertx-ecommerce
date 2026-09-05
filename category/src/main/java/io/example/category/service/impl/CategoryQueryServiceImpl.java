package io.example.category.service.impl;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.domain.PagedResult;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.category.model.Category;
import io.example.category.model.CategoryResponse;
import io.example.category.model.CategoryResponseDeleteAt;
import io.example.category.repository.CategoryQueryRepository;
import io.example.category.service.CategoryQueryService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import io.example.category.domain.requests.FindAllCategoriesRequest;

@RequiredArgsConstructor
public class CategoryQueryServiceImpl implements CategoryQueryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryQueryServiceImpl.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final CategoryQueryRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "category:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private PagedResult<CategoryResponse> mapPagination(PagedResult<Category> res) {
        List<CategoryResponse> data = res.getData().stream().map(CategoryResponse::from).toList();
        return new PagedResult<>(data, res.getTotalRecords());
    }

    private PagedResult<CategoryResponseDeleteAt> mapPaginationDeleteAt(PagedResult<Category> res) {
        List<CategoryResponseDeleteAt> data = res.getData().stream().map(CategoryResponseDeleteAt::from).toList();
        return new PagedResult<>(data, res.getTotalRecords());
    }

    @Override
    public Future<PagedResult<CategoryResponse>> getAll(FindAllCategoriesRequest req) {
        var ctx = metrics.startSpan("CategoryQueryService.getAll");
        String cacheKey = CACHE_PREFIX + "list:all:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
                + req.getPage() + ":" + req.getPageSize();

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Category> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Category>>() {
                                    });
                            return Future.succeededFuture(mapPagination(typedCached));
                        } catch (Exception e) {
                            logger.warn("Failed to deserialize cached categories: {}", e.getMessage());
                        }
                    }
                    return repo.getCategories(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
                            .map(this::mapPagination);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getAll", "Success"))
                .onFailure(e -> {
                    logger.error("getAll failed", e);
                    metrics.completeSpanError(ctx, "getAll", e.getMessage());
                });
    }

    @Override
    public Future<PagedResult<CategoryResponseDeleteAt>> getActive(FindAllCategoriesRequest req) {
        var ctx = metrics.startSpan("CategoryQueryService.getActive");
        String cacheKey = CACHE_PREFIX + "list:active:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
                + req.getPage() + ":" + req.getPageSize();

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Category> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Category>>() {
                                    });
                            return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
                        } catch (Exception e) {
                            logger.warn("Failed to deserialize cached active categories: {}", e.getMessage());
                        }
                    }
                    return repo.getCategoriesActive(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
                            .map(this::mapPaginationDeleteAt);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getActive", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getActive", e.getMessage()));
    }

    @Override
    public Future<PagedResult<CategoryResponseDeleteAt>> getTrashed(FindAllCategoriesRequest req) {
        var ctx = metrics.startSpan("CategoryQueryService.getTrashed");
        String cacheKey = CACHE_PREFIX + "list:trashed:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
                + req.getPage() + ":" + req.getPageSize();

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Category> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Category>>() {
                                    });
                            return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
                        } catch (Exception e) {
                            logger.warn("Failed to deserialize cached trashed categories: {}", e.getMessage());
                        }
                    }
                    return repo.getCategoriesTrashed(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
                            .map(this::mapPaginationDeleteAt);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTrashed", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getTrashed", e.getMessage()));
    }

    @Override
    public Future<CategoryResponse> getById(Long id) {
        var ctx = metrics.startSpan("CategoryQueryService.getById",
                Attributes.builder().put("category.id", id).build());
        String key = CACHE_PREFIX + id;

        return redis.getJson(key, Category.class)
                .compose(cached -> {
                    if (cached != null) {
                        return Future.succeededFuture(CategoryResponse.from(cached));
                    }
                    return repo.getCategoryById(id)
                            .compose(db -> {
                                if (db == null) {
                                    return Future.<Category>failedFuture(new NotFoundException("Category not found"));
                                }
                                return redis.setJson(key, db, CACHE_TTL).<Category>map(v -> db);
                            })
                            .map(CategoryResponse::from);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getById", "Success"))
                .onFailure(e -> {
                    logger.error("getById failed for id: {}", id, e);
                    metrics.completeSpanError(ctx, "getById", e.getMessage());
                });
    }
}