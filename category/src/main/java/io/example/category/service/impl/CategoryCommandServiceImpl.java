package io.example.category.service.impl;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.NotFoundException;
import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.category.model.Category;
import io.example.category.model.CategoryResponse;
import io.example.category.model.CategoryResponseDeleteAt;
import io.example.category.repository.CategoryCommandRepository;
import io.example.category.service.CategoryCommandService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import pb.category.CategoryCommand;

public class CategoryCommandServiceImpl implements CategoryCommandService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryCommandServiceImpl.class);

    private final CategoryCommandRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "category:";

    public CategoryCommandServiceImpl(
            CategoryCommandRepository repo,
            RedisService redis,
            TracingMetrics metrics) {
        this.repo = repo;
        this.redis = redis;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponse<CategoryResponse>> create(CategoryCommand.CreateCategoryRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "CategoryCommandService.create",
                Attributes.builder().put("category.name", Objects.requireNonNull(req.getName())).build());
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        logger.info("Creating category: {}", req.getName());

        return repo.createCategory(req)
                .map(created -> {
                    span.setAttribute("category.id", created.getCategoryId());
                    metrics.completeSpanSuccess(tracingContext, "create", "Category created successfully");
                    return ApiResponse.success("Category created successfully", CategoryResponse.from(created));
                })
                .recover(err -> {
                    logger.error("Failed to create category: {}", req.getName(), err);
                    metrics.completeSpanError(tracingContext, "create", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<CategoryResponse>error("Failed to create: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<CategoryResponse>> update(CategoryCommand.UpdateCategoryRequest req) {
        Long id = (long) req.getCategoryId();
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "CategoryCommandService.update",
                Attributes.builder().put("category.id", id).put("category.name", Objects.requireNonNull(req.getName())).build());

        logger.info("Updating category: {}, name: {}", id, req.getName());

        return repo.updateCategory(req)
                .compose(updated -> {
                    if (updated == null) {
                        return Future.<Category>failedFuture(new NotFoundException("Category not found"));
                    }
                    String cacheKey = CACHE_PREFIX + "id:" + id;
                    return redis.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) {
                                    logger.debug("Category {} cache invalidated", id);
                                }
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for category {}: {}", id, err.getMessage()))
                            .map(updated);
                })
                .map(updated -> {
                    metrics.completeSpanSuccess(tracingContext, "update", "Category updated successfully");
                    return ApiResponse.success("Category updated successfully", CategoryResponse.from(updated));
                })
                .recover(err -> {
                    logger.error("Failed to update category: {}", id, err);
                    metrics.completeSpanError(tracingContext, "update", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<CategoryResponse>error("Failed to update: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<CategoryResponseDeleteAt>> trash(Long id) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "CategoryCommandService.trash",
                Attributes.builder().put("category.id", id).build());

        logger.info("Trashing category: {}", id);

        return repo.trashCategory(id)
                .compose(trashed -> {
                    if (trashed == null) {
                        return Future.<Category>failedFuture(new NotFoundException("Category not found with id: " + id));
                    }
                    String cacheKey = CACHE_PREFIX + "id:" + id;
                    return redis.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) {
                                    logger.debug("Category {} cache invalidated on trash", id);
                                }
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for trashed category {}: {}", id, err.getMessage()))
                            .map(trashed);
                })
                .map(trashed -> {
                    metrics.completeSpanSuccess(tracingContext, "trash", "Category trashed successfully");
                    return ApiResponse.success("Category trashed successfully", CategoryResponseDeleteAt.from(trashed));
                })
                .recover(err -> {
                    logger.error("Failed to trash category: {}", id, err);
                    metrics.completeSpanError(tracingContext, "trash", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<CategoryResponseDeleteAt>error("Failed to trash: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<CategoryResponseDeleteAt>> restore(Long id) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "CategoryCommandService.restore",
                Attributes.builder().put("category.id", id).build());

        logger.info("Restoring category: {}", id);

        return repo.restoreCategory(id)
                .compose(restored -> {
                    if (restored == null) {
                        return Future.<Category>failedFuture(new NotFoundException("Category not found with id: " + id));
                    }
                    String cacheKey = CACHE_PREFIX + "id:" + id;
                    return redis.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) {
                                    logger.debug("Category {} cache invalidated on restore", id);
                                }
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for restored category {}: {}", id, err.getMessage()))
                            .map(restored);
                })
                .map(restored -> {
                    metrics.completeSpanSuccess(tracingContext, "restore", "Category restored successfully");
                    return ApiResponse.success("Category restored successfully", CategoryResponseDeleteAt.from(restored));
                })
                .recover(err -> {
                    logger.error("Failed to restore category: {}", id, err);
                    metrics.completeSpanError(tracingContext, "restore", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<CategoryResponseDeleteAt>error("Failed to restore: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Boolean>> deletePermanent(Long id) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "CategoryCommandService.deletePermanent",
                Attributes.builder().put("category.id", id).build());

        logger.info("Permanently deleting category: {}", id);

        return repo.deleteCategoryPermanently(id)
                .compose(v -> {
                    String cacheKey = CACHE_PREFIX + "id:" + id;
                    return redis.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) {
                                    logger.debug("Category {} cache invalidated on permanent delete", id);
                                }
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for deleted category {}: {}", id, err.getMessage()))
                            .map(v);
                })
                .map(v -> {
                    logger.info("Category deleted permanently: {}", id);
                    metrics.completeSpanSuccess(tracingContext, "deletePermanent", "Category deleted permanently");
                    return ApiResponse.success("Category deleted permanently", true);
                })
                .recover(err -> {
                    logger.error("Failed to permanently delete category: {}", id, err);
                    metrics.completeSpanError(tracingContext, "deletePermanent", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<Boolean>error("Failed to delete permanently: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Integer>> restoreAll() {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryCommandService.restoreAll");

        logger.info("Attempting to restore all trashed categories");

        return repo.restoreAllCategories()
                .map(rows -> {
                    logger.info("All categories restored successfully. Total: {}", rows);
                    metrics.completeSpanSuccess(tracingContext, "restoreAll", "All categories restored");
                    return ApiResponse.success("All categories restored successfully", rows);
                })
                .recover(err -> {
                    logger.error("Failed to restore all categories", err);
                    metrics.completeSpanError(tracingContext, "restoreAll", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<Integer>error("Failed to restore all: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Integer>> deleteAllPermanent() {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryCommandService.deleteAllPermanent");

        logger.info("Attempting to delete permanently all trashed categories");

        return repo.deleteAllPermanentCategories()
                .map(rows -> {
                    logger.info("All trashed categories deleted permanently. Total: {}", rows);
                    metrics.completeSpanSuccess(tracingContext, "deleteAllPermanent", "All trashed categories deleted permanently");
                    return ApiResponse.success("All trashed categories deleted permanently", rows);
                })
                .recover(err -> {
                    logger.error("Failed to delete permanently all trashed categories", err);
                    metrics.completeSpanError(tracingContext, "deleteAllPermanent", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<Integer>error("Failed to delete permanently all: " + err.getMessage()));
                });
    }
}
