package io.example.transaction.service.impl;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.transaction.domain.requests.FindMonthlyStatsRequest;
import io.example.transaction.model.TransactionMonthlyAmountFailed;
import io.example.transaction.model.TransactionMonthlyAmountSuccess;
import io.example.transaction.model.TransactionMonthlyMethod;
import io.example.transaction.model.TransactionYearlyAmountFailed;
import io.example.transaction.model.TransactionYearlyAmountSuccess;
import io.example.transaction.model.TransactionYearlyMethod;
import io.example.transaction.repository.TransactionStatsRepository;
import io.example.transaction.service.TransactionStatsService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionStatsServiceImpl implements TransactionStatsService {
    private static final Logger log = LoggerFactory.getLogger(TransactionStatsServiceImpl.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final TransactionStatsRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Override
    public Future<List<TransactionMonthlyAmountSuccess>> getMonthlyAmountTransactionSuccess(
            FindMonthlyStatsRequest req) {
        var ctx = metrics.startSpan("TransactionStatsService.getMonthlyAmountSuccess");
        String cacheKey = String.format("report:monthly_amount_success:%d:%d", req.getYear(), req.getMonth());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionMonthlyAmountSuccess> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionMonthlyAmountSuccess>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached monthly success: {}", e.getMessage());
                        }
                    }
                    return repo.getMonthlyAmountTransactionSuccess(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyAmountSuccess", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getMonthlyAmountSuccess", e.getMessage()));
    }

    @Override
    public Future<List<TransactionYearlyAmountSuccess>> getYearlyAmountTransactionSuccess(int year) {
        var ctx = metrics.startSpan("TransactionStatsService.getYearlyAmountSuccess");
        String cacheKey = String.format("report:yearly_amount_success:%d", year);

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionYearlyAmountSuccess> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionYearlyAmountSuccess>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached yearly success: {}", e.getMessage());
                        }
                    }
                    return repo.getYearlyAmountTransactionSuccess(year)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyAmountSuccess", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getYearlyAmountSuccess", e.getMessage()));
    }

    @Override
    public Future<List<TransactionMonthlyAmountFailed>> getMonthlyAmountTransactionFailed(FindMonthlyStatsRequest req) {
        var ctx = metrics.startSpan("TransactionStatsService.getMonthlyAmountFailed");
        String cacheKey = String.format("report:monthly_amount_failed:%d:%d", req.getYear(), req.getMonth());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionMonthlyAmountFailed> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionMonthlyAmountFailed>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached monthly failed: {}", e.getMessage());
                        }
                    }
                    return repo.getMonthlyAmountTransactionFailed(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyAmountFailed", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getMonthlyAmountFailed", e.getMessage()));
    }

    @Override
    public Future<List<TransactionYearlyAmountFailed>> getYearlyAmountTransactionFailed(int year) {
        var ctx = metrics.startSpan("TransactionStatsService.getYearlyAmountFailed");
        String cacheKey = String.format("report:yearly_amount_failed:%d", year);

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionYearlyAmountFailed> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionYearlyAmountFailed>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached yearly failed: {}", e.getMessage());
                        }
                    }
                    return repo.getYearlyAmountTransactionFailed(year)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyAmountFailed", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getYearlyAmountFailed", e.getMessage()));
    }

    @Override
    public Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsSuccess(FindMonthlyStatsRequest req) {
        var ctx = metrics.startSpan("TransactionStatsService.getMonthlyMethodSuccess");
        String cacheKey = String.format("report:monthly_method_success:%d:%d", req.getYear(), req.getMonth());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionMonthlyMethod> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionMonthlyMethod>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached monthly method success: {}", e.getMessage());
                        }
                    }
                    return repo.getMonthlyTransactionMethodsSuccess(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyMethodSuccess", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getMonthlyMethodSuccess", e.getMessage()));
    }

    @Override
    public Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsFailed(FindMonthlyStatsRequest req) {
        var ctx = metrics.startSpan("TransactionStatsService.getMonthlyMethodFailed");
        String cacheKey = String.format("report:monthly_method_failed:%d:%d", req.getYear(), req.getMonth());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionMonthlyMethod> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionMonthlyMethod>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached monthly method failed: {}", e.getMessage());
                        }
                    }
                    return repo.getMonthlyTransactionMethodsFailed(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyMethodFailed", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getMonthlyMethodFailed", e.getMessage()));
    }

    @Override
    public Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsSuccess(int year) {
        var ctx = metrics.startSpan("TransactionStatsService.getYearlyMethodSuccess");
        String cacheKey = String.format("report:yearly_method_success:%d", year);

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionYearlyMethod> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionYearlyMethod>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached yearly method success: {}", e.getMessage());
                        }
                    }
                    return repo.getYearlyTransactionMethodsSuccess(year)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyMethodSuccess", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getYearlyMethodSuccess", e.getMessage()));
    }

    @Override
    public Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsFailed(int year) {
        var ctx = metrics.startSpan("TransactionStatsService.getYearlyMethodFailed");
        String cacheKey = String.format("report:yearly_method_failed:%d", year);

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<TransactionYearlyMethod> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<TransactionYearlyMethod>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached yearly method failed: {}", e.getMessage());
                        }
                    }
                    return repo.getYearlyTransactionMethodsFailed(year)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyMethodFailed", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getYearlyMethodFailed", e.getMessage()));
    }
}