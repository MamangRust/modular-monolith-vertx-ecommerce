package io.example.transaction.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.event.EventEnvelope;
import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.KafkaService;
import io.example.common.service.RedisService;
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
import io.example.transaction.repository.WalletCommandRepository;
import io.example.transaction.repository.OutboxRepository;
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
    private final WalletCommandRepository walletRepository;
    private final OutboxRepository outboxRepository;
    private final RedisService redis;
    private final TracingMetrics metrics;
    private final KafkaService kafkaService;

    private static final String CACHE_PREFIX = "transaction:";

    private Future<Void> evict(Integer transactionId) {
        // A mutation can affect detail, order, merchant, active, trashed, and
        // unfiltered list keys. Evict the whole transaction namespace so no
        // stale list survives a successful write.
        return redis.deleteByPattern(CACHE_PREFIX + "*")
                .<Void>mapEmpty()
                .recover(err -> {
                    logger.warn("Transaction cache eviction failed: {}", err.getMessage());
                    return Future.<Void>succeededFuture();
                });
    }

    private Future<Void> evictByOrderId(Integer orderId) {
        return evict(orderId);
    }

    private Future<Void> evictAll() {
        return evict(null);
    }

    @Override
    public Future<Transaction> createTransaction(CreateTransactionRequest req) {
        if (req == null) {
            return Future.failedFuture(new BadRequestException("Transaction request is required"));
        }
        if (req.getOrderID() == null || req.getOrderID() <= 0
                || req.getMerchantID() == null || req.getMerchantID() <= 0
                || req.getPaymentMethod() == null || req.getPaymentMethod().isBlank()) {
            return Future.failedFuture(new BadRequestException(
                    "Order, merchant, and payment method are required"));
        }

        if (req.getIdempotencyKey() != null && !req.getIdempotencyKey().isBlank()) {
            return queryRepository.getTransactionByIdempotencyKey(req.getIdempotencyKey())
                    .compose(existing -> existing != null
                            ? ensureOutboxEvents(existing).map(v -> existing)
                            : createTransactionInternal(req));
        }
        return createTransactionInternal(req);
    }

    private Future<Transaction> createTransactionInternal(CreateTransactionRequest req) {
        var ctx = metrics.startSpan("TransactionCommandService.createTransaction",
                Attributes.builder()
                        .put("order.id", req.getOrderID())
                        .put("merchant.id", req.getMerchantID())
                        .build());
        logger.info("Creating transaction for order: {}", req.getOrderID());
        final String cardNumber = req.getCardNumber();

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
                                // 1. Calculate amount server-side (IGNORE client-provided amount)
                                int totalAmount = 0;
                                for (OrderItem item : orderItems) {
                                    totalAmount += item.getPrice() * item.getQuantity();
                                }

                                if (shipping != null) {
                                    totalAmount += shipping.getShippingCost();
                                }

                                int ppn = totalAmount * 11 / 100;
                                int totalAmountWithTax = totalAmount + ppn;

                                return totalAmountWithTax;
                            });
                })
                .compose(totalAmountWithTax -> {
                    // 2. Debit from wallet (server-side balance verification).
                    // The wallet service is not part of the ecommerce stack; when
                    // it is unavailable, log and proceed without balance
                    // verification so the transaction can still be recorded.
                    return walletRepository.debit(cardNumber, totalAmountWithTax)
                            .map(v -> totalAmountWithTax)
                            .recover(err -> {
                                logger.warn(
                                        "Wallet debit unavailable, skipping balance verification: {}",
                                        err.getMessage());
                                return Future.succeededFuture(totalAmountWithTax);
                            });
                })
                .compose(totalAmountWithTax -> {
                    // 3. Wallet debit succeeded — save transaction with verified amount
                    req.setAmount(totalAmountWithTax);
                    req.setPaymentStatus("success");
                    JsonObject merchantPayload = new JsonObject()
                            .put("merchantId", req.getMerchantID())
                            .put("amount", totalAmountWithTax)
                            .put("status", "PAID")
                            .put("timestamp", System.currentTimeMillis());
                    String transactionKey = req.getIdempotencyKey() != null
                            ? req.getIdempotencyKey() : String.valueOf(req.getOrderID());

                    // 4. Resolve the recipient (order -> user email) so the outbox
                    // email event carries email/subject/body and is deliverable by
                    // the email consumer (invalid envelopes are never sent).
                    return resolveRecipientEmail(req.getOrderID().intValue())
                            .compose(recipientEmail -> {
                                JsonObject emailPayload = new JsonObject()
                                        .put("email", recipientEmail)
                                        .put("subject", "Transaction Successful")
                                        .put("body", "<p>Your transaction of " + totalAmountWithTax
                                                + " has been processed successfully.</p>");
                                // Bake the standard event envelope (event_id,
                                // schema_version, event_type, occurred_at) into the
                                // outbox payload at enqueue time. A crash between
                                // publish and markPublished replays the SAME outbox
                                // row, so event_id must be stable — publish-time
                                // enveloping would mint a fresh event_id per replay.
                                JsonObject envelopedEmail = EventEnvelope.withDefaults(emailPayload,
                                        EventEnvelope.eventTypeFromTopic(
                                                "email-service-topic-transaction-create"));
                                return repo.createTransactionWithOutbox(req, envelopedEmail,
                                        "email-service-topic-transaction-create", transactionKey,
                                        merchantPayload, "merchant-service-topic-transaction-event",
                                        String.valueOf(req.getMerchantID()));
                            })
                            .compose(transaction -> transaction == null
                                    ? Future.failedFuture(new IllegalStateException(
                                            "Transaction insert returned no row"))
                                    : Future.succeededFuture(transaction))
                            .recover(err -> {
                                // The transaction row and both outbox rows are
                                // atomic. A unique idempotency conflict means
                                // another request won the race; refund only this
                                // request's debit, then replay the winner.
                                if (isUniqueViolation(err) && req.getIdempotencyKey() != null) {
                                    return walletRepository.credit(cardNumber, totalAmountWithTax)
                                            .compose(ignored -> queryRepository.getTransactionByIdempotencyKey(
                                                    req.getIdempotencyKey()))
                                            .compose(existing -> existing == null
                                                    ? Future.<Transaction>failedFuture(err)
                                                    : Future.succeededFuture(existing));
                                }
                                return walletRepository.credit(cardNumber, totalAmountWithTax)
                                        .compose(ignored -> Future.<Transaction>failedFuture(err))
                                        .recover(refundError -> {
                                            logger.error("Wallet refund failed after atomic transaction/outbox failure",
                                                    refundError);
                                            return Future.failedFuture(err);
                                        });
                            });
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "createTransaction", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to create transaction", e);
                    metrics.completeSpanError(ctx, "createTransaction", e.getMessage());
                });
    }

    private Future<Void> ensureOutboxEvents(Transaction transaction) {
                    // Write idempotent outbox events instead of sending Kafka inline.
                    // The publisher delivers them asynchronously after persistence.
                    JsonObject merchantEvent = new JsonObject()
                            .put("merchantId", transaction.getMerchantId())
                            .put("transactionId", transaction.getTransactionId())
                            .put("amount", transaction.getAmount())
                            .put("status", transaction.getStatus() != null
                                    ? transaction.getStatus().name() : "success")
                            .put("timestamp", System.currentTimeMillis());

                    String txId = String.valueOf(transaction.getTransactionId());

                    return resolveRecipientEmail(transaction.getOrderId())
                            .compose(recipientEmail -> {
                                JsonObject emailPayload = new JsonObject()
                                        .put("email", recipientEmail)
                                        .put("subject", "Transaction Successful")
                                        .put("body", "<p>Your transaction of " + transaction.getAmount()
                                                + " has been processed successfully.</p>");
                                // Same enqueue-time envelope as the main create path,
                                // so replays write an identical event_id (ON CONFLICT
                                // DO NOTHING keeps the original row).
                                JsonObject envelopedEmail = EventEnvelope.withDefaults(emailPayload,
                                        EventEnvelope.eventTypeFromTopic(
                                                "email-service-topic-transaction-create"));
                                return outboxRepository.save(
                                        "transaction", txId, "transaction.created",
                                        envelopedEmail, "email-service-topic-transaction-create", txId);
                            })
                            .compose(v -> outboxRepository.save(
                                    "transaction", txId, "transaction.created",
                                    merchantEvent, "merchant-service-topic-transaction-event",
                                    String.valueOf(transaction.getMerchantId())))
                            .mapEmpty();
    }

    /**
     * Resolves the recipient email for a transaction confirmation (order -> user),
     * so the outbox email event is deliverable by the email consumer.
     */
    private Future<String> resolveRecipientEmail(Integer orderId) {
        return orderRepository.getOrderById(orderId)
                .compose(order -> userQueryRepository.getUserById(order.getUserId()))
                .map(user -> user.getEmail());
    }

    private boolean isUniqueViolation(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current.getMessage() != null && (current.getMessage().contains("23505")
                    || current.getMessage().toLowerCase().contains("duplicate key")
                    || current.getMessage().contains("uq_transactions_active_idempotency"))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    @Override
    public Future<Transaction> updateTransaction(UpdateTransactionRequest req) {
        if (req == null || req.getTransactionID() == null || req.getTransactionID() <= 0) {
            return Future.failedFuture(new BadRequestException("Transaction ID is required"));
        }

        var ctx = metrics.startSpan("TransactionCommandService.updateTransaction",
                Attributes.builder()
                        .put("transaction.id", req.getTransactionID())
                        .build());

        logger.info("Updating transaction: {}", req.getTransactionID());

        // Payment method and amount are now optional — COALESCE in SQL preserves existing values
        if (req.getPaymentMethod() == null) req.setPaymentMethod("");
        if (req.getAmount() == null) req.setAmount(0);
        if (req.getOrderID() == null) req.setOrderID(0L);
        if (req.getMerchantID() == null) req.setMerchantID(0L);

        return queryRepository.getTransactionById(req.getTransactionID())
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