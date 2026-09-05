package io.example.category.service.impl;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.category.domain.requests.CreateCategoryRequest;
import io.example.category.domain.requests.UpdateCategoryRequest;
import io.example.category.model.CategoryResponse;
import io.example.category.model.CategoryResponseDeleteAt;
import io.example.category.repository.CategoryCommandRepository;
import io.example.category.service.CategoryCommandService;
import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.ConflictException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CategoryCommandServiceImpl implements CategoryCommandService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryCommandServiceImpl.class);

    private final CategoryCommandRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "category:";

    private Future<Void> evictCaches() {
        return redis.deleteByPattern(CACHE_PREFIX + "*")
                .onFailure(err -> logger.warn("Category cache eviction failed: {}", err.getMessage()))
                .recover(err -> Future.succeededFuture(0L))
                .<Void>mapEmpty();
    }

    @Override
    public Future<CategoryResponse> create(CreateCategoryRequest req) {
        var ctx = metrics.startSpan(
                "CategoryCommandService.create",
                Attributes.builder().put("category.name", Objects.requireNonNull(req.getName())).build());
        Span span = Span.fromContext(Objects.requireNonNull(ctx.getContext()));

        logger.info("Creating category: {}", req.getName());

        return repo.createCategory(req)
                .map(created -> {
                    span.setAttribute("category.id", created.getCategoryId());
                    metrics.completeSpanSuccess(ctx, "create", "Category created successfully");
                    return CategoryResponse.from(created);
                })
                .recover(err -> {
                    logger.error("Failed to create category: {}", req.getName(), err);
                    metrics.completeSpanError(ctx, "create", err.getMessage());
                    if (isUniqueViolation(err)) {
                        return Future.failedFuture(new ConflictException(
                                "Category with slug '" + req.getSlugCategory() + "' already exists"));
                    }
                    return Future.failedFuture(err);
                });
    }

    private boolean isUniqueViolation(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current.getMessage() != null && (current.getMessage().contains("23505")
                    || current.getMessage().toLowerCase().contains("duplicate key")
                    || current.getMessage().contains("uq_categories_active_slug"))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    @Override
    public Future<CategoryResponse> update(UpdateCategoryRequest req) {
        Long id = req.getId();
        var ctx = metrics.startSpan(
                "CategoryCommandService.update",
                Attributes.builder().put("category.id", id).put("category.name", req.getName() != null ? req.getName() : "")
                        .build());

        logger.info("Updating category: {}, name: {}", id, req.getName());

        return repo.updateCategory(req)
                .compose(updated -> {
                    if (updated == null) {
                        return Future.failedFuture(new NotFoundException("Category not found"));
                    }
                    return evictCaches()
                            .onFailure(err -> logger.warn("Failed to invalidate category caches for {}: {}", id,
                                    err.getMessage()))
                            .map(updated);
                })
                .map(updated -> {
                    metrics.completeSpanSuccess(ctx, "update", "Category updated successfully");
                    return CategoryResponse.from(updated);
                })
                .onFailure(err -> {
                    logger.error("Failed to update category: {}", id, err);
                    metrics.completeSpanError(ctx, "update", err.getMessage());
                });
    }

    @Override
    public Future<CategoryResponseDeleteAt> trash(Long id) {
        var ctx = metrics.startSpan(
                "CategoryCommandService.trash",
                Attributes.builder().put("category.id", id).build());

        logger.info("Trashing category: {}", id);

        return repo.trashCategory(id)
                .compose(trashed -> {
                    if (trashed == null) {
                        return Future.failedFuture(new NotFoundException("Category not found with id: " + id));
                    }
                    return evictCaches()
                            .onFailure(err -> logger.warn("Failed to invalidate category caches after trash {}: {}", id,
                                    err.getMessage()))
                            .map(trashed);
                })
                .map(trashed -> {
                    metrics.completeSpanSuccess(ctx, "trash", "Category trashed successfully");
                    return CategoryResponseDeleteAt.from(trashed);
                })
                .onFailure(err -> {
                    logger.error("Failed to trash category: {}", id, err);
                    metrics.completeSpanError(ctx, "trash", err.getMessage());
                });
    }

    @Override
    public Future<CategoryResponseDeleteAt> restore(Long id) {
        var ctx = metrics.startSpan(
                "CategoryCommandService.restore",
                Attributes.builder().put("category.id", id).build());

        logger.info("Restoring category: {}", id);

        return repo.restoreCategory(id)
                .compose(restored -> {
                    if (restored == null) {
                        return Future.failedFuture(new BadRequestException("Category not found or must be trashed first"));
                    }
                    return evictCaches()
                            .onFailure(err -> logger.warn("Failed to invalidate category caches after restore {}: {}", id,
                                    err.getMessage()))
                            .map(restored);
                })
                .map(CategoryResponseDeleteAt::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore", "Category restored successfully"))
                .onFailure(err -> {
                    logger.error("Failed to restore category: {}", id, err);
                    metrics.completeSpanError(ctx, "restore", err.getMessage());
                });
    }

    @Override
    public Future<Boolean> deletePermanent(Long id) {
        var ctx = metrics.startSpan(
                "CategoryCommandService.deletePermanent",
                Attributes.builder().put("category.id", id).build());

        logger.info("Permanently deleting category: {}", id);

        return repo.deleteCategoryPermanently(id)
                .compose(deleted -> {
                    if (!deleted) {
                        return Future.failedFuture(new NotFoundException("Trashed category not found with id: " + id));
                    }
                    return evictCaches()
                            .onFailure(err -> logger.warn("Failed to invalidate category caches after delete {}: {}", id,
                                    err.getMessage()))
                            .map(deleted);
                })
                .map(v -> {
                    logger.info("Category deleted permanently: {}", id);
                    metrics.completeSpanSuccess(ctx, "deletePermanent", "Category deleted permanently");
                    return true;
                })
                .onFailure(err -> {
                    logger.error("Failed to permanently delete category: {}", id, err);
                    metrics.completeSpanError(ctx, "deletePermanent", err.getMessage());
                });
    }

    @Override
    public Future<Integer> restoreAll() {
        var ctx = metrics.startSpan("CategoryCommandService.restoreAll");

        logger.info("Attempting to restore all trashed categories");

        return repo.restoreAllCategories()
                .compose(rows -> {
                    if (rows == 0) {
                        return Future.failedFuture(new NotFoundException("No trashed categories found"));
                    }
                    return evictCaches().map(rows);
                })
                .onSuccess(rows -> {
                    logger.info("Restored {} trashed categories", rows);
                    metrics.completeSpanSuccess(ctx, "restoreAll", "Restored all successfully");
                })
                .onFailure(err -> {
                    logger.error("Failed to restore all categories", err);
                    metrics.completeSpanError(ctx, "restoreAll", err.getMessage());
                });
    }

    @Override
    public Future<Integer> deleteAllPermanent() {
        var ctx = metrics.startSpan("CategoryCommandService.deleteAllPermanent");

        logger.info("Attempting to delete permanently all trashed categories");

        return repo.deleteAllPermanentCategories()
                .compose(rows -> {
                    if (rows == 0) {
                        return Future.failedFuture(new NotFoundException("No trashed categories found"));
                    }
                    return evictCaches().map(rows);
                })
                .onSuccess(rows -> {
                    logger.info("All trashed categories deleted permanently. Total: {}", rows);
                    metrics.completeSpanSuccess(ctx, "deleteAllPermanent", "All deleted successfully");
                })
                .onFailure(err -> {
                    logger.error("Failed to delete permanently all trashed categories", err);
                    metrics.completeSpanError(ctx, "deleteAllPermanent", err.getMessage());
                });
    }
}