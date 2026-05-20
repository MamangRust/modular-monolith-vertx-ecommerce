package io.example.transaction.service.impl;

import java.time.Duration;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.model.ApiResponse;
import io.example.common.model.PagedResult;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.transaction.model.FindAllTransaction;
import io.example.transaction.model.FindAllTransactionByMerchant;
import io.example.transaction.model.Transaction;
import io.example.transaction.repository.TransactionQueryRepository;
import io.example.transaction.service.TransactionQueryService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;

public class TransactionQueryServiceImpl implements TransactionQueryService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionQueryServiceImpl.class);

    private final TransactionQueryRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "transaction:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    public TransactionQueryServiceImpl(TransactionQueryRepository repo, RedisService redis, TracingMetrics metrics) {
        this.repo = repo;
        this.redis = redis;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponse<PagedResult<Transaction>>> getTransactions(FindAllTransaction req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("TransactionQueryService.getTransactions");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int page = req.getPage() != null && req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() != null && req.getPageSize() > 0 ? req.getPageSize() : 10;
        String search = req.getSearch() != null ? req.getSearch() : "";

        String cacheKey = String.format("transactions:page:%d:search:%s", page, search);

        return redis.getJson(cacheKey, PagedResult.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("transaction.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_transactions", "Transactions fetched from cache");
                        @SuppressWarnings("unchecked")
                        PagedResult<Transaction> typedCached = (PagedResult<Transaction>) cached;
                        return Future.succeededFuture(ApiResponse.success("Transactions fetched successfully (from cache)", typedCached));
                    }
                    span.setAttribute("transaction.cache_hit", false);
                    return repo.getTransactions(req)
                            .compose(result -> redis.setJson(cacheKey, result, CACHE_TTL).map(result))
                            .map(result -> {
                                metrics.completeSpanSuccess(tracingContext, "get_transactions", "Transactions fetched successfully");
                                return ApiResponse.success("Transactions fetched successfully", result);
                            });
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch transactions", throwable);
                    metrics.completeSpanError(tracingContext, "get_transactions", throwable.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to fetch transactions: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<PagedResult<Transaction>>> getTransactionsActive(FindAllTransaction req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("TransactionQueryService.getTransactionsActive");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int page = req.getPage() != null && req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() != null && req.getPageSize() > 0 ? req.getPageSize() : 10;
        String search = req.getSearch() != null ? req.getSearch() : "";

        String cacheKey = String.format("transactions:active:page:%d:search:%s", page, search);

        return redis.getJson(cacheKey, PagedResult.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("transaction.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_transactions_active", "Active transactions fetched from cache");
                        @SuppressWarnings("unchecked")
                        PagedResult<Transaction> typedCached = (PagedResult<Transaction>) cached;
                        return Future.succeededFuture(ApiResponse.success("Active transactions fetched successfully (from cache)", typedCached));
                    }
                    span.setAttribute("transaction.cache_hit", false);
                    return repo.getTransactionsActive(req)
                            .compose(result -> redis.setJson(cacheKey, result, CACHE_TTL).map(result))
                            .map(result -> {
                                metrics.completeSpanSuccess(tracingContext, "get_transactions_active", "Active transactions fetched successfully");
                                return ApiResponse.success("Active transactions fetched successfully", result);
                            });
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch active transactions", throwable);
                    metrics.completeSpanError(tracingContext, "get_transactions_active", throwable.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to fetch active transactions: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<PagedResult<Transaction>>> getTransactionsTrashed(FindAllTransaction req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("TransactionQueryService.getTransactionsTrashed");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int page = req.getPage() != null && req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() != null && req.getPageSize() > 0 ? req.getPageSize() : 10;
        String search = req.getSearch() != null ? req.getSearch() : "";

        String cacheKey = String.format("transactions:trashed:page:%d:search:%s", page, search);

        return redis.getJson(cacheKey, PagedResult.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("transaction.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_transactions_trashed", "Trashed transactions fetched from cache");
                        @SuppressWarnings("unchecked")
                        PagedResult<Transaction> typedCached = (PagedResult<Transaction>) cached;
                        return Future.succeededFuture(ApiResponse.success("Trashed transactions fetched successfully (from cache)", typedCached));
                    }
                    span.setAttribute("transaction.cache_hit", false);
                    return repo.getTransactionsTrashed(req)
                            .compose(result -> redis.setJson(cacheKey, result, CACHE_TTL).map(result))
                            .map(result -> {
                                metrics.completeSpanSuccess(tracingContext, "get_transactions_trashed", "Trashed transactions fetched successfully");
                                return ApiResponse.success("Trashed transactions fetched successfully", result);
                            });
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch trashed transactions", throwable);
                    metrics.completeSpanError(tracingContext, "get_transactions_trashed", throwable.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to fetch trashed transactions: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<PagedResult<Transaction>>> getTransactionByMerchant(FindAllTransactionByMerchant req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("TransactionQueryService.getTransactionByMerchant");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int merchantId = req.getMerchantId() != null ? req.getMerchantId() : 0;
        int page = req.getPage() != null && req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() != null && req.getPageSize() > 0 ? req.getPageSize() : 10;
        String search = req.getSearch() != null ? req.getSearch() : "";

        String cacheKey = String.format("transactions:merchant:%d:page:%d:search:%s", merchantId, page, search);

        return redis.getJson(cacheKey, PagedResult.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("transaction.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_transaction_by_merchant", "Merchant transactions fetched from cache");
                        @SuppressWarnings("unchecked")
                        PagedResult<Transaction> typedCached = (PagedResult<Transaction>) cached;
                        return Future.succeededFuture(ApiResponse.success("Merchant transactions fetched successfully (from cache)", typedCached));
                    }
                    span.setAttribute("transaction.cache_hit", false);
                    return repo.getTransactionByMerchant(req)
                            .compose(result -> redis.setJson(cacheKey, result, CACHE_TTL).map(result))
                            .map(result -> {
                                metrics.completeSpanSuccess(tracingContext, "get_transaction_by_merchant", "Merchant transactions fetched successfully");
                                return ApiResponse.success("Merchant transactions fetched successfully", result);
                            });
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch merchant transactions", throwable);
                    metrics.completeSpanError(tracingContext, "get_transaction_by_merchant", throwable.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to fetch merchant transactions: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Transaction>> getTransactionById(Long transactionId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "TransactionQueryService.getTransactionById",
                Attributes.builder().put("transaction.id", transactionId).build());
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        String cacheKey = CACHE_PREFIX + transactionId;

        return redis.getJson(cacheKey, Transaction.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("transaction.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_transaction_by_id", "Transaction fetched from cache");
                        return Future.succeededFuture(ApiResponse.success("Transaction fetched successfully (from cache)", cached));
                    }
                    span.setAttribute("transaction.cache_hit", false);
                    return repo.getTransactionById(transactionId)
                            .compose(res -> {
                                if (res == null) {
                                    return Future.failedFuture("Transaction not found");
                                }
                                return redis.setJson(cacheKey, res, CACHE_TTL).map(res);
                            })
                            .map(res -> {
                                metrics.completeSpanSuccess(tracingContext, "get_transaction_by_id", "Transaction fetched successfully");
                                return ApiResponse.success("Transaction fetched successfully", res);
                            });
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch transaction by id: {}", transactionId, throwable);
                    metrics.completeSpanError(tracingContext, "get_transaction_by_id", throwable.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to fetch transaction: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Transaction>> getTransactionByOrderId(Long orderId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "TransactionQueryService.getTransactionByOrderId",
                Attributes.builder().put("order.id", orderId).build());
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        String cacheKey = CACHE_PREFIX + "order:" + orderId;

        return redis.getJson(cacheKey, Transaction.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("transaction.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_transaction_by_order_id", "Transaction fetched from cache");
                        return Future.succeededFuture(ApiResponse.success("Transaction fetched successfully (from cache)", cached));
                    }
                    span.setAttribute("transaction.cache_hit", false);
                    return repo.getTransactionByOrderId(orderId)
                            .compose(res -> {
                                if (res == null) {
                                    return Future.failedFuture("Transaction not found for order");
                                }
                                return redis.setJson(cacheKey, res, CACHE_TTL).map(res);
                            })
                            .map(res -> {
                                metrics.completeSpanSuccess(tracingContext, "get_transaction_by_order_id", "Transaction fetched successfully");
                                return ApiResponse.success("Transaction fetched successfully", res);
                            });
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch transaction by order id: {}", orderId, throwable);
                    metrics.completeSpanError(tracingContext, "get_transaction_by_order_id", throwable.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to fetch transaction: " + throwable.getMessage()));
                });
    }
}
