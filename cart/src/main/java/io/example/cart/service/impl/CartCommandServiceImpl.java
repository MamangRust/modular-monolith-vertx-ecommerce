package io.example.cart.service.impl;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.cart.model.CartCreateRecord;
import io.example.cart.model.CartResponse;
import io.example.cart.repository.CartCommandRepository;
import io.example.cart.repository.ProductQueryRepository;
import io.example.cart.repository.UserQueryRepository;
import io.example.cart.service.CartCommandService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import pb.cart.CartCommand.CreateCartRequest;
import pb.cart.CartCommand.DeleteCartRequest;
import pb.cart.CartCommand.DeleteAllCartRequest;

public class CartCommandServiceImpl implements CartCommandService {
    private static final Logger logger = LoggerFactory.getLogger(CartCommandServiceImpl.class);

    private final CartCommandRepository repo;
    private final ProductQueryRepository productRepo;
    private final UserQueryRepository userRepo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "cart:";

    public CartCommandServiceImpl(
            CartCommandRepository repo,
            ProductQueryRepository productRepo,
            UserQueryRepository userRepo,
            RedisService redis,
            TracingMetrics metrics) {
        this.repo = repo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.redis = redis;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponse<CartResponse>> create(CreateCartRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "CartCommandService.create",
                Attributes.builder()
                        .put("cart.product_id", (long) req.getProductId())
                        .put("cart.user_id", (long) req.getUserId())
                        .build());
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        logger.info("Creating cart item: product={}, user={}", req.getProductId(), req.getUserId());

        return productRepo.findById(req.getProductId())
                .compose(product -> {
                    if (product == null) {
                        return Future.failedFuture("Product not found");
                    }
                    return userRepo.findById(req.getUserId())
                            .compose(userExists -> {
                                if (!userExists) {
                                    return Future.failedFuture("User not found");
                                }

                                CartCreateRecord record = CartCreateRecord.builder()
                                        .productId((long) req.getProductId())
                                        .userId((long) req.getUserId())
                                        .quantity(req.getQuantity())
                                        .name(product.getName())
                                        .price(product.getPrice())
                                        .imageProduct(product.getImageProduct())
                                        .weight(product.getWeight())
                                        .build();

                                return repo.createCart(record);
                            });
                })
                .compose(created -> {
                    span.setAttribute("cart.id", created.getCartId());
                    String pattern = CACHE_PREFIX + "all:u:" + req.getUserId() + ":*";
                    return redis.deleteByPattern(pattern)
                            .onSuccess(count -> logger.debug("Invalidated {} cache keys for user {}", count, req.getUserId()))
                            .onFailure(err -> logger.warn("Failed to invalidate cache: {}", err.getMessage()))
                            .map(created);
                })
                .map(created -> {
                    metrics.completeSpanSuccess(tracingContext, "create", "Cart item created successfully");
                    return ApiResponse.success("Cart item created successfully", CartResponse.from(created));
                })
                .recover(err -> {
                    logger.error("Failed to create cart item", err);
                    metrics.completeSpanError(tracingContext, "create", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<CartResponse>error("Failed to create cart item: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Boolean>> deletePermanent(DeleteCartRequest req) {
        Long cartId = (long) req.getCartId();
        Integer userId = req.getUserId();
        
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "CartCommandService.deletePermanent",
                Attributes.builder()
                        .put("cart.id", cartId)
                        .put("cart.user_id", (long) userId)
                        .build());

        logger.info("Permanently deleting cart item: id={}, user={}", cartId, userId);

        return repo.deletePermanent(cartId, userId)
                .compose(deleted -> {
                    if (!deleted) {
                        return Future.succeededFuture(false);
                    }
                    String pattern = CACHE_PREFIX + "all:u:" + userId + ":*";
                    return redis.deleteByPattern(pattern)
                            .onSuccess(count -> logger.debug("Invalidated {} cache keys for user {}", count, userId))
                            .onFailure(err -> logger.warn("Failed to invalidate cache: {}", err.getMessage()))
                            .map(true);
                })
                .map(deleted -> {
                    metrics.completeSpanSuccess(tracingContext, "delete_permanent", "Cart item deleted successfully");
                    return ApiResponse.success("Cart item deleted successfully", deleted);
                })
                .recover(err -> {
                    logger.error("Failed to delete cart item: id={}", cartId, err);
                    metrics.completeSpanError(tracingContext, "delete_permanent", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Boolean>error("Failed to delete cart item: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Boolean>> deleteAll(DeleteAllCartRequest req) {
        Integer userId = req.getUserId();
        java.util.List<Long> cartIds = req.getCartIdsList().stream().map(Integer::longValue).toList();

        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "CartCommandService.deleteAll",
                Attributes.builder()
                        .put("cart.user_id", (long) userId)
                        .build());

        logger.info("Permanently deleting all requested cart items for user={}", userId);

        return repo.deleteAllPermanently(cartIds, userId)
                .compose(deleted -> {
                    if (!deleted) {
                        return Future.succeededFuture(false);
                    }
                    String pattern = CACHE_PREFIX + "all:u:" + userId + ":*";
                    return redis.deleteByPattern(pattern)
                            .onSuccess(count -> logger.debug("Invalidated {} cache keys for user {}", count, userId))
                            .onFailure(err -> logger.warn("Failed to invalidate cache: {}", err.getMessage()))
                            .map(true);
                })
                .map(deleted -> {
                    metrics.completeSpanSuccess(tracingContext, "delete_all", "All requested cart items deleted successfully");
                    return ApiResponse.success("All requested cart items deleted successfully", deleted);
                })
                .recover(err -> {
                    logger.error("Failed to delete all requested cart items for user: {}", userId, err);
                    metrics.completeSpanError(tracingContext, "delete_all", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Boolean>error("Failed to delete all requested cart items: " + err.getMessage()));
                });
    }
}
