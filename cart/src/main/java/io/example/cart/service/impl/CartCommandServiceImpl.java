package io.example.cart.service.impl;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.cart.domain.requests.CartCreateRecord;
import io.example.cart.model.CartResponse;
import io.example.cart.repository.CartCommandRepository;
import io.example.cart.repository.ProductQueryRepository;
import io.example.cart.repository.UserQueryRepository;
import io.example.cart.service.CartCommandService;
import io.example.cart.domain.requests.CreateCartRequest;
import io.example.cart.domain.requests.DeleteCartRequest;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CartCommandServiceImpl implements CartCommandService {
    private final CartCommandRepository repository;
    private final ProductQueryRepository productRepository;
    private final UserQueryRepository userRepository;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private Future<Void> evict(Integer userId) {
        return redis.deleteByPattern("cart:list:all:" + userId + ":*").<Void>mapEmpty();
    }

    @Override
    public Future<CartResponse> create(CreateCartRequest req) {
        var ctx = metrics.startSpan("CartCommandService.create",
                Attributes.builder()
                        .put("cart.product_id", (long) req.getProductId())
                        .put("cart.user_id", (long) req.getUserId())
                        .build());

        return productRepository.findById(req.getProductId() != null ? req.getProductId().intValue() : 0)
                .compose(product -> {
                    if (product == null) {
                        return Future.failedFuture(new NotFoundException("Product not found"));
                    }
                    return userRepository.findById(req.getUserId())
                            .compose(userExists -> {
                                if (!userExists) {
                                    return Future.failedFuture(new NotFoundException("User not found"));
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

                                return repository.createCart(record);
                            });
                })
                .compose(created -> evict(req.getUserId()).map(created))
                .map(CartResponse::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "create", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "create", e.getMessage()));
    }

    @Override
    public Future<Boolean> deletePermanent(DeleteCartRequest req) {
        Long cartId = (req.getCartIds() != null && !req.getCartIds().isEmpty()) ? req.getCartIds().get(0) : null;
        Integer userId = req.getUserId();

        var ctx = metrics.startSpan("CartCommandService.deletePermanent",
                Attributes.builder()
                        .put("cart.id", cartId != null ? cartId : 0L)
                        .put("cart.user_id", userId != null ? (long) userId : 0L)
                        .build());

        if (cartId == null || userId == null) {
            return Future.failedFuture(new BadRequestException("Cart ID and User ID are required"));
        }

        return repository.deletePermanent(cartId, userId)
                .compose(deleted -> {
                    if (!deleted) {
                        return Future.failedFuture(new NotFoundException("Cart not found"));
                    }
                    return evict(userId).map(true);
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deletePermanent", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "deletePermanent", e.getMessage()));
    }

    @Override
    public Future<Boolean> deleteAll(DeleteCartRequest req) {
        Integer userId = req.getUserId();
        java.util.List<Long> cartIds = req.getCartIds();

        var ctx = metrics.startSpan("CartCommandService.deleteAll",
                Attributes.builder()
                        .put("cart.user_id", userId != null ? (long) userId : 0L)
                        .build());

        if (userId == null || cartIds == null || cartIds.isEmpty()) {
            return Future.failedFuture(new BadRequestException("User ID and Cart IDs are required"));
        }

        return repository.deleteAllPermanently(cartIds, userId)
                .compose(deleted -> {
                    if (!deleted) {
                        return Future.failedFuture(new NotFoundException("Carts not found"));
                    }
                    return evict(userId).map(true);
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deleteAll", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "deleteAll", e.getMessage()));
    }
}