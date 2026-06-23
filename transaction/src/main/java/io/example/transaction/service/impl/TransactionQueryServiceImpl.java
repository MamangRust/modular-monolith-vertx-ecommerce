package io.example.transaction.service.impl;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.domain.PagedResult;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.transaction.domain.requests.FindAllTransaction;
import io.example.transaction.domain.requests.FindAllTransactionByMerchant;
import io.example.transaction.model.Transaction;
import io.example.transaction.repository.TransactionQueryRepository;
import io.example.transaction.service.TransactionQueryService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionQueryServiceImpl implements TransactionQueryService {
        private static final Logger log = LoggerFactory.getLogger(TransactionQueryServiceImpl.class);
        private static final ObjectMapper mapper = new ObjectMapper();
        private final TransactionQueryRepository repository;
        private final RedisService redis;
        private final TracingMetrics metrics;

        private static final String CACHE_PREFIX = "transaction:";
        private static final Duration CACHE_TTL = Duration.ofMinutes(10);

        @Override
        public Future<PagedResult<Transaction>> getTransactions(FindAllTransaction req) {
                var ctx = metrics.startSpan("TransactionQueryService.getTransactions");
                String cacheKey = CACHE_PREFIX + "list:all:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
                                + req.getPage() + ":" + req.getPageSize();

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<Transaction> typedCached = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<Transaction>>() {
                                                                        });
                                                        return Future.succeededFuture(typedCached);
                                                } catch (Exception e) {
                                                        log.warn("Failed to deserialize cached transactions: {}",
                                                                        e.getMessage());
                                                }
                                        }
                                        return repository.getTransactions(req)
                                                        .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL)
                                                                        .map(v -> res));
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTransactions", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getTransactions", e.getMessage()));
        }

        @Override
        public Future<PagedResult<Transaction>> getTransactionsActive(FindAllTransaction req) {
                var ctx = metrics.startSpan("TransactionQueryService.getTransactionsActive");
                String cacheKey = CACHE_PREFIX + "list:active:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
                                + req.getPage() + ":" + req.getPageSize();

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<Transaction> typedCached = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<Transaction>>() {
                                                                        });
                                                        return Future.succeededFuture(typedCached);
                                                } catch (Exception e) {
                                                        log.warn("Failed to deserialize cached active transactions: {}",
                                                                        e.getMessage());
                                                }
                                        }
                                        return repository.getTransactionsActive(req)
                                                        .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL)
                                                                        .map(v -> res));
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTransactionsActive", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getTransactionsActive",
                                                e.getMessage()));
        }

        @Override
        public Future<PagedResult<Transaction>> getTransactionsTrashed(FindAllTransaction req) {
                var ctx = metrics.startSpan("TransactionQueryService.getTransactionsTrashed");
                String cacheKey = CACHE_PREFIX + "list:trashed:" + (req.getSearch() != null ? req.getSearch() : "")
                                + ":"
                                + req.getPage() + ":" + req.getPageSize();

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<Transaction> typedCached = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<Transaction>>() {
                                                                        });
                                                        return Future.succeededFuture(typedCached);
                                                } catch (Exception e) {
                                                        log.warn("Failed to deserialize cached trashed transactions: {}",
                                                                        e.getMessage());
                                                }
                                        }
                                        return repository.getTransactionsTrashed(req)
                                                        .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL)
                                                                        .map(v -> res));
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTransactionsTrashed", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getTransactionsTrashed",
                                                e.getMessage()));
        }

        @Override
        public Future<PagedResult<Transaction>> getTransactionByMerchant(FindAllTransactionByMerchant req) {
                var ctx = metrics.startSpan("TransactionQueryService.getTransactionByMerchant",
                                Attributes.builder().put("merchant.id", (long) req.getMerchantId()).build());
                String cacheKey = CACHE_PREFIX + "list:merchant:" + req.getMerchantId() + ":"
                                + (req.getSearch() != null ? req.getSearch() : "") + ":"
                                + req.getPage() + ":" + req.getPageSize();

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<Transaction> typedCached = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<Transaction>>() {
                                                                        });
                                                        return Future.succeededFuture(typedCached);
                                                } catch (Exception e) {
                                                        log.warn("Failed to deserialize cached merchant transactions: {}",
                                                                        e.getMessage());
                                                }
                                        }
                                        return repository.getTransactionByMerchant(req)
                                                        .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL)
                                                                        .map(v -> res));
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTransactionByMerchant", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getTransactionByMerchant",
                                                e.getMessage()));
        }

        @Override
        public Future<Transaction> getTransactionById(Long transactionId) {
                var ctx = metrics.startSpan("TransactionQueryService.getTransactionById",
                                Attributes.builder().put("transaction.id", (long) transactionId).build());
                String key = CACHE_PREFIX + "id:" + transactionId;

                return redis.getJson(key, Transaction.class)
                                .compose(cached -> {
                                        if (cached != null) {
                                                return Future.succeededFuture(cached);
                                        }
                                        return repository.getTransactionById(transactionId)
                                                        .compose(db -> {
                                                                if (db == null) {
                                                                        return Future.<Transaction>failedFuture(
                                                                                        new NotFoundException(
                                                                                                        "Transaction not found"));
                                                                }
                                                                return redis.setJson(key, db, CACHE_TTL)
                                                                                .<Transaction>map(v -> db);
                                                        });
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTransactionById", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getTransactionById", e.getMessage()));
        }

        @Override
        public Future<Transaction> getTransactionByOrderId(Long orderId) {
                var ctx = metrics.startSpan("TransactionQueryService.getTransactionByOrderId",
                                Attributes.builder().put("order.id", (long) orderId).build());
                String key = CACHE_PREFIX + "order:" + orderId;

                return redis.getJson(key, Transaction.class)
                                .compose(cached -> {
                                        if (cached != null) {
                                                return Future.succeededFuture(cached);
                                        }
                                        return repository.getTransactionByOrderId(orderId)
                                                        .compose(db -> {
                                                                if (db == null) {
                                                                        return Future.<Transaction>failedFuture(
                                                                                        new NotFoundException(
                                                                                                        "Transaction not found for order"));
                                                                }
                                                                return redis.setJson(key, db, CACHE_TTL)
                                                                                .<Transaction>map(v -> db);
                                                        });
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTransactionByOrderId", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getTransactionByOrderId",
                                                e.getMessage()));
        }
}