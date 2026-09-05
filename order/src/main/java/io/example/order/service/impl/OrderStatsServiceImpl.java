package io.example.order.service.impl;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.order.domain.requests.*;
import io.example.order.model.OrderMonthly;
import io.example.order.model.OrderMonthlyTotalRevenue;
import io.example.order.model.OrderYearly;
import io.example.order.model.OrderYearlyTotalRevenue;
import io.example.order.repository.OrderStatsRepository;
import io.example.order.service.OrderStatsService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderStatsServiceImpl implements OrderStatsService {
    private static final Logger log = LoggerFactory.getLogger(OrderStatsServiceImpl.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final OrderStatsRepository repository;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "order:stats:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    @Override
    public Future<List<OrderMonthlyTotalRevenue>> getMonthlyTotalRevenue(MonthTotalRevenue req) {
        var ctx = metrics.startSpan("OrderStatsService.getMonthlyTotalRevenue");
        String cacheKey = String.format("%smonthly_revenue:y:%d:m:%d", CACHE_PREFIX, req.getYear(), req.getMonth());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<OrderMonthlyTotalRevenue> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<OrderMonthlyTotalRevenue>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached monthly total revenue: {}", e.getMessage());
                        }
                    }
                    return repository.getMonthlyTotalRevenue(req.getYear(), req.getMonth())
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL).map(v -> list));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyTotalRevenue", "Success"))
                .onFailure(e -> {
                    log.error("Failed monthly total revenue stats for year={}, month={}", req.getYear(), req.getMonth(), e);
                    metrics.completeSpanError(ctx, "getMonthlyTotalRevenue", e.getMessage());
                });
    }

    @Override
    public Future<List<OrderYearlyTotalRevenue>> getYearlyTotalRevenue(int year) {
        var ctx = metrics.startSpan("OrderStatsService.getYearlyTotalRevenue");
        String cacheKey = String.format("%syearly_revenue:y:%d", CACHE_PREFIX, year);

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<OrderYearlyTotalRevenue> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<OrderYearlyTotalRevenue>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached yearly total revenue: {}", e.getMessage());
                        }
                    }
                    return repository.getYearlyTotalRevenue(year)
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL).map(v -> list));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyTotalRevenue", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getYearlyTotalRevenue", e.getMessage()));
    }

    @Override
    public Future<List<OrderMonthlyTotalRevenue>> getMonthlyTotalRevenueById(MonthTotalRevenueByIdRequest req) {
        var ctx = metrics.startSpan("OrderStatsService.getMonthlyTotalRevenueById");
        String cacheKey = String.format("%smonthly_revenue_id:%d:y:%d:m:%d", CACHE_PREFIX, req.getOrderId(),
                req.getYear(), req.getMonth());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<OrderMonthlyTotalRevenue> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<OrderMonthlyTotalRevenue>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached monthly total revenue by id: {}", e.getMessage());
                        }
                    }
                    return repository.getMonthlyTotalRevenueById(req.getOrderId(), req.getYear(), req.getMonth())
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL).map(v -> list));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyTotalRevenueById", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getMonthlyTotalRevenueById", e.getMessage()));
    }

    @Override
    public Future<List<OrderYearlyTotalRevenue>> getYearlyTotalRevenueById(YearTotalRevenueByIdRequest req) {
        var ctx = metrics.startSpan("OrderStatsService.getYearlyTotalRevenueById");
        String cacheKey = String.format("%syearly_revenue_id:%d:y:%d", CACHE_PREFIX, req.getOrderId(), req.getYear());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<OrderYearlyTotalRevenue> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<OrderYearlyTotalRevenue>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached yearly total revenue by id: {}", e.getMessage());
                        }
                    }
                    return repository.getYearlyTotalRevenueById(req.getOrderId(), req.getYear())
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL).map(v -> list));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyTotalRevenueById", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getYearlyTotalRevenueById", e.getMessage()));
    }

    @Override
    public Future<List<OrderMonthlyTotalRevenue>> getMonthlyTotalRevenueByMerchant(
            MonthTotalRevenueMerchantRequest req) {
        var ctx = metrics.startSpan("OrderStatsService.getMonthlyTotalRevenueByMerchant");
        String cacheKey = String.format("%smonthly_revenue_merchant:%d:y:%d:m:%d", CACHE_PREFIX, req.getMerchantId(),
                req.getYear(), req.getMonth());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<OrderMonthlyTotalRevenue> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<OrderMonthlyTotalRevenue>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached monthly total revenue by merchant: {}",
                                    e.getMessage());
                        }
                    }
                    return repository
                            .getMonthlyTotalRevenueByMerchant(req.getMerchantId().intValue(), req.getYear(),
                                    req.getMonth())
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL).map(v -> list));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyTotalRevenueByMerchant", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getMonthlyTotalRevenueByMerchant", e.getMessage()));
    }

    @Override
    public Future<List<OrderYearlyTotalRevenue>> getYearlyTotalRevenueByMerchant(YearTotalRevenueMerchantRequest req) {
        var ctx = metrics.startSpan("OrderStatsService.getYearlyTotalRevenueByMerchant");
        String cacheKey = String.format("%syearly_revenue_merchant:%d:y:%d", CACHE_PREFIX, req.getMerchantId(),
                req.getYear());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<OrderYearlyTotalRevenue> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<OrderYearlyTotalRevenue>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached yearly total revenue by merchant: {}",
                                    e.getMessage());
                        }
                    }
                    return repository.getYearlyTotalRevenueByMerchant(req.getMerchantId().intValue(), req.getYear())
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL).map(v -> list));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyTotalRevenueByMerchant", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getYearlyTotalRevenueByMerchant", e.getMessage()));
    }

    @Override
    public Future<List<OrderMonthly>> getMonthlyOrder(int year) {
        var ctx = metrics.startSpan("OrderStatsService.getMonthlyOrder");
        String cacheKey = String.format("%smonthly_order:y:%d", CACHE_PREFIX, year);

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<OrderMonthly> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<OrderMonthly>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached monthly order stats: {}", e.getMessage());
                        }
                    }
                    return repository.getMonthlyOrder(year)
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL).map(v -> list));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyOrder", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getMonthlyOrder", e.getMessage()));
    }

    @Override
    public Future<List<OrderYearly>> getYearlyOrder(int year) {
        var ctx = metrics.startSpan("OrderStatsService.getYearlyOrder");
        String cacheKey = String.format("%syearly_order:y:%d", CACHE_PREFIX, year);

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<OrderYearly> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<OrderYearly>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached yearly order stats: {}", e.getMessage());
                        }
                    }
                    return repository.getYearlyOrder(year)
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL).map(v -> list));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyOrder", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getYearlyOrder", e.getMessage()));
    }

    @Override
    public Future<List<OrderMonthly>> getMonthlyOrderByMerchant(MonthOrderMerchantRequest req) {
        var ctx = metrics.startSpan("OrderStatsService.getMonthlyOrderByMerchant");
        String cacheKey = String.format("%smonthly_order_merchant:%d:y:%d", CACHE_PREFIX, req.getMerchantId(),
                req.getYear());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<OrderMonthly> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<OrderMonthly>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached monthly order stats by merchant: {}",
                                    e.getMessage());
                        }
                    }
                    return repository.getMonthlyOrderByMerchant(req.getMerchantId().intValue(), req.getYear())
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL).map(v -> list));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyOrderByMerchant", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getMonthlyOrderByMerchant", e.getMessage()));
    }

    @Override
    public Future<List<OrderYearly>> getYearlyOrderByMerchant(YearOrderMerchantRequest req) {
        var ctx = metrics.startSpan("OrderStatsService.getYearlyOrderByMerchant");
        String cacheKey = String.format("%syearly_order_merchant:%d:y:%d", CACHE_PREFIX, req.getMerchantId(),
                req.getYear());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            List<OrderYearly> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<List<OrderYearly>>() {
                                    });
                            return Future.succeededFuture(typedCached);
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached yearly order stats by merchant: {}", e.getMessage());
                        }
                    }
                    return repository.getYearlyOrderByMerchant(req.getMerchantId().intValue(), req.getYear())
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL).map(v -> list));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyOrderByMerchant", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getYearlyOrderByMerchant", e.getMessage()));
    }
}