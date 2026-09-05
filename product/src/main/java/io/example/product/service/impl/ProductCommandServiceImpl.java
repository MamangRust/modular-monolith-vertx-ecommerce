package io.example.product.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.product.domain.requests.CreateProductRequest;
import io.example.product.domain.requests.UpdateProductRequest;
import io.example.product.model.ProductResponse;
import io.example.product.model.ProductResponseDeleteAt;
import io.example.product.repository.CategoryQueryRepository;
import io.example.product.repository.MerchantQueryRepository;
import io.example.product.repository.ProductCommandRepository;
import io.example.product.repository.ProductQueryRepository;
import io.example.product.service.ProductCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductCommandServiceImpl implements ProductCommandService {
    private static final Logger logger = LoggerFactory.getLogger(ProductCommandServiceImpl.class);

    private final ProductCommandRepository repository;
    private final ProductQueryRepository queryRepository;
    private final CategoryQueryRepository categoryRepo;
    private final MerchantQueryRepository merchantRepo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "product:";

    private Future<Void> evict(Object id) {
        return redis.delete(CACHE_PREFIX + id)
                .compose(v -> redis.deleteByPattern("products:*"))
                .<Void>mapEmpty();
    }

    private Future<Void> evictAll() {
        return redis.deleteByPattern("products:*")
                .compose(v -> redis.deleteByPattern(CACHE_PREFIX + "*"))
                .<Void>mapEmpty();
    }

    @Override
    public Future<ProductResponse> create(CreateProductRequest req) {
        var ctx = metrics.startSpan("ProductCommandService.create",
                Attributes.builder().put("product.name", req.getName()).build());

        return categoryRepo.findById(req.getCategoryId().intValue())
                .compose(existsCategory -> {
                    if (!existsCategory) {
                        return Future.failedFuture(
                                new NotFoundException("Category not found with ID: " + req.getCategoryId()));
                    }
                    return merchantRepo.findById(req.getMerchantId().intValue());
                })
                .compose(existsMerchant -> {
                    if (!existsMerchant) {
                        return Future.failedFuture(
                                new NotFoundException("Merchant not found with ID: " + req.getMerchantId()));
                    }
                    return repository.create(req);
                })
                .compose(data -> evict(data.getProductId()).map(v -> data))
                .map(ProductResponse::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "create", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to create product: {}", req.getName(), e);
                    metrics.completeSpanError(ctx, "create", e.getMessage());
                });
    }

    @Override
    public Future<ProductResponse> update(UpdateProductRequest req) {
        var ctx = metrics.startSpan("ProductCommandService.update",
                Attributes.builder().put("product.id", req.getProductId()).build());

        // Conditional checks: only validate existence if IDs are actually provided
        var categoryIdFuture = (req.getCategoryId() != null && req.getCategoryId() > 0)
                ? categoryRepo.findById(req.getCategoryId().intValue())
                : Future.succeededFuture(true);
        var merchantIdFuture = (req.getMerchantId() != null && req.getMerchantId() > 0)
                ? merchantRepo.findById(req.getMerchantId().intValue())
                : Future.succeededFuture(true);

        return categoryIdFuture
                .compose(existsCategory -> {
                    if (!existsCategory) {
                        return Future.failedFuture(
                                new NotFoundException("Category not found with ID: " + req.getCategoryId()));
                    }
                    return merchantIdFuture;
                })
                .compose(existsMerchant -> {
                    if (!existsMerchant) {
                        return Future.failedFuture(
                                new NotFoundException("Merchant not found with ID: " + req.getMerchantId()));
                    }
                    return repository.update(req);
                })
                .compose(data -> {
                    if (data == null) {
                        return Future.failedFuture(new NotFoundException("Product not found"));
                    }
                    return evict(req.getProductId()).map(v -> data);
                })
                .map(ProductResponse::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "update", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "update", e.getMessage()));
    }

    @Override
    public Future<ProductResponse> updateProductCountStock(Integer productId, Integer countInStock) {
        var ctx = metrics.startSpan("ProductCommandService.updateProductCountStock",
                Attributes.builder().put("product.id", productId).build());

        return repository.updateProductCountStock(productId, countInStock)
                .compose(data -> {
                    if (data == null) {
                        return Future.failedFuture(new NotFoundException("Product not found"));
                    }
                    return evict(productId).map(v -> data);
                })
                .map(ProductResponse::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "update_stock", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "update_stock", e.getMessage()));
    }

    @Override
    public Future<ProductResponse> decrementStock(Integer productId, Integer quantity) {
        var ctx = metrics.startSpan("ProductCommandService.decrementStock",
                Attributes.builder()
                        .put("product.id", productId)
                        .put("quantity", quantity)
                        .build());

        logger.info("Atomically decrementing stock for product: {} by {}", productId, quantity);

        return repository.decrementStock(productId, quantity)
                .compose(data -> evict(productId).map(v -> data))
                .map(ProductResponse::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "decrement_stock", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "decrement_stock", e.getMessage()));
    }

    @Override
    public Future<ProductResponse> incrementStock(Integer productId, Integer quantity) {
        if (productId == null || productId <= 0 || quantity == null || quantity <= 0) {
            return Future.failedFuture(new BadRequestException(
                    "Product ID and a positive quantity are required"));
        }

        var ctx = metrics.startSpan("ProductCommandService.incrementStock",
                Attributes.builder().put("product.id", productId).put("quantity", quantity).build());
        return repository.incrementStock(productId, quantity)
                .compose(data -> evict(productId).map(v -> data))
                .map(ProductResponse::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "increment_stock", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "increment_stock", e.getMessage()));
    }

    @Override
    public Future<ProductResponseDeleteAt> trash(Long id) {
        var ctx = metrics.startSpan("ProductCommandService.trash",
                Attributes.builder().put("product.id", id).build());

        return repository.trash(id)
                .compose(data -> {
                    if (data == null) {
                        return Future.failedFuture(new NotFoundException("Product not found"));
                    }
                    return evict(id).map(v -> data);
                })
                .map(ProductResponseDeleteAt::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "trash", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "trash", e.getMessage()));
    }

    @Override
    public Future<ProductResponseDeleteAt> restore(Long id) {
        var ctx = metrics.startSpan("ProductCommandService.restore",
                Attributes.builder().put("product.id", id).build());

        logger.info("Restoring product: {}", id);

        return queryRepository.findByIdTrashed(id)
                .compose(trashed -> {
                    if (trashed == null) {
                        return Future
                                .failedFuture(new BadRequestException("Product not found or must be trashed first"));
                    }
                    return repository.restore(id);
                })
                .compose(r -> {
                    if (r == null) {
                        return Future.failedFuture(new NotFoundException("Product not found"));
                    }
                    return evict(id).map(v -> r);
                })
                .map(ProductResponseDeleteAt::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to restore product", e);
                    metrics.completeSpanError(ctx, "restore", e.getMessage());
                });
    }

    @Override
    public Future<Void> deletePermanent(Long id) {
        var ctx = metrics.startSpan("ProductCommandService.deletePermanent",
                Attributes.builder().put("product.id", id).build());

        return queryRepository.findByIdTrashed(id)
                .compose(existing -> {
                    if (existing == null) {
                        return Future.<Void>failedFuture(
                                new NotFoundException("Product not found or must be trashed first"));
                    }
                    return repository.deletePermanent(id)
                            .compose(v -> evictAll());
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deletePermanent", "Product deleted permanently"))
                .onFailure(err -> metrics.completeSpanError(ctx, "deletePermanent", err.getMessage()));
    }

    @Override
    public Future<Void> restoreAll() {
        var ctx = metrics.startSpan("ProductCommandService.restoreAll");

        return repository.restoreAll()
                .compose(count -> {
                    if (count == 0) {
                        return Future.<Void>failedFuture(new NotFoundException("No trashed products found"));
                    }
                    return evictAll();
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore_all", "Success"))
                .onFailure(err -> metrics.completeSpanError(ctx, "restore_all", err.getMessage()));
    }

    @Override
    public Future<Void> deleteAllPermanent() {
        var ctx = metrics.startSpan("ProductCommandService.deleteAllPermanent");

        return repository.deleteAll()
                .compose(count -> {
                    if (count == 0) {
                        return Future.<Void>failedFuture(new NotFoundException("No trashed products found"));
                    }
                    return evictAll();
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "delete_all_permanent", "Success"))
                .onFailure(err -> metrics.completeSpanError(ctx, "delete_all_permanent", err.getMessage()));
    }
}