package io.example.order.service.impl;

import java.time.Duration;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.common.exception.NotFoundException;
import io.example.common.exception.BadRequestException;
import io.example.order.model.Order;
import io.example.order.model.OrderResponse;
import io.example.order.model.OrderResponseDeleteAt;
import io.example.order.model.CreateOrderRecord;
import io.example.order.model.UpdateOrderRecord;
import io.example.order.repository.*;
import io.example.order.service.OrderCommandService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import pb.order.OrderCommand.CreateOrderRequest;
import pb.order.OrderCommand.UpdateOrderRequest;

public class OrderCommandServiceImpl implements OrderCommandService {
    private static final Logger logger = LoggerFactory.getLogger(OrderCommandServiceImpl.class);

    private final OrderCommandRepository orderCommandRepo;
    private final OrderQueryRepository orderQueryRepo;
    private final OrderItemCommandRepository orderItemCommandRepo;
    private final OrderItemQueryRepository orderItemQueryRepo;
    private final ShippingAddressCommandRepository shippingAddressCommandRepo;
    private final TransactionCommandRepository transactionCommandRepo;

    private final UserQueryRepository userQueryRepo;
    private final ProductQueryRepository productQueryRepo;
    private final MerchantQueryRepository merchantQueryRepo;
    private final ProductCommandRepository productCommandRepo;

    private final RedisService redis;
    private final TracingMetrics metrics;

    public OrderCommandServiceImpl(
            OrderCommandRepository orderCommandRepo,
            OrderQueryRepository orderQueryRepo,
            OrderItemCommandRepository orderItemCommandRepo,
            OrderItemQueryRepository orderItemQueryRepo,
            ShippingAddressCommandRepository shippingAddressCommandRepo,
            TransactionCommandRepository transactionCommandRepo,
            UserQueryRepository userQueryRepo,
            ProductQueryRepository productQueryRepo,
            MerchantQueryRepository merchantQueryRepo,
            ProductCommandRepository productCommandRepo,
            RedisService redis,
            TracingMetrics metrics) {
        this.orderCommandRepo = orderCommandRepo;
        this.orderQueryRepo = orderQueryRepo;
        this.orderItemCommandRepo = orderItemCommandRepo;
        this.orderItemQueryRepo = orderItemQueryRepo;
        this.shippingAddressCommandRepo = shippingAddressCommandRepo;
        this.transactionCommandRepo = transactionCommandRepo;
        this.userQueryRepo = userQueryRepo;
        this.productQueryRepo = productQueryRepo;
        this.merchantQueryRepo = merchantQueryRepo;
        this.productCommandRepo = productCommandRepo;
        this.redis = redis;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponse<OrderResponse>> createOrder(CreateOrderRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderCommandService.createOrder");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));
        span.setAttribute("merchant.id", (long) req.getMerchantId());
        span.setAttribute("user.id", (long) req.getUserId());

        logger.info("Creating order for merchant: {}, user: {}", req.getMerchantId(), req.getUserId());

        return merchantQueryRepo.findById(req.getMerchantId())
                .compose(merchantExists -> {
                    if (!merchantExists) {
                        return Future.<Boolean>failedFuture(new NotFoundException("Merchant not found"));
                    }
                    return userQueryRepo.findById(req.getUserId());
                })
                .compose(userExists -> {
                    if (!userExists) {
                        return Future.<Order>failedFuture(new NotFoundException("User not found"));
                    }
                    // Validate all products and stock levels
                    @SuppressWarnings("rawtypes")
                    List<Future> validationFutures = req.getItemsList().stream()
                            .map(item -> productQueryRepo.findById(item.getProductId())
                                    .compose(product -> {
                                        if (product == null) {
                                            return Future.failedFuture(new NotFoundException("Product not found: " + item.getProductId()));
                                        }
                                        if (product.getCountInStock() < item.getQuantity()) {
                                            return Future.failedFuture(new BadRequestException("Insufficient stock for product ID: " + item.getProductId()));
                                        }
                                        return Future.succeededFuture(product);
                                    }))
                            .collect(Collectors.toList());

                    Future<CompositeFuture> allFuture = CompositeFuture.all(validationFutures);
                    return allFuture.compose(composite -> {
                        // Create main order row
                        CreateOrderRecord record = new CreateOrderRecord((long) req.getMerchantId(), (long) req.getUserId(), 0);
                        return orderCommandRepo.createOrder(record)
                                .compose(order -> {
                                    // Create order items and deduct stock
                                    Future<Void> itemsFuture = Future.succeededFuture();
                                    for (int i = 0; i < req.getItemsCount(); i++) {
                                        var item = req.getItems(i);
                                        var product = (io.example.order.model.ProductInfo) composite.list().get(i);
                                        itemsFuture = itemsFuture.compose(v -> 
                                            orderItemCommandRepo.createOrderItem((long) order.getOrderId(), item.getProductId(), item.getQuantity(), product.getPrice())
                                                    .compose(orderItem -> productCommandRepo.updateProductCountStock(item.getProductId(), product.getCountInStock() - item.getQuantity()))
                                                    .mapEmpty()
                                        );
                                    }

                                    // Create shipping address
                                    var shipping = req.getShipping();
                                    return itemsFuture.compose(v -> 
                                        shippingAddressCommandRepo.createShippingAddress(
                                                (long) order.getOrderId(),
                                                shipping.getAlamat(),
                                                shipping.getProvinsi(),
                                                shipping.getNegara(),
                                                shipping.getKota(),
                                                shipping.getCourier(),
                                                shipping.getShippingMethod(),
                                                shipping.getShippingCost()
                                        )
                                    ).compose(v -> 
                                        // Calculate total price and update order
                                        orderItemQueryRepo.calculateTotalPrice(order.getOrderId().intValue())
                                                .compose(totalPrice -> orderCommandRepo.updateOrder(new UpdateOrderRecord((long) order.getOrderId(), totalPrice)))
                                    );
                                });
                    });
                })
                .compose(updatedOrder -> {
                    span.setAttribute("order.id", (long) updatedOrder.getOrderId());
                    // Invalidate caches
                    return invalidateCache(updatedOrder.getOrderId(), req.getMerchantId())
                            .map(v -> ApiResponse.success("Order created successfully", OrderResponse.from(updatedOrder)));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "create_order", "Success"))
                .recover(err -> {
                    logger.error("Failed to create order", err);
                    metrics.completeSpanError(tracingContext, "create_order", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to create order: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<OrderResponse>> updateOrder(UpdateOrderRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderCommandService.updateOrder");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));
        span.setAttribute("order.id", (long) req.getOrderId());
        span.setAttribute("user.id", (long) req.getUserId());

        logger.info("Updating order: {}", req.getOrderId());

        return orderQueryRepo.getOrderById((long) req.getOrderId())
                .compose(existingOrder -> {
                    if (existingOrder == null) {
                        return Future.<Order>failedFuture(new NotFoundException("Order not found"));
                    }
                    return userQueryRepo.findById(req.getUserId())
                            .map(userExists -> {
                                if (!userExists) {
                                    throw new NotFoundException("User not found");
                                }
                                return existingOrder;
                            });
                })
                .compose(existingOrder -> {
                    // Update/Create items
                    Future<Void> itemsFuture = Future.succeededFuture();
                    for (var item : req.getItemsList()) {
                        itemsFuture = itemsFuture.compose(v -> {
                            if (item.getOrderItemId() > 0) {
                                return productQueryRepo.findById(item.getProductId())
                                        .compose(product -> {
                                            if (product == null) {
                                                return Future.failedFuture(new NotFoundException("Product not found: " + item.getProductId()));
                                            }
                                            return orderItemCommandRepo.updateOrderItem((long) item.getOrderItemId(), item.getQuantity(), product.getPrice())
                                                    .mapEmpty();
                                        });
                            } else {
                                return productQueryRepo.findById(item.getProductId())
                                        .compose(product -> {
                                            if (product == null) {
                                                return Future.failedFuture(new NotFoundException("Product not found: " + item.getProductId()));
                                            }
                                            if (product.getCountInStock() < item.getQuantity()) {
                                                return Future.failedFuture(new BadRequestException("Insufficient stock for product ID: " + item.getProductId()));
                                            }
                                            return orderItemCommandRepo.createOrderItem((long) req.getOrderId(), item.getProductId(), item.getQuantity(), product.getPrice())
                                                    .compose(orderItem -> productCommandRepo.updateProductCountStock(item.getProductId(), product.getCountInStock() - item.getQuantity()))
                                                    .mapEmpty();
                                        });
                            }
                        });
                    }

                    // Update shipping address
                    var shipping = req.getShipping();
                    return itemsFuture.compose(v -> 
                        shippingAddressCommandRepo.updateShippingAddress(
                                (long) shipping.getShippingId(),
                                shipping.getAlamat(),
                                shipping.getProvinsi(),
                                shipping.getNegara(),
                                shipping.getKota(),
                                shipping.getCourier(),
                                shipping.getShippingMethod(),
                                shipping.getShippingCost()
                        )
                    ).compose(v -> 
                        // Recalculate price and update order total
                        orderItemQueryRepo.calculateTotalPrice(req.getOrderId())
                                .compose(totalPrice -> orderCommandRepo.updateOrder(new UpdateOrderRecord((long) req.getOrderId(), totalPrice)))
                    );
                })
                .compose(updatedOrder -> {
                    return invalidateCache(updatedOrder.getOrderId(), updatedOrder.getMerchantId())
                            .map(v -> ApiResponse.success("Order updated successfully", OrderResponse.from(updatedOrder)));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "update_order", "Success"))
                .recover(err -> {
                    logger.error("Failed to update order", err);
                    metrics.completeSpanError(tracingContext, "update_order", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to update order: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<OrderResponseDeleteAt>> trash(Long id) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderCommandService.trash");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));
        span.setAttribute("order.id", id);

        return orderCommandRepo.trashOrder(id)
                .compose(order -> {
                    if (order == null) {
                        return Future.failedFuture(new NotFoundException("Order not found with ID: " + id));
                    }
                    return orderItemCommandRepo.trashOrderItem(id.intValue())
                            .compose(items -> invalidateCache(id, order.getMerchantId()))
                            .map(v -> ApiResponse.success("Order trashed successfully", OrderResponseDeleteAt.from(order)));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "trash_order", "Success"))
                .recover(err -> {
                    logger.error("Failed to trash order", err);
                    metrics.completeSpanError(tracingContext, "trash_order", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to trash order: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<OrderResponseDeleteAt>> restore(Long id) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderCommandService.restore");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));
        span.setAttribute("order.id", id);

        return orderCommandRepo.restoreOrder(id)
                .compose(order -> {
                    if (order == null) {
                        return Future.failedFuture(new NotFoundException("Order not found with ID: " + id));
                    }
                    return orderItemCommandRepo.restoreOrderItem(id.intValue())
                            .compose(items -> invalidateCache(id, order.getMerchantId()))
                            .map(v -> ApiResponse.success("Order restored successfully", OrderResponseDeleteAt.from(order)));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "restore_order", "Success"))
                .recover(err -> {
                    logger.error("Failed to restore order", err);
                    metrics.completeSpanError(tracingContext, "restore_order", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to restore order: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Boolean>> deletePermanent(Long id) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderCommandService.deletePermanent");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));
        span.setAttribute("order.id", id);

        return orderQueryRepo.getOrderById(id)
                .compose(order -> {
                    if (order == null) {
                        return Future.failedFuture(new NotFoundException("Order not found with ID: " + id));
                    }
                    // Cascade delete order_items, shipping_address, transaction and finally the order itself
                    return orderItemCommandRepo.deleteOrderItemPermanently(id.intValue())
                            .compose(v -> shippingAddressCommandRepo.deleteShippingAddressPermanently(id.intValue()))
                            .compose(v -> transactionCommandRepo.deleteByOrderIDPermanent(id.intValue()))
                            .compose(v -> orderCommandRepo.deleteOrderPermanently(id))
                            .compose(v -> invalidateCache(id, order.getMerchantId()))
                            .map(v -> ApiResponse.success("Order deleted permanently", true));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "delete_permanent", "Success"))
                .recover(err -> {
                    logger.error("Failed to delete order permanently", err);
                    metrics.completeSpanError(tracingContext, "delete_permanent", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to delete permanently: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Integer>> restoreAll() {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderCommandService.restoreAll");

        return orderCommandRepo.restoreAllOrders()
                .compose(count -> orderItemCommandRepo.restoreAllOrderItems()
                        .compose(v -> invalidateAllCaches())
                        .map(v -> ApiResponse.success("All orders restored successfully", count)))
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "restore_all", "Success"))
                .recover(err -> {
                    logger.error("Failed to restore all orders", err);
                    metrics.completeSpanError(tracingContext, "restore_all", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to restore all: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Integer>> deleteAllPermanent() {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderCommandService.deleteAllPermanent");

        return orderCommandRepo.deleteAllPermanentOrders()
                .compose(count -> orderItemCommandRepo.deleteAllPermanentOrderItems()
                        .compose(v -> shippingAddressCommandRepo.deleteAllShippingAddress())
                        .compose(v -> transactionCommandRepo.deleteAll())
                        .compose(v -> invalidateAllCaches())
                        .map(v -> ApiResponse.success("All orders deleted permanently", count)))
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "delete_all_permanent", "Success"))
                .recover(err -> {
                    logger.error("Failed to delete all orders permanently", err);
                    metrics.completeSpanError(tracingContext, "delete_all_permanent", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to delete all: " + err.getMessage()));
                });
    }

    private Future<Void> invalidateCache(Long orderId, Integer merchantId) {
        String keyOrder = "order:id:" + orderId;
        String patPrefixAll = "order:all:*";
        String patPrefixActive = "order:active:*";
        String patPrefixTrashed = "order:trashed:*";
        String patPrefixMerchant = "order:merchant:" + merchantId + ":*";

        return redis.delete(keyOrder)
                .compose(v -> redis.deleteByPattern(patPrefixAll))
                .compose(v -> redis.deleteByPattern(patPrefixActive))
                .compose(v -> redis.deleteByPattern(patPrefixTrashed))
                .compose(v -> redis.deleteByPattern(patPrefixMerchant))
                .mapEmpty();
    }

    private Future<Void> invalidateAllCaches() {
        return redis.deleteByPattern("order:*").mapEmpty();
    }
}
