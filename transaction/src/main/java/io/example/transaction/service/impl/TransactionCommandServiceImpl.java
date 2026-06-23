package io.example.transaction.service.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.KafkaService;
import io.example.common.service.RedisService;
import io.example.common.utils.EmailTemplate;
import io.example.transaction.domain.requests.CreateTransactionRequest;
import io.example.transaction.domain.requests.UpdateTransactionRequest;
import io.example.transaction.enums.PaymentStatus;
import io.example.transaction.handler.ProtoConverter;
import io.example.transaction.model.OrderItem;
import io.example.transaction.model.Transaction;
import io.example.transaction.repository.MerchantQueryRepository;
import io.example.transaction.repository.OrderItemRepository;
import io.example.transaction.repository.OrderQueryRepository;
import io.example.transaction.repository.ShippingAddressQueryRepository;
import io.example.transaction.repository.TransactionCommandRepository;
import io.example.transaction.repository.TransactionQueryRepository;
import io.example.transaction.repository.UserQueryRepository;
import io.example.transaction.service.TransactionCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import pb.transaction.TransactionCommon.TransactionResponseDeleteAt;

@RequiredArgsConstructor
public class TransactionCommandServiceImpl implements TransactionCommandService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionCommandServiceImpl.class);

    private final TransactionCommandRepository repo;
    private final TransactionQueryRepository queryRepository;
    private final MerchantQueryRepository merchantRepository;
    private final OrderQueryRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShippingAddressQueryRepository shippingAddressRepository;
    private final UserQueryRepository userQueryRepository;
    private final RedisService redis;
    private final TracingMetrics metrics;
    private final KafkaService kafkaService;

    private static final String CACHE_PREFIX = "transaction:";

    private Future<Void> evict(Integer transactionId) {
        return redis.delete(CACHE_PREFIX + transactionId).mapEmpty();
    }

    private Future<Void> evictByOrderId(Integer orderId) {
        return redis.delete(CACHE_PREFIX + "order:" + orderId).mapEmpty();
    }

    private Future<Void> evictAll() {
        return redis.deleteByPattern(CACHE_PREFIX + "*").mapEmpty();
    }

    @Override
    public Future<Transaction> createTransaction(CreateTransactionRequest req) {
        var ctx = metrics.startSpan("TransactionCommandService.createTransaction",
                Attributes.builder()
                        .put("order.id", req.getOrderID())
                        .put("merchant.id", req.getMerchantID())
                        .build());

        logger.info("Creating transaction for order: {}", req.getOrderID());

        return merchantRepository.findById(req.getMerchantID().intValue())
                .compose(existsMerchant -> {
                    if (!existsMerchant) {
                        return Future.failedFuture(new NotFoundException("Merchant not found"));
                    }
                    return orderRepository.findById(req.getOrderID().intValue());
                })
                .compose(existsOrder -> {
                    if (!existsOrder) {
                        return Future.failedFuture(new NotFoundException("Order not found"));
                    }
                    return orderItemRepository.findOrderItemByOrder(req.getOrderID().intValue());
                })
                .compose(orderItems -> {
                    if (orderItems == null || orderItems.isEmpty()) {
                        return Future.failedFuture(new NotFoundException("Order items not found"));
                    }

                    for (OrderItem item : orderItems) {
                        if (item.getQuantity() <= 0) {
                            return Future.failedFuture(new BadRequestException("Invalid order item quantity"));
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
                                    throw new BadRequestException(
                                            "Insufficient balance. Required: " + totalAmountWithTax);
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
                                        .compose(v -> {
                                            // Send transaction event to merchant module for cache invalidation
                                            JsonObject merchantEvent = new JsonObject()
                                                    .put("merchantId", transaction.getMerchantId())
                                                    .put("transactionId", transaction.getTransactionId())
                                                    .put("amount", transaction.getAmount())
                                                    .put("status", transaction.getStatus() != null
                                                            ? transaction.getStatus().name() : "success")
                                                    .put("timestamp", System.currentTimeMillis());

                                            return kafkaService
                                                    .sendMessage("merchant-service-topic-transaction-event",
                                                            String.valueOf(transaction.getMerchantId()),
                                                            merchantEvent);
                                        })
                                        .map(v -> transaction);
                            })
                            .recover(err -> {
                                logger.error("Failed to send transaction email", err);
                                return Future.succeededFuture(transaction);
                            });
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "createTransaction", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to create transaction", e);
                    metrics.completeSpanError(ctx, "createTransaction", e.getMessage());
                });
    }

    @Override
    public Future<Transaction> updateTransaction(UpdateTransactionRequest req) {
        var ctx = metrics.startSpan("TransactionCommandService.updateTransaction",
                Attributes.builder()
                        .put("transaction.id", req.getTransactionID())
                        .put("order.id", req.getOrderID())
                        .put("merchant.id", req.getMerchantID())
                        .build());

        logger.info("Updating transaction: {}", req.getTransactionID());

        return repo.updateTransaction(req)
                .compose(existingTx -> {
                    if (existingTx == null) {
                        return Future.failedFuture(new NotFoundException("Transaction not found"));
                    }
                    if (existingTx.getStatus() == PaymentStatus.PAID) {
                        return Future.failedFuture(new BadRequestException("Payment status cannot be modified"));
                    }
                    return merchantRepository.findById(req.getMerchantID().intValue());
                })
                .compose(existsMerchant -> {
                    if (!existsMerchant) {
                        return Future.failedFuture(new NotFoundException("Merchant not found"));
                    }
                    return orderRepository.findById(req.getOrderID().intValue());
                })
                .compose(existsOrder -> {
                    if (!existsOrder) {
                        return Future.failedFuture(new NotFoundException("Order not found"));
                    }
                    return orderItemRepository.findOrderItemByOrder(req.getOrderID().intValue());
                })
                .compose(orderItems -> {
                    if (orderItems == null || orderItems.isEmpty()) {
                        return Future.failedFuture(new NotFoundException("Order items not found"));
                    }

                    for (OrderItem item : orderItems) {
                        if (item.getQuantity() <= 0) {
                            return Future.failedFuture(new BadRequestException("Invalid order item quantity"));
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
                                    throw new BadRequestException(
                                            "Insufficient balance. Required: " + totalAmountWithTax);
                                }

                                req.setAmount(totalAmountWithTax);
                                req.setPaymentStatus("success");

                                return req;
                            });
                })
                .compose(validatedReq -> repo.updateTransaction(validatedReq))
                .compose(transaction -> evict(transaction.getTransactionId().intValue()).map(v -> transaction))
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "updateTransaction", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to update transaction", e);
                    metrics.completeSpanError(ctx, "updateTransaction", e.getMessage());
                });
    }

    @Override
    public Future<TransactionResponseDeleteAt> trashTransaction(Long transactionId) {
        var ctx = metrics.startSpan("TransactionCommandService.trashTransaction",
                Attributes.builder().put("transaction.id", transactionId).build());

        logger.info("Trashing transaction: {}", transactionId);

        return repo.trashTransaction(transactionId)
                .compose(data -> {
                    if (data == null) {
                        return Future.failedFuture(new NotFoundException("Transaction not found"));
                    }
                    return evict(transactionId.intValue()).map(v -> data);
                })
                .map(ProtoConverter::toProtoResponseDeleteAt)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "trashTransaction", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to trash transaction", e);
                    metrics.completeSpanError(ctx, "trashTransaction", e.getMessage());
                });
    }

    @Override
    public Future<TransactionResponseDeleteAt> restoreTransaction(Long transactionId) {
        var ctx = metrics.startSpan("TransactionCommandService.restoreTransaction",
                Attributes.builder().put("transaction.id", transactionId).build());

        logger.info("Restoring transaction: {}", transactionId);

        return queryRepository.findByTrashedId(transactionId)
                .compose(existing -> {
                    if (existing == null) {
                        return Future
                                .failedFuture(new BadRequestException("Transaction not found or not in trashed state"));
                    }
                    return repo.restoreTransaction(transactionId);
                })
                .compose(data -> {
                    if (data == null) {
                        return Future
                                .failedFuture(new BadRequestException("Transaction not found or not in trashed state"));
                    }
                    return evict(transactionId.intValue()).map(v -> data);
                })
                .map(ProtoConverter::toProtoResponseDeleteAt)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restoreTransaction", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to restore transaction", e);
                    metrics.completeSpanError(ctx, "restoreTransaction", e.getMessage());
                });
    }

    @Override
    public Future<Void> deleteTransactionPermanently(Long transactionId) {
        var ctx = metrics.startSpan("TransactionCommandService.deleteTransactionPermanently",
                Attributes.builder().put("transaction.id", transactionId).build());

        logger.info("Permanently deleting transaction: {}", transactionId);

        return queryRepository.findByTrashedId(transactionId)
                .compose(existing -> {
                    if (existing == null) {
                        return Future.failedFuture(
                                new BadRequestException("Transaction not found or must be trashed first"));
                    }
                    return repo.deleteTransactionPermanently(transactionId);
                })
                .compose(deleted -> {
                    if (!deleted) {
                        return Future.<Void>failedFuture(
                                new BadRequestException("Transaction not found or must be trashed first"));
                    }
                    return evict(transactionId.intValue());
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deleteTransactionPermanently", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to delete transaction permanently", e);
                    metrics.completeSpanError(ctx, "deleteTransactionPermanently", e.getMessage());
                });
    }

    @Override
    public Future<Void> deleteTransactionByOrderIdPermanently(Long orderId) {
        var ctx = metrics.startSpan("TransactionCommandService.deleteTransactionByOrderIdPermanently",
                Attributes.builder().put("order.id", orderId).build());

        logger.info("Permanently deleting transaction by order id: {}", orderId);

        return repo.deleteTransactionByOrderIdPermanently(orderId)
                .compose(v -> evictByOrderId(orderId.intValue()))
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deleteTransactionByOrderIdPermanently", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to permanently delete transaction by order id", e);
                    metrics.completeSpanError(ctx, "deleteTransactionByOrderIdPermanently", e.getMessage());
                });
    }

    @Override
    public Future<Void> restoreAllTransactions() {
        var ctx = metrics.startSpan("TransactionCommandService.restoreAllTransactions");

        logger.info("Restoring all transactions");

        return repo.restoreAllTransactions()
                .compose(count -> {
                    if (count == 0) {
                        return Future.<Void>failedFuture(new NotFoundException("No trashed transactions found"));
                    }
                    return evictAll();
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restoreAllTransactions", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to restore all transactions", e);
                    metrics.completeSpanError(ctx, "restoreAllTransactions", e.getMessage());
                });
    }

    @Override
    public Future<Void> deleteAllPermanentTransactions() {
        var ctx = metrics.startSpan("TransactionCommandService.deleteAllPermanentTransactions");

        logger.info("Permanently deleting all transactions");

        return repo.deleteAllPermanentTransactions()
                .compose(count -> {
                    if (count == 0) {
                        return Future.<Void>failedFuture(new NotFoundException("No trashed transactions found"));
                    }
                    return evictAll();
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deleteAllPermanentTransactions", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to permanently delete all transactions", e);
                    metrics.completeSpanError(ctx, "deleteAllPermanentTransactions", e.getMessage());
                });
    }
}