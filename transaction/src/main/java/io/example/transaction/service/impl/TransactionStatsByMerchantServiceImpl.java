package io.example.transaction.service.impl;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.transaction.domain.requests.FindMonthlyMerchantStatsRequest;
import io.example.transaction.domain.requests.FindYearlyMerchantStatsRequest;
import io.example.transaction.model.*;
import io.example.transaction.repository.TransactionStatsByMerchantRepository;
import io.example.transaction.service.TransactionStatsByMerchantService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionStatsByMerchantServiceImpl implements TransactionStatsByMerchantService {
    private static final Logger log = LoggerFactory.getLogger(TransactionStatsByMerchantServiceImpl.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final TransactionStatsByMerchantRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Override
    public Future<List<TransactionMonthlyAmountSuccess>> getMonthlyAmountTransactionSuccessByMerchant(
            FindMonthlyMerchantStatsRequest req) {
        var ctx = metrics.startSpan("TransactionStatsByMerchantService.getMonthlyAmountSuccess");
        String cacheKey = String.format("report:merchant:monthly_amount_success:%d:%d:%d", req.getMerchantId(),
                req.getYear(), req.getMonth());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionMonthlyAmountSuccess> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionMonthlyAmountSuccess>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached merchant monthly success: {}", e.getMessage());
                        }
                    }
                    return repo.getMonthlyAmountTransactionSuccessByMerchant(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyAmountSuccess", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getMonthlyAmountSuccess", e.getMessage()));
    }

    @Override
    public Future<List<TransactionYearlyAmountSuccess>> getYearlyAmountTransactionSuccessByMerchant(
            FindYearlyMerchantStatsRequest req) {
        var ctx = metrics.startSpan("TransactionStatsByMerchantService.getYearlyAmountSuccess");
        String cacheKey = String.format("report:merchant:yearly_amount_success:%d:%d", req.getMerchantId(),
                req.getYear());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionYearlyAmountSuccess> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionYearlyAmountSuccess>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached merchant yearly success: {}", e.getMessage());
                        }
                    }
                    return repo.getYearlyAmountTransactionSuccessByMerchant(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyAmountSuccess", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getYearlyAmountSuccess", e.getMessage()));
    }

    @Override
    public Future<List<TransactionMonthlyAmountFailed>> getMonthlyAmountTransactionFailedByMerchant(
            FindMonthlyMerchantStatsRequest req) {
        var ctx = metrics.startSpan("TransactionStatsByMerchantService.getMonthlyAmountFailed");
        String cacheKey = String.format("report:merchant:monthly_amount_failed:%d:%d:%d", req.getMerchantId(),
                req.getYear(), req.getMonth());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionMonthlyAmountFailed> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionMonthlyAmountFailed>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached merchant monthly failed: {}", e.getMessage());
                        }
                    }
                    return repo.getMonthlyAmountTransactionFailedByMerchant(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyAmountFailed", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getMonthlyAmountFailed", e.getMessage()));
    }

    @Override
    public Future<List<TransactionYearlyAmountFailed>> getYearlyAmountTransactionFailedByMerchant(
            FindYearlyMerchantStatsRequest req) {
        var ctx = metrics.startSpan("TransactionStatsByMerchantService.getYearlyAmountFailed");
        String cacheKey = String.format("report:merchant:yearly_amount_failed:%d:%d", req.getMerchantId(),
                req.getYear());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionYearlyAmountFailed> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionYearlyAmountFailed>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached merchant yearly failed: {}", e.getMessage());
                        }
                    }
                    return repo.getYearlyAmountTransactionFailedByMerchant(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyAmountFailed", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getYearlyAmountFailed", e.getMessage()));
    }

    @Override
    public Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsByMerchantSuccess(
            FindMonthlyMerchantStatsRequest req) {
        var ctx = metrics.startSpan("TransactionStatsByMerchantService.getMonthlyMethodSuccess");
        String cacheKey = String.format("report:merchant:monthly_method_success:%d:%d:%d", req.getMerchantId(),
                req.getYear(), req.getMonth());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionMonthlyMethod> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionMonthlyMethod>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached merchant monthly method success: {}",
                                    e.getMessage());
                        }
                    }
                    return repo.getMonthlyTransactionMethodsByMerchantSuccess(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyMethodSuccess", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getMonthlyMethodSuccess", e.getMessage()));
    }

    @Override
    public Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsByMerchantFailed(
            FindMonthlyMerchantStatsRequest req) {
        var ctx = metrics.startSpan("TransactionStatsByMerchantService.getMonthlyMethodFailed");
        String cacheKey = String.format("report:merchant:monthly_method_failed:%d:%d:%d", req.getMerchantId(),
                req.getYear(), req.getMonth());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionMonthlyMethod> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionMonthlyMethod>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached merchant monthly method failed: {}", e.getMessage());
                        }
                    }
                    return repo.getMonthlyTransactionMethodsByMerchantFailed(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyMethodFailed", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getMonthlyMethodFailed", e.getMessage()));
    }

    @Override
    public Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsByMerchantSuccess(
            FindYearlyMerchantStatsRequest req) {
        var ctx = metrics.startSpan("TransactionStatsByMerchantService.getYearlyMethodSuccess");
        String cacheKey = String.format("report:merchant:yearly_method_success:%d:%d", req.getMerchantId(),
                req.getYear());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionYearlyMethod> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionYearlyMethod>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached merchant yearly method success: {}", e.getMessage());
                        }
                    }
                    return repo.getYearlyTransactionMethodsByMerchantSuccess(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyMethodSuccess", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getYearlyMethodSuccess", e.getMessage()));
    }

    @Override
    public Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsByMerchantFailed(
            FindYearlyMerchantStatsRequest req) {
        var ctx = metrics.startSpan("TransactionStatsByMerchantService.getYearlyMethodFailed");
        String cacheKey = String.format("report:merchant:yearly_method_failed:%d:%d", req.getMerchantId(),
                req.getYear());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionYearlyMethod> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionYearlyMethod>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached merchant yearly method failed: {}", e.getMessage());
                        }
                    }
                    return repo.getYearlyTransactionMethodsByMerchantFailed(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyMethodFailed", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getYearlyMethodFailed", e.getMessage()));
    }
}