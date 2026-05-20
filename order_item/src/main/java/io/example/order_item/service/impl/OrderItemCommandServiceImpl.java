package io.example.order_item.service.impl;

import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.NotFoundException;
import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.order_item.model.OrderItem;
import io.example.order_item.model.OrderItemResponse;
import io.example.order_item.model.OrderItemResponseDeleteAt;
import io.example.order_item.repository.OrderItemCommandRepository;
import io.example.order_item.service.OrderItemCommandService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import pb.order_item.OrderItemCommand.CreateOrderItemRecordRequest;
import pb.order_item.OrderItemCommand.UpdateOrderItemRecordRequest;

public class OrderItemCommandServiceImpl implements OrderItemCommandService {
    private static final Logger logger = LoggerFactory.getLogger(OrderItemCommandServiceImpl.class);

    private final OrderItemCommandRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "order_item:";

    public OrderItemCommandServiceImpl(
            OrderItemCommandRepository repo,
            RedisService redis,
            TracingMetrics metrics) {
        this.repo = repo;
        this.redis = redis;
        this.metrics = metrics;
    }

    private Future<Void> invalidateCaches(Integer orderId) {
        return redis.delete(CACHE_PREFIX + "order:" + orderId)
                .compose(v -> redis.deleteByPattern(CACHE_PREFIX + "all:*"))
                .compose(v -> redis.deleteByPattern(CACHE_PREFIX + "active:*"))
                .compose(v -> redis.deleteByPattern(CACHE_PREFIX + "trashed:*"))
                .mapEmpty();
    }

    private Future<Void> invalidateAllCaches() {
        return redis.deleteByPattern(CACHE_PREFIX + "*")
                .mapEmpty();
    }

    @Override
    public Future<ApiResponse<OrderItemResponse>> create(CreateOrderItemRecordRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "OrderItemCommandService.create",
                Attributes.builder()
                        .put("order.id", (long) req.getOrderId())
                        .put("product.id", (long) req.getProductId())
                        .build());
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        logger.info("Creating order item for order ID: {}, product ID: {}", req.getOrderId(), req.getProductId());

        return repo.createOrderItem(req.getOrderId(), req.getProductId(), req.getQuantity(), req.getPrice())
                .compose(created -> {
                    span.setAttribute("order_item.id", created.getOrderItemId());
                    return invalidateCaches(req.getOrderId())
                            .onFailure(err -> logger.warn("Failed to invalidate cache: {}", err.getMessage()))
                            .map(created);
                })
                .map(created -> {
                    metrics.completeSpanSuccess(tracingContext, "create", "Order item created successfully");
                    return ApiResponse.success("Order item created successfully", OrderItemResponse.from(created));
                })
                .recover(err -> {
                    logger.error("Failed to create order item", err);
                    metrics.completeSpanError(tracingContext, "create", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<OrderItemResponse>error("Failed to create order item: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<OrderItemResponse>> update(UpdateOrderItemRecordRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "OrderItemCommandService.update",
                Attributes.builder()
                        .put("order_item.id", (long) req.getOrderItemId())
                        .build());

        logger.info("Updating order item ID: {}, quantity: {}, price: {}", req.getOrderItemId(), req.getQuantity(), req.getPrice());

        return repo.updateOrderItem(req.getOrderItemId(), req.getQuantity(), req.getPrice())
                .compose(updated -> {
                    if (updated == null) {
                        return Future.failedFuture(new NotFoundException("Order item not found"));
                    }
                    return invalidateCaches(updated.getOrderId())
                            .onFailure(err -> logger.warn("Failed to invalidate cache: {}", err.getMessage()))
                            .map(updated);
                })
                .map(updated -> {
                    metrics.completeSpanSuccess(tracingContext, "update", "Order item updated successfully");
                    return ApiResponse.success("Order item updated successfully", OrderItemResponse.from(updated));
                })
                .recover(err -> {
                    logger.error("Failed to update order item ID: {}", req.getOrderItemId(), err);
                    metrics.completeSpanError(tracingContext, "update", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<OrderItemResponse>error("Failed to update order item: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<OrderItemResponseDeleteAt>>> trash(Integer orderId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "OrderItemCommandService.trash",
                Attributes.builder()
                        .put("order.id", (long) orderId)
                        .build());

        logger.info("Trashing order items for order ID: {}", orderId);

        return repo.trashOrderItem(orderId)
                .compose(items -> invalidateCaches(orderId)
                        .onFailure(err -> logger.warn("Failed to invalidate cache: {}", err.getMessage()))
                        .map(items))
                .map(items -> {
                    metrics.completeSpanSuccess(tracingContext, "trash", "Order items trashed successfully");
                    List<OrderItemResponseDeleteAt> responses = items.stream()
                            .map(OrderItemResponseDeleteAt::from)
                            .toList();
                    return ApiResponse.success("Order items trashed successfully", responses);
                })
                .recover(err -> {
                    logger.error("Failed to trash order items for order ID: {}", orderId, err);
                    metrics.completeSpanError(tracingContext, "trash", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<List<OrderItemResponseDeleteAt>>error("Failed to trash order items: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<OrderItemResponseDeleteAt>>> restore(Integer orderId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "OrderItemCommandService.restore",
                Attributes.builder()
                        .put("order.id", (long) orderId)
                        .build());

        logger.info("Restoring order items for order ID: {}", orderId);

        return repo.restoreOrderItem(orderId)
                .compose(items -> invalidateCaches(orderId)
                        .onFailure(err -> logger.warn("Failed to invalidate cache: {}", err.getMessage()))
                        .map(items))
                .map(items -> {
                    metrics.completeSpanSuccess(tracingContext, "restore", "Order items restored successfully");
                    List<OrderItemResponseDeleteAt> responses = items.stream()
                            .map(OrderItemResponseDeleteAt::from)
                            .toList();
                    return ApiResponse.success("Order items restored successfully", responses);
                })
                .recover(err -> {
                    logger.error("Failed to restore order items for order ID: {}", orderId, err);
                    metrics.completeSpanError(tracingContext, "restore", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<List<OrderItemResponseDeleteAt>>error("Failed to restore order items: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deletePermanent(Integer orderItemId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "OrderItemCommandService.deletePermanent",
                Attributes.builder()
                        .put("order_item.id", (long) orderItemId)
                        .build());

        logger.info("Permanently deleting order item ID: {}", orderItemId);

        return repo.deleteOrderItemPermanently(orderItemId)
                .compose(v -> invalidateAllCaches()
                        .onFailure(err -> logger.warn("Failed to invalidate cache: {}", err.getMessage()))
                        .mapEmpty())
                .map(v -> {
                    metrics.completeSpanSuccess(tracingContext, "deletePermanent", "Order item deleted permanently");
                    return ApiResponse.<Void>success("Order item deleted permanently", null);
                })
                .recover(err -> {
                    logger.error("Failed to permanently delete order item ID: {}", orderItemId, err);
                    metrics.completeSpanError(tracingContext, "deletePermanent", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to delete order item: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deleteByOrderPermanent(Integer orderId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "OrderItemCommandService.deleteByOrderPermanent",
                Attributes.builder()
                        .put("order.id", (long) orderId)
                        .build());

        logger.info("Permanently deleting order items for order ID: {}", orderId);

        return repo.deleteOrderItemByOrderPermanent(orderId)
                .compose(v -> invalidateCaches(orderId)
                        .onFailure(err -> logger.warn("Failed to invalidate cache: {}", err.getMessage()))
                        .mapEmpty())
                .map(v -> {
                    metrics.completeSpanSuccess(tracingContext, "deleteByOrderPermanent", "Order items deleted permanently");
                    return ApiResponse.<Void>success("Order items deleted permanently", null);
                })
                .recover(err -> {
                    logger.error("Failed to permanently delete order items for order ID: {}", orderId, err);
                    metrics.completeSpanError(tracingContext, "deleteByOrderPermanent", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to delete order items: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> restoreAll() {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderItemCommandService.restoreAll");

        logger.info("Restoring all trashed order items");

        return repo.restoreAllOrderItems()
                .compose(v -> invalidateAllCaches()
                        .onFailure(err -> logger.warn("Failed to invalidate cache: {}", err.getMessage()))
                        .mapEmpty())
                .map(v -> {
                    metrics.completeSpanSuccess(tracingContext, "restoreAll", "All order items restored");
                    return ApiResponse.<Void>success("All order items restored successfully", null);
                })
                .recover(err -> {
                    logger.error("Failed to restore all order items", err);
                    metrics.completeSpanError(tracingContext, "restoreAll", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to restore all order items: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deleteAll() {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderItemCommandService.deleteAll");

        logger.info("Permanently deleting all trashed order items");

        return repo.deleteAllPermanentOrderItems()
                .compose(v -> invalidateAllCaches()
                        .onFailure(err -> logger.warn("Failed to invalidate cache: {}", err.getMessage()))
                        .mapEmpty())
                .map(v -> {
                    metrics.completeSpanSuccess(tracingContext, "deleteAll", "All trashed order items deleted permanently");
                    return ApiResponse.<Void>success("All trashed order items deleted permanently", null);
                })
                .recover(err -> {
                    logger.error("Failed to permanently delete all order items", err);
                    metrics.completeSpanError(tracingContext, "deleteAll", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to delete all order items: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Integer>> calculateTotalPrice(Integer orderId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "OrderItemCommandService.calculateTotalPrice",
                Attributes.builder()
                        .put("order.id", (long) orderId)
                        .build());

        logger.info("Calculating total price for order ID: {}", orderId);

        return repo.calculateTotalPrice(orderId)
                .map(totalPrice -> {
                    metrics.completeSpanSuccess(tracingContext, "calculateTotalPrice", "Total price calculated successfully");
                    return ApiResponse.success("Total price calculated successfully", totalPrice);
                })
                .recover(err -> {
                    logger.error("Failed to calculate total price for order ID: {}", orderId, err);
                    metrics.completeSpanError(tracingContext, "calculateTotalPrice", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Integer>error("Failed to calculate total price: " + err.getMessage()));
                });
    }
}
