package io.example.order_item.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.order_item.model.OrderItemResponse;
import io.example.order_item.model.OrderItemResponseDeleteAt;
import io.example.order_item.repository.OrderItemCommandRepository;
import io.example.order_item.repository.OrderItemQueryRepository;
import io.example.order_item.service.OrderItemCommandService;
import io.example.order_item.domain.requests.CreateOrderItemRecordRequest;
import io.example.order_item.domain.requests.UpdateOrderItemRecordRequest;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderItemCommandServiceImpl implements OrderItemCommandService {
        private static final Logger logger = LoggerFactory.getLogger(OrderItemCommandServiceImpl.class);

        private final OrderItemCommandRepository repo;
        private final OrderItemQueryRepository queryRepository;
        private final RedisService redis;
        private final TracingMetrics metrics;

        private static final String CACHE_PREFIX = "order_item:";

        private Future<Void> evict(Long orderId) {
                return redis.delete(CACHE_PREFIX + "order:" + orderId)
                                .compose(v -> redis.deleteByPattern(CACHE_PREFIX + "all:*"))
                                .compose(v -> redis.deleteByPattern(CACHE_PREFIX + "active:*"))
                                .compose(v -> redis.deleteByPattern(CACHE_PREFIX + "trashed:*"))
                                .<Void>mapEmpty();
        }

        private Future<Void> evictAll() {
                return redis.deleteByPattern(CACHE_PREFIX + "*").<Void>mapEmpty();
        }

        @Override
        public Future<OrderItemResponse> create(CreateOrderItemRecordRequest req) {
                var ctx = metrics.startSpan("OrderItemCommandService.create",
                                Attributes.builder()
                                                .put("order.id", (long) req.getOrderId())
                                                .put("product.id", (long) req.getProductId())
                                                .build());

                return repo.createOrderItem(req)
                                .compose(created -> evict((long) req.getOrderId()).map(v -> created))
                                .map(OrderItemResponse::from)
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "create",
                                                "Order item created successfully"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "create", e.getMessage()));
        }

        @Override
        public Future<OrderItemResponse> update(UpdateOrderItemRecordRequest req) {
                var ctx = metrics.startSpan("OrderItemCommandService.update",
                                Attributes.builder()
                                                .put("order_item.id", (long) req.getOrderItemId())
                                                .build());

                return repo.updateOrderItem(req)
                                .compose(updated -> {
                                        if (updated == null) {
                                                return Future.failedFuture(
                                                                new NotFoundException("Order item not found"));
                                        }
                                        return evict((long) updated.getOrderId()).map(v -> updated);
                                })
                                .map(OrderItemResponse::from)
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "update",
                                                "Order item updated successfully"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "update", e.getMessage()));
        }

        @Override
        public Future<List<OrderItemResponseDeleteAt>> trash(Long orderId) {
                var ctx = metrics.startSpan("OrderItemCommandService.trash",
                                Attributes.builder().put("order.id", orderId).build());

                return repo.trashOrderItem(orderId)
                                .compose(items -> evict(orderId).map(v -> items))
                                .map(items -> items.stream().map(OrderItemResponseDeleteAt::from).toList())
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "trash",
                                                "Order items trashed successfully"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "trash", e.getMessage()));
        }

        @Override
        public Future<List<OrderItemResponseDeleteAt>> restore(Long orderId) {
                var ctx = metrics.startSpan("OrderItemCommandService.restore",
                                Attributes.builder().put("order.id", orderId).build());

                logger.info("Restoring order items for order ID: {}", orderId);

                // restoreOrderItem operates on the order_id and restores all
                // trashed children. Do not pre-check findByTrashedId here: that
                // query is intentionally keyed by order_item_id and would reject
                // a valid order restore when the IDs differ.
                return repo.restoreOrderItem(orderId)
                                .compose(r -> {
                                        if (r == null || r.isEmpty()) {
                                                return Future.failedFuture(new NotFoundException("Order items not found or must be trashed first"));
                                        }
                                        return evict(orderId).map(v -> r);
                                })
                                .map(items -> items.stream().map(OrderItemResponseDeleteAt::from).toList())
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore", "Order items restored successfully"))
                                .onFailure(e -> {
                                        logger.error("Failed to restore order items", e);
                                        metrics.completeSpanError(ctx, "restore", e.getMessage());
                                });
        }

        @Override
        public Future<Void> deletePermanent(Long orderItemId) {
                var ctx = metrics.startSpan("OrderItemCommandService.deletePermanent",
                                Attributes.builder().put("order_item.id", orderItemId).build());

                return queryRepository.findByTrashedId(orderItemId)
                                .compose(trashed -> {
                                        if (trashed == null) {
                                                return Future.<Void>failedFuture(
                                                                new BadRequestException(
                                                                                "Order item not found or must be trashed before permanent deletion"));
                                        }
                                        return repo.deleteOrderItemPermanently(orderItemId)
                                                         .compose(v -> evictAll());
                                })
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deletePermanent",
                                                "Order item deleted permanently"))
                                .onFailure(err -> metrics.completeSpanError(ctx, "deletePermanent", err.getMessage()));
        }

        @Override
        public Future<Void> deleteByOrderPermanent(Long orderId) {
                var ctx = metrics.startSpan("OrderItemCommandService.deleteByOrderPermanent",
                                Attributes.builder().put("order.id", orderId).build());

                return repo.deleteOrderItemByOrderPermanent(orderId)
                                .compose(v -> evict(orderId))
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deleteByOrderPermanent",
                                                "Order items deleted permanently"))
                                .onFailure(err -> metrics.completeSpanError(ctx, "deleteByOrderPermanent",
                                                err.getMessage()));
        }

        @Override
        public Future<Void> restoreAll() {
                var ctx = metrics.startSpan("OrderItemCommandService.restoreAll");

                return repo.restoreAllOrderItems()
                                .compose(count -> {
                                        if (count == 0) {
                                                return Future.<Void>failedFuture(
                                                                new NotFoundException("No trashed order items found"));
                                        }
                                        return evictAll();
                                })
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore_all",
                                                "All order items restored"))
                                .onFailure(err -> metrics.completeSpanError(ctx, "restore_all", err.getMessage()));
        }

        @Override
        public Future<Void> deleteAll() {
                var ctx = metrics.startSpan("OrderItemCommandService.deleteAll");

                return repo.deleteAllPermanentOrderItems()
                                .compose(count -> {
                                        if (count == 0) {
                                                return Future.<Void>failedFuture(
                                                                new NotFoundException("No trashed order items found"));
                                        }
                                        return evictAll();
                                })
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "delete_all_permanent",
                                                "All order items permanently deleted"))
                                .onFailure(err -> metrics.completeSpanError(ctx, "delete_all_permanent",
                                                err.getMessage()));
        }

        @Override
        public Future<BigDecimal> calculateTotalPrice(Long orderId) {
                var ctx = metrics.startSpan("OrderItemCommandService.calculateTotalPrice",
                                Attributes.builder().put("order.id", orderId).build());

                return repo.calculateTotalPrice(orderId)
                                .map(val -> val != null ? BigDecimal.valueOf(val) : BigDecimal.ZERO)
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "calculateTotalPrice",
                                                "Total price calculated"))
                                .onFailure(err -> metrics.completeSpanError(ctx, "calculateTotalPrice",
                                                err.getMessage()));
        }
}