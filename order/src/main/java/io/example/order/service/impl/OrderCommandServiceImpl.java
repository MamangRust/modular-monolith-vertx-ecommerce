package io.example.order.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.order.domain.requests.CreateOrderItemRecordRequest;
import io.example.order.domain.requests.CreateOrderRecordRequest;
import io.example.order.domain.requests.CreateOrderRequest;
import io.example.order.domain.requests.CreateShippingAddressRequest;
import io.example.order.domain.requests.UpdateOrderItemRecordRequest;
import io.example.order.domain.requests.UpdateOrderRecordRequest;
import io.example.order.domain.requests.UpdateOrderRequest;
import io.example.order.domain.requests.UpdateShippingAddressRequest;
import io.example.order.model.Order;
import io.example.order.model.OrderResponse;
import io.example.order.model.OrderResponseDeleteAt;
import io.example.order.model.ShippingAddress;
import io.example.order.repository.MerchantQueryRepository;
import io.example.order.repository.OrderCommandRepository;
import io.example.order.repository.OrderItemCommandRepository;
import io.example.order.repository.OrderItemQueryRepository;
import io.example.order.repository.OrderQueryRepository;
import io.example.order.repository.ProductCommandRepository;
import io.example.order.repository.ProductQueryRepository;
import io.example.order.repository.ShippingAddressCommandRepository;
import io.example.order.repository.TransactionCommandRepository;
import io.example.order.repository.UserQueryRepository;
import io.example.order.service.OrderCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
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

    private Future<Void> evict(Long orderId, Integer merchantId) {
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

    private Future<Void> evictAll() {
        return redis.deleteByPattern("order:*").mapEmpty();
    }

    @Override
    public Future<OrderResponse> createOrder(CreateOrderRequest req) {
        var ctx = metrics.startSpan("OrderCommandService.createOrder",
                Attributes.builder()
                        .put("merchant.id", (long) req.getMerchantId())
                        .put("user.id", (long) req.getUserId())
                        .build());

        logger.info("Creating order for merchant: {}, user: {}", req.getMerchantId(), req.getUserId());

        return merchantQueryRepo.findById(req.getMerchantId().intValue())
                .<Boolean>compose(merchantExists -> {
                    if (!merchantExists) {
                        return Future.<Boolean>failedFuture(new NotFoundException("Merchant not found"));
                    }
                    return userQueryRepo.findById(req.getUserId());
                })
                .<Order>compose(userExists -> {
                    if (!userExists) {
                        return Future.<Order>failedFuture(new NotFoundException("User not found"));
                    }
                    List<Future<io.example.order.model.ProductInfo>> validationFutures = req.getItems().stream()
                            .map(item -> productQueryRepo.findById(item.getProductId().intValue())
                                    .compose(product -> {
                                        if (product == null) {
                                            return Future.<io.example.order.model.ProductInfo>failedFuture(
                                                    new NotFoundException("Product not found: " + item.getProductId()));
                                        }
                                        if (product.getCountInStock() < item.getQuantity()) {
                                            return Future.<io.example.order.model.ProductInfo>failedFuture(
                                                    new BadRequestException(
                                                            "Insufficient stock for product ID: "
                                                                    + item.getProductId()));
                                        }
                                        return Future.succeededFuture(product);
                                    }))
                            .collect(Collectors.toList());

                    Future<CompositeFuture> allFuture = Future.all(validationFutures);
                    return allFuture.<Order>compose(composite -> {
                        CreateOrderRecordRequest record = CreateOrderRecordRequest.builder()
                                .merchantId((long) req.getMerchantId())
                                .userId((long) req.getUserId())
                                .totalPrice(0)
                                .build();
                        return orderCommandRepo.createOrder(record)
                                .<Order>compose(order -> {
                                    Future<Void> itemsFuture = Future.succeededFuture();
                                    for (int i = 0; i < req.getItems().size(); i++) {
                                        var item = req.getItems().get(i);
                                        var product = (io.example.order.model.ProductInfo) composite.list().get(i);
                                        itemsFuture = itemsFuture.compose(v -> orderItemCommandRepo
                                                .createOrderItem(CreateOrderItemRecordRequest.builder()
                                                        .orderId((long) order.getOrderId())
                                                        .productId((long) item.getProductId())
                                                        .quantity(item.getQuantity())
                                                        .price(product.getPrice())
                                                        .build())
                                                .compose(orderItem -> productCommandRepo.updateProductCountStock(
                                                        item.getProductId().intValue(),
                                                        product.getCountInStock() - item.getQuantity()))
                                                .mapEmpty());
                                    }

                                    var shipping = req.getShippingAddress();
                                    return itemsFuture
                                            .<ShippingAddress>compose(
                                                    v -> shippingAddressCommandRepo.createShippingAddress(
                                                            CreateShippingAddressRequest.builder()
                                                                    .orderId((long) order.getOrderId())
                                                                    .alamat(shipping.getAlamat())
                                                                    .provinsi(shipping.getProvinsi())
                                                                    .negara(shipping.getNegara())
                                                                    .kota(shipping.getKota())
                                                                    .courier(shipping.getCourier())
                                                                    .shippingMethod(shipping.getShippingMethod())
                                                                    .shippingCost(shipping.getShippingCost())
                                                                    .build()))
                                            .<Order>compose(v -> orderItemQueryRepo
                                                    .calculateTotalPrice(order.getOrderId().intValue())
                                                    .compose(totalPrice -> orderCommandRepo.updateOrder(
                                                            UpdateOrderRecordRequest.builder()
                                                                    .orderId((long) order.getOrderId())
                                                                    .totalPrice(totalPrice)
                                                                    .build())));
                                });
                    });
                })
                .compose(updatedOrder -> evict(updatedOrder.getOrderId(), req.getMerchantId().intValue())
                        .map(v -> updatedOrder))
                .map(OrderResponse::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "createOrder", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to create order", e);
                    metrics.completeSpanError(ctx, "createOrder", e.getMessage());
                });
    }

    @Override
    public Future<OrderResponse> updateOrder(UpdateOrderRequest req) {
        var ctx = metrics.startSpan("OrderCommandService.updateOrder",
                Attributes.builder()
                        .put("order.id", (long) req.getOrderId())
                        .put("user.id", (long) req.getUserId())
                        .build());

        logger.info("Updating order: {}", req.getOrderId());

        return orderQueryRepo.getOrderById(req.getOrderId())
                .<Order>compose(existingOrder -> {
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
                .<Order>compose(existingOrder -> {
                    Future<Void> itemsFuture = Future.succeededFuture();
                    for (var item : req.getItems()) {
                        itemsFuture = itemsFuture.compose(v -> {
                            if (item.getOrderItemId() > 0) {
                                return productQueryRepo.findById(item.getProductId().intValue())
                                        .compose(product -> {
                                            if (product == null) {
                                                return Future.<Void>failedFuture(new NotFoundException(
                                                        "Product not found: " + item.getProductId()));
                                            }
                                            return orderItemCommandRepo
                                                    .updateOrderItem(UpdateOrderItemRecordRequest.builder()
                                                            .orderItemId((long) item.getOrderItemId())
                                                            .quantity(item.getQuantity())
                                                            .price(product.getPrice())
                                                            .build())
                                                    .mapEmpty();
                                        });
                            } else {
                                return productQueryRepo.findById(item.getProductId().intValue())
                                        .compose(product -> {
                                            if (product == null) {
                                                return Future.<Void>failedFuture(new NotFoundException(
                                                        "Product not found: " + item.getProductId()));
                                            }
                                            if (product.getCountInStock() < item.getQuantity()) {
                                                return Future.<Void>failedFuture(new BadRequestException(
                                                        "Insufficient stock for product ID: " + item.getProductId()));
                                            }
                                            return orderItemCommandRepo
                                                    .createOrderItem(CreateOrderItemRecordRequest.builder()
                                                            .orderId((long) req.getOrderId())
                                                            .productId((long) item.getProductId())
                                                            .quantity(item.getQuantity())
                                                            .price(product.getPrice())
                                                            .build())
                                                    .compose(orderItem -> productCommandRepo.updateProductCountStock(
                                                            item.getProductId().intValue(),
                                                            product.getCountInStock() - item.getQuantity()))
                                                    .mapEmpty();
                                        });
                            }
                        });
                    }

                    var shipping = req.getShippingAddress();
                    return itemsFuture.<ShippingAddress>compose(v -> shippingAddressCommandRepo.updateShippingAddress(
                            UpdateShippingAddressRequest.builder()
                                    .shippingId((long) shipping.getShippingId())
                                    .orderId((long) req.getOrderId())
                                    .alamat(shipping.getAlamat())
                                    .provinsi(shipping.getProvinsi())
                                    .negara(shipping.getNegara())
                                    .kota(shipping.getKota())
                                    .courier(shipping.getCourier())
                                    .shippingMethod(shipping.getShippingMethod())
                                    .shippingCost(shipping.getShippingCost())
                                    .build()))
                            .<Order>compose(v -> orderItemQueryRepo.calculateTotalPrice(req.getOrderId().intValue())
                                    .compose(totalPrice -> orderCommandRepo
                                            .updateOrder(UpdateOrderRecordRequest.builder()
                                                    .orderId((long) req.getOrderId())
                                                    .totalPrice(totalPrice)
                                                    .build())));
                })
                .compose(updatedOrder -> evict(updatedOrder.getOrderId(), updatedOrder.getMerchantId())
                        .map(v -> updatedOrder))
                .map(OrderResponse::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "updateOrder", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to update order", e);
                    metrics.completeSpanError(ctx, "updateOrder", e.getMessage());
                });
    }

    @Override
    public Future<OrderResponseDeleteAt> trash(Long id) {
        var ctx = metrics.startSpan("OrderCommandService.trash",
                Attributes.builder().put("order.id", id).build());

        logger.info("Trashing order: {}", id);

        return orderCommandRepo.trashOrder(id)
                .compose(order -> {
                    if (order == null) {
                        return Future.failedFuture(new NotFoundException("Order not found with ID: " + id));
                    }
                    return orderItemCommandRepo.trashOrderItem(id)
                            .compose(items -> evict(id, order.getMerchantId()))
                            .map(v -> order);
                })
                .map(OrderResponseDeleteAt::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "trash", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to trash order", e);
                    metrics.completeSpanError(ctx, "trash", e.getMessage());
                });
    }

    @Override
    public Future<OrderResponseDeleteAt> restore(Long id) {
        var ctx = metrics.startSpan("OrderCommandService.restore",
                Attributes.builder().put("order.id", id).build());

        logger.info("Restoring order: {}", id);

        return orderQueryRepo.findByTrashedId(id)
                .compose(trashed -> {
                    if (trashed == null) {
                        return Future.failedFuture(new BadRequestException("Order not found or must be trashed first"));
                    }
                    return orderCommandRepo.restoreOrder(id);
                })
                .compose(order -> {
                    if (order == null) {
                        return Future.failedFuture(new NotFoundException("Order not found"));
                    }
                    return orderItemCommandRepo.restoreOrderItem(id)
                            .compose(items -> evict(id, order.getMerchantId()))
                            .map(v -> order);
                })
                .map(OrderResponseDeleteAt::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to restore order", e);
                    metrics.completeSpanError(ctx, "restore", e.getMessage());
                });
    }

    @Override
    public Future<Void> deletePermanent(Long id) {
        var ctx = metrics.startSpan("OrderCommandService.deletePermanent",
                Attributes.builder().put("order.id", id).build());

        logger.info("Deleting order permanently: {}", id);

        return orderQueryRepo.findByTrashedId(id)
                .compose(order -> {
                    if (order == null) {
                        return Future.failedFuture(new NotFoundException("Order not found with ID: " + id));
                    }
                    return orderItemCommandRepo.deleteOrderItemPermanently(id)
                            .compose(v -> shippingAddressCommandRepo.deleteShippingAddressPermanently(id))
                            .compose(v -> transactionCommandRepo.deleteByOrderIDPermanent(id))
                            .compose(v -> orderCommandRepo.deleteOrderPermanently(id))
                            .compose(v -> evict(id, order.getMerchantId()));
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deletePermanent", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to delete order permanently", e);
                    metrics.completeSpanError(ctx, "deletePermanent", e.getMessage());
                });
    }

    @Override
    public Future<Void> restoreAll() {
        var ctx = metrics.startSpan("OrderCommandService.restoreAll");

        logger.info("Restoring all orders");

        return orderCommandRepo.restoreAllOrders()
                .compose(count -> {
                    if (count == 0) {
                        return Future.<Void>failedFuture(new NotFoundException("No trashed orders found"));
                    }
                    return orderItemCommandRepo.restoreAllOrderItems()
                            .compose(v -> evictAll());
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restoreAll", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to restore all orders", e);
                    metrics.completeSpanError(ctx, "restoreAll", e.getMessage());
                });
    }

    @Override
    public Future<Void> deleteAllPermanent() {
        var ctx = metrics.startSpan("OrderCommandService.deleteAllPermanent");

        logger.info("Deleting all orders permanently");

        return orderCommandRepo.deleteAllPermanentOrders()
                .compose(count -> {
                    if (count == 0) {
                        return Future.<Void>failedFuture(new NotFoundException("No trashed orders found"));
                    }
                    return orderItemCommandRepo.deleteAllPermanentOrderItems()
                            .compose(v -> shippingAddressCommandRepo.deleteAllShippingAddress())
                            .compose(v -> transactionCommandRepo.deleteAll())
                            .compose(v -> evictAll());
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deleteAllPermanent", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to delete all orders permanently", e);
                    metrics.completeSpanError(ctx, "deleteAllPermanent", e.getMessage());
                });
    }
}