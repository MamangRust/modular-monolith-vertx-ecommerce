package io.example.product.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.example.common.domain.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.product.model.CreateProductRequest;
import io.example.product.model.ProductResponse;
import io.example.product.model.ProductResponseDeleteAt;
import io.example.product.model.UpdateProductRequest;
import io.example.product.repository.CategoryQueryRepository;
import io.example.product.repository.MerchantQueryRepository;
import io.example.product.repository.ProductCommandRepository;
import io.example.product.service.ProductCommandService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;

public class ProductCommandServiceImpl implements ProductCommandService {
    private static final Logger logger = LoggerFactory.getLogger(ProductCommandServiceImpl.class);

    private final ProductCommandRepository repository;
    private final CategoryQueryRepository categoryRepo;
    private final MerchantQueryRepository merchantRepo;
    private final RedisService redisService;
    private final TracingMetrics tracingMetrics;

    public ProductCommandServiceImpl(
            ProductCommandRepository repository,
            CategoryQueryRepository categoryRepo,
            MerchantQueryRepository merchantRepo,
            RedisService redisService,
            TracingMetrics tracingMetrics) {
        this.repository = repository;
        this.categoryRepo = categoryRepo;
        this.merchantRepo = merchantRepo;
        this.redisService = redisService;
        this.tracingMetrics = tracingMetrics;
    }

    @Override
    public Future<ApiResponse<ProductResponse>> create(CreateProductRequest req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ProductCommandService.create");
        Span span = Span.fromContext(tracingContext.getContext());

        logger.info("Creating product: {}", req.getName());

        return categoryRepo.findById(req.getCategoryId().intValue())
                .compose(existsCategory -> {
                    if (!existsCategory) {
                        return Future.failedFuture(new RuntimeException("Category not found with ID: " + req.getCategoryId()));
                    }
                    return merchantRepo.findById(req.getMerchantId().intValue());
                })
                .compose(existsMerchant -> {
                    if (!existsMerchant) {
                        return Future.failedFuture(new RuntimeException("Merchant not found with ID: " + req.getMerchantId()));
                    }
                    return repository.create(req);
                })
                .compose(data -> {
                    span.setAttribute("id", data.getProductId());
                    return redisService.deleteByPattern("products:*")
                            .map(data);
                })
                .map(data -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "create", "Success");
                    return ApiResponse.success("Product created", ProductResponse.from(data));
                })
                .recover(err -> {
                    logger.error("Failed to create product", err);
                    tracingMetrics.completeSpanError(tracingContext, "create", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to create: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<ProductResponse>> update(UpdateProductRequest req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ProductCommandService.update",
                Attributes.builder().put("id", req.getProductId()).build());
        Span span = Span.fromContext(tracingContext.getContext());

        logger.info("Updating product ID: {}", req.getProductId());

        return categoryRepo.findById(req.getCategoryId().intValue())
                .compose(existsCategory -> {
                    if (!existsCategory) {
                        return Future.failedFuture(new RuntimeException("Category not found with ID: " + req.getCategoryId()));
                    }
                    return merchantRepo.findById(req.getMerchantId().intValue());
                })
                .compose(existsMerchant -> {
                    if (!existsMerchant) {
                        return Future.failedFuture(new RuntimeException("Merchant not found with ID: " + req.getMerchantId()));
                    }
                    return repository.update(req);
                })
                .compose(data -> {
                    if (data == null) {
                        return Future.failedFuture(new RuntimeException("Product not found"));
                    }
                    return redisService.delete("product:" + req.getProductId())
                            .compose(v -> redisService.deleteByPattern("products:*"))
                            .map(data);
                })
                .map(data -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "update", "Success");
                    return ApiResponse.success("Product updated", ProductResponse.from(data));
                })
                .recover(err -> {
                    logger.error("Failed to update product", err);
                    tracingMetrics.completeSpanError(tracingContext, "update", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to update: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<ProductResponse>> updateProductCountStock(Long productId, Integer countInStock) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ProductCommandService.updateProductCountStock",
                Attributes.builder().put("id", productId).build());

        logger.info("Updating stock for product ID: {} to {}", productId, countInStock);

        return repository.updateProductCountStock(productId, countInStock)
                .compose(data -> {
                    if (data == null) {
                        return Future.failedFuture(new RuntimeException("Product not found"));
                    }
                    return redisService.delete("product:" + productId)
                            .compose(v -> redisService.deleteByPattern("products:*"))
                            .map(data);
                })
                .map(data -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "update_stock", "Success");
                    return ApiResponse.success("Product stock updated", ProductResponse.from(data));
                })
                .recover(err -> {
                    logger.error("Failed to update product stock", err);
                    tracingMetrics.completeSpanError(tracingContext, "update_stock", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to update stock: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<ProductResponseDeleteAt>> trash(Long id) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ProductCommandService.trash",
                Attributes.builder().put("id", id).build());

        logger.info("Trashing product ID: {}", id);

        return repository.trash(id)
                .compose(data -> {
                    if (data == null) {
                        return Future.failedFuture(new RuntimeException("Product not found"));
                    }
                    return redisService.delete("product:" + id)
                            .compose(v -> redisService.deleteByPattern("products:*"))
                            .map(data);
                })
                .map(data -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "trash", "Success");
                    return ApiResponse.success("Product trashed", ProductResponseDeleteAt.from(data));
                })
                .recover(err -> {
                    logger.error("Failed to trash product", err);
                    tracingMetrics.completeSpanError(tracingContext, "trash", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to trash: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<ProductResponseDeleteAt>> restore(Long id) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ProductCommandService.restore",
                Attributes.builder().put("id", id).build());

        logger.info("Restoring product ID: {}", id);

        return repository.restore(id)
                .compose(data -> {
                    if (data == null) {
                        return Future.failedFuture(new RuntimeException("Product not found"));
                    }
                    return redisService.delete("product:" + id)
                            .compose(v -> redisService.deleteByPattern("products:*"))
                            .map(data);
                })
                .map(data -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "restore", "Success");
                    return ApiResponse.success("Product restored", ProductResponseDeleteAt.from(data));
                })
                .recover(err -> {
                    logger.error("Failed to restore product", err);
                    tracingMetrics.completeSpanError(tracingContext, "restore", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to restore: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deletePermanent(Long id) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ProductCommandService.deletePermanent",
                Attributes.builder().put("id", id).build());

        logger.info("Permanently deleting product ID: {}", id);

        return repository.deletePermanent(id)
                .compose(v -> redisService.delete("product:" + id))
                .compose(v -> redisService.deleteByPattern("products:*"))
                .map(v -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "delete_permanent", "Success");
                    return ApiResponse.<Void>success("Product deleted permanently", null);
                })
                .recover(err -> {
                    logger.error("Failed to permanently delete product", err);
                    tracingMetrics.completeSpanError(tracingContext, "delete_permanent", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to delete permanently: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> restoreAll() {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ProductCommandService.restoreAll");

        logger.info("Restoring all soft-deleted products");

        return repository.restoreAll()
                .compose(v -> redisService.deleteByPattern("product:*"))
                .compose(v -> redisService.deleteByPattern("products:*"))
                .map(v -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "restore_all", "Success");
                    return ApiResponse.<Void>success("All products restored successfully", null);
                })
                .recover(err -> {
                    logger.error("Failed to restore all products", err);
                    tracingMetrics.completeSpanError(tracingContext, "restore_all", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to restore all: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deleteAllPermanent() {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ProductCommandService.deleteAllPermanent");

        logger.info("Permanently deleting all soft-deleted products");

        return repository.deleteAll()
                .compose(v -> redisService.deleteByPattern("product:*"))
                .compose(v -> redisService.deleteByPattern("products:*"))
                .map(v -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "delete_all", "Success");
                    return ApiResponse.<Void>success("All soft-deleted products deleted permanently", null);
                })
                .recover(err -> {
                    logger.error("Failed to delete all permanent products", err);
                    tracingMetrics.completeSpanError(tracingContext, "delete_all", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to delete all permanently: " + err.getMessage()));
                });
    }
}
