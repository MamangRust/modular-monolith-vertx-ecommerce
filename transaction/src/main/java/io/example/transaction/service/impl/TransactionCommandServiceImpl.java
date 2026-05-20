package io.example.transaction.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.common.service.KafkaService;
import io.example.common.utils.EmailTemplate;
import io.example.transaction.model.CreateTransactionRequest;
import io.example.transaction.model.UpdateTransactionRequest;
import io.example.transaction.model.OrderItem;
import io.example.transaction.model.Transaction;
import io.example.transaction.enums.PaymentStatus;
import io.example.transaction.repository.*;
import io.example.transaction.service.TransactionCommandService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class TransactionCommandServiceImpl implements TransactionCommandService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionCommandServiceImpl.class);

    private final TransactionCommandRepository repo;
    private final MerchantQueryRepository merchantRepository;
    private final OrderQueryRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShippingAddressQueryRepository shippingAddressRepository;
    private final UserQueryRepository userQueryRepository;
    private final RedisService redis;
    private final TracingMetrics metrics;
    private final KafkaService kafkaService;

    private static final String CACHE_PREFIX = "transaction:";

    public TransactionCommandServiceImpl(
            TransactionCommandRepository repo,
            MerchantQueryRepository merchantRepository,
            OrderQueryRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ShippingAddressQueryRepository shippingAddressRepository,
            UserQueryRepository userQueryRepository,
            RedisService redis,
            TracingMetrics metrics,
            KafkaService kafkaService) {
        this.repo = repo;
        this.merchantRepository = merchantRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.shippingAddressRepository = shippingAddressRepository;
        this.userQueryRepository = userQueryRepository;
        this.redis = redis;
        this.metrics = metrics;
        this.kafkaService = kafkaService;
    }

    @Override
    public Future<ApiResponse<Transaction>> createTransaction(CreateTransactionRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "TransactionCommandService.createTransaction",
                Attributes.builder()
                        .put("order.id", req.getOrderID())
                        .put("merchant.id", req.getMerchantID())
                        .build());
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        logger.info("Creating transaction for order: {}", req.getOrderID());

        return merchantRepository.findById(req.getMerchantID().intValue())
                .compose(existsMerchant -> {
                    if (!existsMerchant) {
                        return Future.failedFuture("Merchant not found");
                    }
                    return orderRepository.findById(req.getOrderID().intValue());
                })
                .compose(existsOrder -> {
                    if (!existsOrder) {
                        return Future.failedFuture("Order not found");
                    }
                    return orderItemRepository.findOrderItemByOrder(req.getOrderID().intValue());
                })
                .compose(orderItems -> {
                    if (orderItems == null || orderItems.isEmpty()) {
                        return Future.failedFuture("Order items not found");
                    }

                    for (OrderItem item : orderItems) {
                        if (item.getQuantity() <= 0) {
                            return Future.failedFuture("Invalid order item quantity");
                        }
                    }

                    return shippingAddressRepository.findByOrderId(req.getOrderID().intValue())
                            .map(shipping -> {
                                int totalAmount = 0;
                                for (OrderItem item : orderItems) {
                                    totalAmount += item.getPrice() * item.getQuantity();
                                }

                                if (shipping != null) {
                                    totalAmount += shipping.getShippingCost();
                                }

                                int ppn = totalAmount * 11 / 100;
                                int totalAmountWithTax = totalAmount + ppn;

                                if (req.getAmount() < totalAmountWithTax) {
                                    throw new RuntimeException("Insufficient balance. Required: " + totalAmountWithTax);
                                }

                                req.setAmount(totalAmountWithTax);
                                req.setPaymentStatus("success");

                                return req;
                            });
                })
                .compose(validatedReq -> repo.createTransaction(validatedReq))
                .compose(transaction -> {
                    // Send Email via Kafka
                    return orderRepository.getOrderById(req.getOrderID().intValue())
                            .compose(order -> userQueryRepository.getUserById(order.getUserId()))
                            .compose(user -> {
                                String htmlBody = EmailTemplate.generateHtml(Map.of(
                                        "Title", "Transaction Successful",
                                        "Message",
                                        String.format("Your transaction of %d has been processed successfully.",
                                                transaction.getAmount()),
                                        "Button", "View History",
                                        "Link", "https://sanedge.example.com/transaction/history"));

                                JsonObject emailPayload = new JsonObject()
                                        .put("email", user.getEmail())
                                        .put("subject", "Transaction Successful - SanEdge")
                                        .put("body", htmlBody);

                                return kafkaService.sendMessage("email-service-topic-transaction-create",
                                        String.valueOf(transaction.getTransactionId()), emailPayload)
                                        .map(v -> transaction);
                            })
                            .recover(err -> {
                                logger.error("Failed to send transaction email", err);
                                return Future.succeededFuture(transaction);
                            });
                })
                .map(transaction -> {
                    span.setAttribute("transaction.id", transaction.getTransactionId());
                    metrics.completeSpanSuccess(tracingContext, "create", "Transaction created successfully");
                    return ApiResponse.success("Transaction created successfully", transaction);
                })
                .recover(err -> {
                    logger.error("Failed to create transaction", err);
                    metrics.completeSpanError(tracingContext, "create", err.getMessage());
                    return Future
                            .succeededFuture(ApiResponse.error("Failed to create transaction: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Transaction>> updateTransaction(UpdateTransactionRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "TransactionCommandService.updateTransaction",
                Attributes.builder()
                        .put("transaction.id", req.getTransactionID())
                        .put("order.id", req.getOrderID())
                        .put("merchant.id", req.getMerchantID())
                        .build());
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        logger.info("Updating transaction: {}", req.getTransactionID());

        return repo.updateTransaction(req) // We can check if it exists or do it direct
                .compose(existingTx -> {
                    if (existingTx == null) {
                        return Future.failedFuture("Transaction not found");
                    }
                    if (existingTx.getStatus() == PaymentStatus.PAID) {
                        return Future.failedFuture("Payment status cannot be modified");
                    }
                    return merchantRepository.findById(req.getMerchantID().intValue());
                })
                .compose(existsMerchant -> {
                    if (!existsMerchant) {
                        return Future.failedFuture("Merchant not found");
                    }
                    return orderRepository.findById(req.getOrderID().intValue());
                })
                .compose(existsOrder -> {
                    if (!existsOrder) {
                        return Future.failedFuture("Order not found");
                    }
                    return orderItemRepository.findOrderItemByOrder(req.getOrderID().intValue());
                })
                .compose(orderItems -> {
                    if (orderItems == null || orderItems.isEmpty()) {
                        return Future.failedFuture("Order items not found");
                    }

                    for (OrderItem item : orderItems) {
                        if (item.getQuantity() <= 0) {
                            return Future.failedFuture("Invalid order item quantity");
                        }
                    }

                    return shippingAddressRepository.findByOrderId(req.getOrderID().intValue())
                            .map(shipping -> {
                                int totalAmount = 0;
                                for (OrderItem item : orderItems) {
                                    totalAmount += item.getPrice() * item.getQuantity();
                                }

                                if (shipping != null) {
                                    totalAmount += shipping.getShippingCost();
                                }

                                int ppn = totalAmount * 11 / 100;
                                int totalAmountWithTax = totalAmount + ppn;

                                if (req.getAmount() < totalAmountWithTax) {
                                    throw new RuntimeException("Insufficient balance. Required: " + totalAmountWithTax);
                                }

                                req.setAmount(totalAmountWithTax);
                                req.setPaymentStatus("success");

                                return req;
                            });
                })
                .compose(validatedReq -> repo.updateTransaction(validatedReq))
                .compose(transaction -> {
                    String cacheKey = CACHE_PREFIX + transaction.getTransactionId();
                    return redis.delete(cacheKey).map(transaction);
                })
                .map(transaction -> {
                    span.setAttribute("transaction.id", transaction.getTransactionId());
                    metrics.completeSpanSuccess(tracingContext, "update", "Transaction updated successfully");
                    return ApiResponse.success("Transaction updated successfully", transaction);
                })
                .recover(err -> {
                    logger.error("Failed to update transaction", err);
                    metrics.completeSpanError(tracingContext, "update", err.getMessage());
                    return Future
                            .succeededFuture(ApiResponse.error("Failed to update transaction: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Transaction>> trashTransaction(Long transactionId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "TransactionCommandService.trashTransaction",
                Attributes.builder().put("transaction.id", transactionId).build());

        logger.info("Trashing transaction: {}", transactionId);

        return repo.trashTransaction(transactionId)
                .compose(data -> {
                    if (data == null) {
                        return Future.failedFuture("Transaction not found");
                    }
                    String cacheKey = CACHE_PREFIX + transactionId;
                    return redis.delete(cacheKey).map(data);
                })
                .map(data -> {
                    metrics.completeSpanSuccess(tracingContext, "trash", "Transaction trashed successfully");
                    return ApiResponse.success("Transaction trashed successfully", data);
                })
                .recover(err -> {
                    logger.error("Failed to trash transaction", err);
                    metrics.completeSpanError(tracingContext, "trash", err.getMessage());
                    return Future
                            .succeededFuture(ApiResponse.error("Failed to trash transaction: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Transaction>> restoreTransaction(Long transactionId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "TransactionCommandService.restoreTransaction",
                Attributes.builder().put("transaction.id", transactionId).build());

        logger.info("Restoring transaction: {}", transactionId);

        return repo.restoreTransaction(transactionId)
                .compose(data -> {
                    if (data == null) {
                        return Future.failedFuture("Transaction not found");
                    }
                    String cacheKey = CACHE_PREFIX + transactionId;
                    return redis.delete(cacheKey).map(data);
                })
                .map(data -> {
                    metrics.completeSpanSuccess(tracingContext, "restore", "Transaction restored successfully");
                    return ApiResponse.success("Transaction restored successfully", data);
                })
                .recover(err -> {
                    logger.error("Failed to restore transaction", err);
                    metrics.completeSpanError(tracingContext, "restore", err.getMessage());
                    return Future
                            .succeededFuture(ApiResponse.error("Failed to restore transaction: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deleteTransactionPermanently(Long transactionId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "TransactionCommandService.deleteTransactionPermanently",
                Attributes.builder().put("transaction.id", transactionId).build());

        logger.info("Permanently deleting transaction: {}", transactionId);

        return repo.deleteTransactionPermanently(transactionId)
                .compose(v -> {
                    String cacheKey = CACHE_PREFIX + transactionId;
                    return redis.delete(cacheKey).map(v);
                })
                .map(v -> {
                    metrics.completeSpanSuccess(tracingContext, "delete_permanent", "Transaction deleted permanently");
                    return ApiResponse.<Void>success("Transaction deleted permanently");
                })
                .recover(err -> {
                    logger.error("Failed to permanently delete transaction", err);
                    metrics.completeSpanError(tracingContext, "delete_permanent", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to permanently delete transaction: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deleteTransactionByOrderIdPermanently(Long orderId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "TransactionCommandService.deleteTransactionByOrderIdPermanently",
                Attributes.builder().put("order.id", orderId).build());

        logger.info("Permanently deleting transaction by order id: {}", orderId);

        return repo.deleteTransactionByOrderIdPermanently(orderId)
                .compose(v -> {
                    String cacheKey = CACHE_PREFIX + "order:" + orderId;
                    return redis.delete(cacheKey).map(v);
                })
                .map(v -> {
                    metrics.completeSpanSuccess(tracingContext, "delete_by_order_permanent",
                            "Transaction deleted permanently by order id");
                    return ApiResponse.<Void>success("Transaction deleted permanently by order id");
                })
                .recover(err -> {
                    logger.error("Failed to permanently delete transaction by order id", err);
                    metrics.completeSpanError(tracingContext, "delete_by_order_permanent", err.getMessage());
                    return Future.succeededFuture(ApiResponse
                            .<Void>error("Failed to permanently delete transaction by order id: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Integer>> restoreAllTransactions() {
        TracingMetrics.TracingContext tracingContext = metrics
                .startSpan("TransactionCommandService.restoreAllTransactions");

        logger.info("Restoring all transactions");

        return repo.restoreAllTransactions()
                .map(count -> {
                    metrics.completeSpanSuccess(tracingContext, "restore_all", "Success");
                    return ApiResponse.success("All transactions restored successfully", count);
                })
                .recover(err -> {
                    logger.error("Failed to restore all transactions", err);
                    metrics.completeSpanError(tracingContext, "restore_all", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to restore all transactions: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Integer>> deleteAllPermanentTransactions() {
        TracingMetrics.TracingContext tracingContext = metrics
                .startSpan("TransactionCommandService.deleteAllPermanentTransactions");

        logger.info("Permanently deleting all trashed transactions");

        return repo.deleteAllPermanentTransactions()
                .map(count -> {
                    metrics.completeSpanSuccess(tracingContext, "delete_all_permanent", "Success");
                    return ApiResponse.success("All trashed transactions permanently deleted successfully", count);
                })
                .recover(err -> {
                    logger.error("Failed to permanently delete all transactions", err);
                    metrics.completeSpanError(tracingContext, "delete_all_permanent", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to permanently delete all transactions: " + err.getMessage()));
                });
    }
}
