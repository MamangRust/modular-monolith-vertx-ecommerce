package io.example.order.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import io.example.order.model.OrderItem;
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
        String keyOrder = "order:" + orderId;
        String listPattern = "order:list:*";
        String merchantPattern = "order:list:merchant:" + merchantId + ":*";

        return redis.delete(keyOrder)
                .compose(v -> redis.deleteByPattern(listPattern))
                .compose(v -> redis.deleteByPattern(merchantPattern))
                .<Void>mapEmpty()
                .recover(err -> {
                    logger.warn("Order cache eviction failed: {}", err.getMessage());
                    return Future.<Void>succeededFuture();
                });
    }

    private int withTax(Integer itemTotal, Integer shippingCost) {
        int subtotal = (itemTotal == null ? 0 : itemTotal) + (shippingCost == null ? 0 : shippingCost);
        return subtotal + (subtotal * 11 / 100);
    }

    private Future<Void> evictAll() {
        return redis.deleteByPattern("order:*")
                .<Void>mapEmpty()
                .recover(err -> {
                    logger.warn("Order cache eviction failed: {}", err.getMessage());
                    return Future.<Void>succeededFuture();
                });
    }

    /**
     * Atomically decrement stock for all order items via gRPC to the product
     * service. Each decrement is an atomic SQL operation (count_in_stock - qty
     * WITH count_in_stock >= qty guard), so concurrent requests cannot oversell.
     * If any item has insufficient stock the whole chain fails immediately.
     */
    private Future<Void> decrementAllStocks(List<CreateOrderItemRecordRequest> items,
            List<CreateOrderItemRecordRequest> completed) {
        Future<Void> chain = Future.succeededFuture();
        for (var item : items) {
            chain = chain.compose(v -> productCommandRepo
                    .decrementStock(item.getProductId().intValue(), item.getQuantity().intValue())
                    .onSuccess(ignored -> completed.add(item))
                    .recover(err -> compensateStocks(completed)
                            .compose(ignored -> Future.<Void>failedFuture(err))
                            .recover(compensationError -> {
                                logger.error("Stock decrement compensation failed", compensationError);
                                return Future.failedFuture(err);
                            })));
        }
        return chain;
    }

    private Future<Void> compensateStocks(List<CreateOrderItemRecordRequest> items) {
        Future<Void> chain = Future.succeededFuture();
        for (var item : items) {
            chain = chain.compose(v -> productCommandRepo.incrementStock(
                    item.getProductId().intValue(), item.getQuantity().intValue()));
        }
        return chain;
    }

    static int calculateStockDelta(int oldQuantity, int newQuantity) {
        return newQuantity - oldQuantity;
    }

    private static final class StockAdjustment {
        private final Integer productId;
        private final int reservedDelta;

        private StockAdjustment(Integer productId, int reservedDelta) {
            this.productId = productId;
            this.reservedDelta = reservedDelta;
        }
    }

    private static final class ItemChange {
        private final Long orderItemId;
        private final Integer quantity;
        private final Integer price;

        private ItemChange(Long orderItemId, Integer quantity, Integer price) {
            this.orderItemId = orderItemId;
            this.quantity = quantity;
            this.price = price;
        }
    }

    private Future<Void> rollbackItemChanges(List<ItemChange> changes) {
        Future<Void> chain = Future.succeededFuture();
        for (int i = changes.size() - 1; i >= 0; i--) {
            ItemChange change = changes.get(i);
            chain = chain.compose(v -> orderItemCommandRepo.updateOrderItem(
                    UpdateOrderItemRecordRequest.builder()
                            .orderItemId(change.orderItemId)
                            .quantity(change.quantity)
                            .price(change.price)
                            .build())
                    .mapEmpty());
        }
        return chain;
    }

    private Future<Void> removeCreatedItems(List<Long> createdItemIds) {
        Future<Void> chain = Future.succeededFuture();
        for (Long itemId : createdItemIds) {
            chain = chain.compose(v -> orderItemCommandRepo.deleteOrderItemByIdPermanently(itemId));
        }
        return chain;
    }

    private Future<Void> rollbackUpdate(List<ItemChange> changedItems,
            List<Long> createdItemIds, List<StockAdjustment> adjustments) {
        return rollbackItemChanges(changedItems)
                .recover(err -> {
                    logger.error("Failed to rollback existing order items", err);
                    return Future.<Void>succeededFuture();
                })
                .compose(v -> removeCreatedItems(createdItemIds)
                        .recover(err -> {
                            logger.error("Failed to remove newly created order items", err);
                            return Future.<Void>succeededFuture();
                        }))
                .compose(v -> compensateAdjustments(adjustments)
                        .recover(err -> {
                            logger.error("Failed to compensate stock adjustments", err);
                            return Future.<Void>succeededFuture();
                        }))
                .mapEmpty();
    }

    private Future<Void> applyStockAdjustment(Integer productId, int reservedDelta,
            List<StockAdjustment> applied) {
        if (reservedDelta == 0) {
            return Future.succeededFuture();
        }
        Future<Void> operation = reservedDelta > 0
                ? productCommandRepo.decrementStock(productId, reservedDelta)
                : productCommandRepo.incrementStock(productId, -reservedDelta);
        return operation.onSuccess(v -> applied.add(new StockAdjustment(productId, reservedDelta)));
    }

    private Future<Void> compensateAdjustments(List<StockAdjustment> adjustments) {
        Future<Void> chain = Future.succeededFuture();
        for (int i = adjustments.size() - 1; i >= 0; i--) {
            StockAdjustment adjustment = adjustments.get(i);
            int inverse = -adjustment.reservedDelta;
            chain = chain.compose(v -> inverse > 0
                    ? productCommandRepo.decrementStock(adjustment.productId, inverse)
                    : productCommandRepo.incrementStock(adjustment.productId, -inverse));
        }
        return chain;
    }

    /**
     * Mencatat satu perubahan stok (decrement/increment) yang berhasil agar
     * bisa dikompensasi jika operasi utama gagal.
     */
    private static final class StockDecrement {
        private final Integer productId;
        private final int quantity;

        private StockDecrement(Integer productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }

    /**
     * Increment balik (kompensasi) semua stok yang sudah didecrement.
     * <p>
     * Setiap item yang berhasil dikompensasi di-remove dari {@code applied},
     * sehingga pemanggilan kedua (recover lapis atas) tidak pernah
     * double-increment stok.
     *
     * @param swallowErrors {@code true} → error kompensasi di-log & di-swallow
     *                      agar error asli tetap menang; {@code false} → error
     *                      dipropagasi (dipakai undo setelah kalah race
     *                      restore-all, di mana tidak ada error asli).
     */
    private Future<Void> restoreStockDecrements(List<StockDecrement> applied, boolean swallowErrors) {
        List<StockDecrement> snapshot = new ArrayList<>(applied);
        Future<Void> chain = Future.succeededFuture();
        for (StockDecrement decrement : snapshot) {
            chain = chain.compose(v -> productCommandRepo
                    .incrementStock(decrement.productId, decrement.quantity)
                    .onSuccess(ignored -> applied.remove(decrement))
                    .recover(err -> {
                        if (swallowErrors) {
                            logger.warn("Stock compensation failed for product {}: {}",
                                    decrement.productId, err.getMessage());
                            return Future.succeededFuture();
                        }
                        return Future.failedFuture(err);
                    }));
        }
        return chain;
    }

    /**
     * Decrement balik semua stok yang sudah di-restore (increment) saat trash
     * order gagal. Item yang berhasil di-revert di-remove dari {@code restored}
     * agar tidak ada double-decrement pada recover lapis atas.
     */
    private Future<Void> revertStockRestores(List<StockDecrement> restored) {
        List<StockDecrement> snapshot = new ArrayList<>(restored);
        Future<Void> chain = Future.succeededFuture();
        for (StockDecrement restore : snapshot) {
            chain = chain.compose(v -> productCommandRepo
                    .decrementStock(restore.productId, restore.quantity)
                    .onSuccess(ignored -> restored.remove(restore)));
        }
        return chain;
    }

    /**
     * Decrement stok semua item aktif (dipakai restore order / restore-all).
     * Setiap sukses dicatat ke {@code applied} untuk kompensasi. Kompensasi
     * bila salah satu gagal ditangani di recover method utama (satu titik).
     */
    private Future<Void> decrementStockForOrderItems(List<OrderItem> items,
            List<StockDecrement> applied) {
        Future<Void> chain = Future.succeededFuture();
        for (OrderItem item : items) {
            chain = chain.compose(v -> productCommandRepo
                    .decrementStock(item.getProductId().intValue(), item.getQuantity().intValue())
                    .onSuccess(ignored -> applied.add(new StockDecrement(
                            item.getProductId().intValue(), item.getQuantity().intValue()))));
        }
        return chain;
    }

    /**
     * Increment (restore) stok semua item aktif — dipakai trash order. Setiap
     * sukses dicatat ke {@code restored}. Revert bila gagal ditangani di
     * recover method utama (satu titik).
     */
    private Future<Void> restoreStockForOrderItems(List<OrderItem> items,
            List<StockDecrement> restored) {
        Future<Void> chain = Future.succeededFuture();
        for (OrderItem item : items) {
            chain = chain.compose(v -> productCommandRepo
                    .incrementStock(item.getProductId().intValue(), item.getQuantity().intValue())
                    .onSuccess(ignored -> restored.add(new StockDecrement(
                            item.getProductId().intValue(), item.getQuantity().intValue()))));
        }
        return chain;
    }

    @Override
    public Future<OrderResponse> createOrder(CreateOrderRequest req) {
        if (req == null || req.getMerchantId() == null || req.getMerchantId() <= 0
                || req.getUserId() == null || req.getUserId() <= 0
                || req.getItems() == null || req.getItems().isEmpty()
                || req.getShippingAddress() == null) {
            return Future.failedFuture(new BadRequestException(
                    "Merchant, user, items, and shipping address are required"));
        }
        for (var item : req.getItems()) {
            if (item == null || item.getProductId() == null || item.getProductId() <= 0
                    || item.getQuantity() == null || item.getQuantity() <= 0) {
                return Future.failedFuture(new BadRequestException(
                        "Each order item requires a valid product and positive quantity"));
            }
        }

        var ctx = metrics.startSpan("OrderCommandService.createOrder",
                Attributes.builder()
                        .put("merchant.id", (long) req.getMerchantId())
                        .put("user.id", (long) req.getUserId())
                        .build());

        logger.info("Creating order for merchant: {}, user: {}", req.getMerchantId(), req.getUserId());

        List<CreateOrderItemRecordRequest> decrementedStocks = new java.util.ArrayList<>();

        return merchantQueryRepo.findById(req.getMerchantId().intValue())
                .<Boolean>compose(merchantExists -> {
                    if (!merchantExists) {
                        return Future.<Boolean>failedFuture(new NotFoundException("Merchant not found"));
                    }
                    return userQueryRepo.findById(req.getUserId());
                })
                .compose(userExists -> {
                    if (!userExists) {
                        return Future.failedFuture(new NotFoundException("User not found"));
                    }

                    // 1. Validate all products exist (read-only, no mutation yet)
                    List<Future<io.example.order.model.ProductInfo>> existenceFutures = req.getItems().stream()
                            .map(item -> productQueryRepo.findById(item.getProductId().intValue())
                                    .compose(product -> {
                                        if (product == null) {
                                            return Future.<io.example.order.model.ProductInfo>failedFuture(
                                                    new NotFoundException("Product not found: " + item.getProductId()));
                                        }
                                        return Future.succeededFuture(product);
                                    }))
                            .collect(Collectors.toList());

                    return Future.<List<io.example.order.model.ProductInfo>>all(existenceFutures)
                            .map(CompositeFuture::list);
                })
                .compose(products -> {
                    // 2. Atomically decrement stock for every item.
                    //    The gRPC call performs a single atomic SQL update with a
                    //    count_in_stock >= quantity guard, preventing race conditions.
                    List<CreateOrderItemRecordRequest> itemRecords = req.getItems().stream()
                            .map(item -> CreateOrderItemRecordRequest.builder()
                                    .productId((long) item.getProductId())
                                    .quantity(item.getQuantity())
                                    .price(0) // will be set below
                                    .build())
                            .collect(Collectors.toList());

                    return decrementAllStocks(itemRecords, decrementedStocks)
                            .map(v -> products);
                })
                .<Order>compose(products -> {
                    // 3. All stocks decremented successfully — create the order.
                    CreateOrderRecordRequest record = CreateOrderRecordRequest.builder()
                            .merchantId((long) req.getMerchantId())
                            .userId((long) req.getUserId())
                            .totalPrice(0)
                            .build();

                    return orderCommandRepo.createOrder(record)
                            .<Order>compose(order -> {
                                // 4. Create order items with correct prices
                                Future<Void> itemsFuture = Future.succeededFuture();
                                for (int i = 0; i < req.getItems().size(); i++) {
                                    var item = req.getItems().get(i);
                                    var product = (io.example.order.model.ProductInfo) products.get(i);
                                    itemsFuture = itemsFuture.compose(v -> orderItemCommandRepo
                                            .createOrderItem(CreateOrderItemRecordRequest.builder()
                                                    .orderId((long) order.getOrderId())
                                                    .productId((long) item.getProductId())
                                                    .quantity(item.getQuantity())
                                                    .price(product.getPrice())
                                                    .build())
                                            .mapEmpty());
                                }

                                // 5. Create shipping address
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
                                        // 6. Recalculate and save total price
                                        .<Order>compose(v -> orderItemQueryRepo
                                                .calculateTotalPrice(order.getOrderId().intValue())                                                                .compose(totalPrice -> orderCommandRepo.updateOrder(
                                                        UpdateOrderRecordRequest.builder()
                                                                .orderId((long) order.getOrderId())
                                                                .totalPrice(withTax(totalPrice, shipping.getShippingCost()))
                                                                .build())));
                            })
                            .recover(err -> compensateStocks(decrementedStocks)
                                    .compose(ignored -> Future.<Order>failedFuture(err))
                                    .recover(compensationError -> {
                                        logger.error("Order creation compensation failed", compensationError);
                                        return Future.failedFuture(err);
                                    }));
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
        if (req == null || req.getOrderId() == null || req.getOrderId() <= 0
                || req.getUserId() == null || req.getUserId() <= 0
                || req.getItems() == null || req.getItems().isEmpty()) {
            return Future.failedFuture(new BadRequestException(
                    "Order, user, and items are required"));
        }
        for (var item : req.getItems()) {
            if (item == null || item.getProductId() == null || item.getProductId() <= 0
                    || item.getQuantity() == null || item.getQuantity() <= 0) {
                return Future.failedFuture(new BadRequestException(
                        "Each order item requires a valid product and positive quantity"));
            }
        }
        if (req.getShippingAddress() != null
                && (req.getShippingAddress().getShippingId() == null
                        || req.getShippingAddress().getShippingId() <= 0)) {
            return Future.failedFuture(new BadRequestException("Shipping ID is required when shipping is provided"));
        }

        var ctx = metrics.startSpan("OrderCommandService.updateOrder",
                Attributes.builder()
                        .put("order.id", (long) req.getOrderId())
                        .put("user.id", (long) req.getUserId())
                        .build());

        logger.info("Updating order: {}", req.getOrderId());
        List<StockAdjustment> appliedAdjustments = new ArrayList<>();
        List<ItemChange> changedItems = new ArrayList<>();
        List<Long> createdItemIds = new ArrayList<>();

        return orderQueryRepo.getOrderById(req.getOrderId())
                .<Order>compose(existingOrder -> {
                    if (existingOrder == null) {
                        return Future.<Order>failedFuture(new NotFoundException("Order not found"));
                    }
                    if (existingOrder.getUserId() == null
                            || existingOrder.getUserId().intValue() != req.getUserId().intValue()) {
                        return Future.failedFuture(new io.example.common.exception.grpc.ForbiddenException(
                                "You do not own this order"));
                    }
                    return userQueryRepo.findById(req.getUserId())
                            .map(userExists -> {
                                if (!userExists) {
                                    throw new NotFoundException("User not found");
                                }
                                return existingOrder;
                            });
                })
                .<Order>compose(existingOrder -> orderItemQueryRepo.getOrderItemsByOrder(
                        req.getOrderId().intValue()).compose(existingItems -> {
                    Map<Long, OrderItem> existingById = new HashMap<>();
                    if (existingItems == null) {
                        return Future.failedFuture(new NotFoundException("Order items not found"));
                    }
                    for (OrderItem existingItem : existingItems) {
                        existingById.put(existingItem.getOrderItemId(), existingItem);
                    }

                    Future<Void> itemsFuture = Future.succeededFuture();
                    for (var item : req.getItems()) {
                        itemsFuture = itemsFuture.compose(v -> {
                            if (item.getOrderItemId() != null && item.getOrderItemId() > 0) {
                                OrderItem existingItem = existingById.get(item.getOrderItemId());
                                if (existingItem == null) {
                                    return Future.failedFuture(new NotFoundException(
                                            "Order item not found: " + item.getOrderItemId()));
                                }
                                if (existingItem.getProductId() == null
                                        || existingItem.getProductId().longValue() != item.getProductId()) {
                                    return Future.failedFuture(new BadRequestException(
                                            "Changing the product of an existing order item is not supported"));
                                }
                                int delta = calculateStockDelta(existingItem.getQuantity(), item.getQuantity());
                                return applyStockAdjustment(item.getProductId().intValue(), delta,
                                        appliedAdjustments)
                                        .compose(ignored -> productQueryRepo.findById(
                                                item.getProductId().intValue()))
                                        .compose(product -> {
                                            if (product == null) {
                                                return Future.<Void>failedFuture(new NotFoundException(
                                                        "Product not found: " + item.getProductId()));
                                            }
                                            return orderItemCommandRepo
                                                    .updateOrderItem(UpdateOrderItemRecordRequest.builder()
                                                            .orderItemId(item.getOrderItemId())
                                                            .quantity(item.getQuantity())
                                                            .price(product.getPrice())
                                                            .build())
                                                    .onSuccess(ignored -> changedItems.add(new ItemChange(
                                                            existingItem.getOrderItemId(),
                                                            existingItem.getQuantity(),
                                                            existingItem.getPrice())))
                                                    .mapEmpty();
                                        });
                            }

                            // New item — atomically reserve its quantity.
                            return applyStockAdjustment(item.getProductId().intValue(), item.getQuantity(),
                                    appliedAdjustments)
                                    .compose(ignored -> productQueryRepo.findById(
                                            item.getProductId().intValue()))
                                    .compose(product -> {
                                        if (product == null) {
                                            return Future.<Void>failedFuture(new NotFoundException(
                                                    "Product not found: " + item.getProductId()));
                                        }
                                        return orderItemCommandRepo
                                                .createOrderItem(CreateOrderItemRecordRequest.builder()
                                                        .orderId((long) req.getOrderId())
                                                        .productId((long) item.getProductId())
                                                        .quantity(item.getQuantity())
                                                        .price(product.getPrice())
                                                        .build())
                                                .compose(created -> {
                                                    if (created == null || created.getOrderItemId() == null) {
                                                        return Future.failedFuture(new IllegalStateException(
                                                                "Order item creation returned no item"));
                                                    }
                                                    createdItemIds.add(created.getOrderItemId());
                                                    return Future.succeededFuture();
                                                });
                                    });
                        });
                    }

                    var shipping = req.getShippingAddress();
                    return itemsFuture.<ShippingAddress>compose(v -> {
                        if (shipping == null) {
                            // Partial order updates may omit shipping. Keep the
                            // persisted address and cost for total calculation.
                            return shippingAddressCommandRepo.getShippingAddressByOrderID(req.getOrderId());
                        }
                        return shippingAddressCommandRepo.updateShippingAddress(
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
                                        .build());
                    }).<Order>compose(effectiveShipping -> orderItemQueryRepo
                            .calculateTotalPrice(req.getOrderId().intValue())
                            .compose(totalPrice -> orderCommandRepo.updateOrder(UpdateOrderRecordRequest.builder()
                                    .orderId((long) req.getOrderId())
                                    .totalPrice(withTax(totalPrice,
                                            effectiveShipping != null ? effectiveShipping.getShippingCost() : 0))
                                    .build())));
                }))
                .recover(err -> rollbackUpdate(changedItems, createdItemIds, appliedAdjustments)
                        .compose(ignored -> Future.<Order>failedFuture(err)))
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
        List<StockDecrement> restored = new ArrayList<>();

        return orderQueryRepo.getOrderById(id)
                .<Order>compose(order -> {
                    if (order == null) {
                        return Future.failedFuture(new NotFoundException("Order not found with ID: " + id));
                    }
                    return orderItemQueryRepo.getOrderItemsByOrder(id.intValue())
                            .compose(items -> restoreStockForOrderItems(items, restored))
                            .compose(v -> orderCommandRepo.trashOrder(id))
                            .compose(trashed -> {
                                if (trashed == null) {
                                    // Sudah di-trash request lain (race) — revert stok.
                                    // Error revert hanya di-log; NotFound tetap menang.
                                    return revertStockRestores(restored)
                                            .recover(revertErr -> {
                                                logger.error("Stock restore revert failed on race", revertErr);
                                                return Future.succeededFuture();
                                            })
                                            .compose(rv -> Future.<Order>failedFuture(new NotFoundException(
                                                    "Order not found with ID: " + id)));
                                }
                                return Future.succeededFuture(trashed);
                            })
                            .recover(err -> revertStockRestores(restored)
                                    .compose(ignored -> Future.<Order>failedFuture(err))
                                    .recover(revertError -> {
                                        logger.error("Stock restore revert failed", revertError);
                                        return Future.failedFuture(err);
                                    }));
                })
                .compose(order -> evict(id, order.getMerchantId()).map(v -> order))
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
        List<StockDecrement> decremented = new ArrayList<>();

        return orderQueryRepo.findByTrashedId(id)
                .<Order>compose(trashed -> {
                    if (trashed == null) {
                        return Future.failedFuture(
                                new BadRequestException("Order not found or must be trashed first"));
                    }
                    return orderItemQueryRepo.getOrderItemsByOrder(id.intValue())
                            .compose(items -> decrementStockForOrderItems(items, decremented))
                            .compose(v -> orderCommandRepo.restoreOrder(id))
                            .compose(restored -> {
                                if (restored == null) {
                                    // Sudah di-restore request lain (race) — kompensasi stok.
                                    return restoreStockDecrements(decremented, true)
                                            .compose(rv -> Future.<Order>failedFuture(
                                                    new NotFoundException("Order not found")));
                                }
                                return Future.succeededFuture(restored);
                            })
                            .recover(err -> restoreStockDecrements(decremented, true)
                                    .compose(ignored -> Future.<Order>failedFuture(err))
                                    .recover(compensationError -> {
                                        logger.error("Stock decrement compensation failed", compensationError);
                                        return Future.failedFuture(err);
                                    }));
                })
                .compose(order -> evict(id, order.getMerchantId()).map(v -> order))
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
                        return Future.failedFuture(new BadRequestException(
                                "Order not found or must be trashed before permanent deletion"));
                    }
                    return orderItemCommandRepo.deleteOrderItemPermanently(id)
                            .compose(v -> shippingAddressCommandRepo.deleteShippingAddressPermanently(id))
                            .compose(v -> transactionCommandRepo.deleteByOrderIDPermanent(id))
                            .compose(v -> orderCommandRepo.deleteOrderPermanently(id))
                            .compose(deleted -> {
                                if (deleted == null || !deleted) {
                                    return Future.failedFuture(new NotFoundException(
                                            "Order not found with ID: " + id));
                                }
                                return Future.succeededFuture();
                            })
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

        return orderCommandRepo.findAllTrashed()
                .compose(trashedOrders -> {
                    if (trashedOrders == null || trashedOrders.isEmpty()) {
                        return Future.<Void>failedFuture(new NotFoundException("No trashed orders found"));
                    }
                    int[] restoredCount = {0};
                    Future<Void> chain = Future.succeededFuture();
                    for (Order order : trashedOrders) {
                        chain = chain.compose(v -> restoreOneOrder(order, restoredCount));
                    }
                    return chain.compose(v -> {
                        if (restoredCount[0] == 0) {
                            return Future.<Void>failedFuture(new NotFoundException("No trashed orders found"));
                        }
                        return evictAll();
                    });
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restoreAll", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to restore all orders", e);
                    metrics.completeSpanError(ctx, "restoreAll", e.getMessage());
                });
    }

    /**
     * Restore satu order dalam alur restore-all: decrement stok item aktif,
     * lalu {@code restoreOrder(orderId)} atomik. Bila order ternyata sudah
     * di-restore request lain (race) → undo decrement sendiri (tanpa swallow)
     * lalu skip — bukan error; bila undo gagal → operasi fail (stok
     * inkonsisten). Bila decrement gagal → kompensasi order ini + error asli
     * menang (order sebelumnya tetap ter-restore).
     */
    private Future<Void> restoreOneOrder(Order order, int[] restoredCount) {
        List<StockDecrement> decremented = new ArrayList<>();
        Long orderId = order.getOrderId();

        return orderItemQueryRepo.getOrderItemsByOrder(orderId.intValue())
                .compose(items -> decrementStockForOrderItems(items, decremented))
                .compose(v -> orderCommandRepo.restoreOrder(orderId))
                .compose(restored -> {
                    if (restored == null) {
                        // Kalah race — undo decrement sendiri; gagal → fail (stok inkonsisten).
                        return restoreStockDecrements(decremented, false);
                    }
                    restoredCount[0]++;
                    return Future.succeededFuture();
                })
                .recover(err -> restoreStockDecrements(decremented, true)
                        .compose(ignored -> Future.<Void>failedFuture(err))
                        .recover(compensationError -> {
                            logger.error("Stock decrement compensation failed for order {}",
                                    orderId, compensationError);
                            return Future.failedFuture(err);
                        }));
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