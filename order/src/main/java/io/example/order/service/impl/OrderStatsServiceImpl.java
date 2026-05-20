package io.example.order.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.order.model.OrderMonthly;
import io.example.order.model.OrderMonthlyTotalRevenue;
import io.example.order.model.OrderYearly;
import io.example.order.model.OrderYearlyTotalRevenue;
import io.example.order.repository.OrderStatsRepository;
import io.example.order.service.OrderStatsService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;

public class OrderStatsServiceImpl implements OrderStatsService {
    private static final Logger logger = LoggerFactory.getLogger(OrderStatsServiceImpl.class);

    private final OrderStatsRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "order:stats:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    public OrderStatsServiceImpl(OrderStatsRepository repo, RedisService redis, TracingMetrics metrics) {
        this.repo = repo;
        this.redis = redis;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponse<List<OrderMonthlyTotalRevenue>>> getMonthlyTotalRevenue(int year, int month) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderStatsService.getMonthlyTotalRevenue");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        String cacheKey = String.format("%smonthly_revenue:y:%d:m:%d", CACHE_PREFIX, year, month);

        return redis.getJson(cacheKey, List.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("stats.cache_hit", true);
                        @SuppressWarnings("unchecked")
                        List<OrderMonthlyTotalRevenue> typed = (List<OrderMonthlyTotalRevenue>) cached;
                        return Future.succeededFuture(ApiResponse.success("Monthly total revenue fetched from cache", typed));
                    }
                    span.setAttribute("stats.cache_hit", false);
                    return repo.getMonthlyTotalRevenue(year, month)
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL)
                                    .map(v -> ApiResponse.success("Monthly total revenue fetched successfully", list)));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "get_monthly_total_revenue", "Success"))
                .recover(err -> handleFailure(tracingContext, "get_monthly_total_revenue", err));
    }

    @Override
    public Future<ApiResponse<List<OrderYearlyTotalRevenue>>> getYearlyTotalRevenue(int year) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderStatsService.getYearlyTotalRevenue");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        String cacheKey = String.format("%syearly_revenue:y:%d", CACHE_PREFIX, year);

        return redis.getJson(cacheKey, List.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("stats.cache_hit", true);
                        @SuppressWarnings("unchecked")
                        List<OrderYearlyTotalRevenue> typed = (List<OrderYearlyTotalRevenue>) cached;
                        return Future.succeededFuture(ApiResponse.success("Yearly total revenue fetched from cache", typed));
                    }
                    span.setAttribute("stats.cache_hit", false);
                    return repo.getYearlyTotalRevenue(year)
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL)
                                    .map(v -> ApiResponse.success("Yearly total revenue fetched successfully", list)));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "get_yearly_total_revenue", "Success"))
                .recover(err -> handleFailure(tracingContext, "get_yearly_total_revenue", err));
    }

    @Override
    public Future<ApiResponse<List<OrderMonthlyTotalRevenue>>> getMonthlyTotalRevenueById(Long orderId, int year, int month) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderStatsService.getMonthlyTotalRevenueById");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));
        span.setAttribute("order.id", orderId);

        String cacheKey = String.format("%smonthly_revenue_id:%d:y:%d:m:%d", CACHE_PREFIX, orderId, year, month);

        return redis.getJson(cacheKey, List.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("stats.cache_hit", true);
                        @SuppressWarnings("unchecked")
                        List<OrderMonthlyTotalRevenue> typed = (List<OrderMonthlyTotalRevenue>) cached;
                        return Future.succeededFuture(ApiResponse.success("Monthly total revenue by ID fetched from cache", typed));
                    }
                    span.setAttribute("stats.cache_hit", false);
                    return repo.getMonthlyTotalRevenueById(orderId, year, month)
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL)
                                    .map(v -> ApiResponse.success("Monthly total revenue by ID fetched successfully", list)));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "get_monthly_total_revenue_by_id", "Success"))
                .recover(err -> handleFailure(tracingContext, "get_monthly_total_revenue_by_id", err));
    }

    @Override
    public Future<ApiResponse<List<OrderYearlyTotalRevenue>>> getYearlyTotalRevenueById(Long orderId, int year) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderStatsService.getYearlyTotalRevenueById");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));
        span.setAttribute("order.id", orderId);

        String cacheKey = String.format("%syearly_revenue_id:%d:y:%d", CACHE_PREFIX, orderId, year);

        return redis.getJson(cacheKey, List.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("stats.cache_hit", true);
                        @SuppressWarnings("unchecked")
                        List<OrderYearlyTotalRevenue> typed = (List<OrderYearlyTotalRevenue>) cached;
                        return Future.succeededFuture(ApiResponse.success("Yearly total revenue by ID fetched from cache", typed));
                    }
                    span.setAttribute("stats.cache_hit", false);
                    return repo.getYearlyTotalRevenueById(orderId, year)
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL)
                                    .map(v -> ApiResponse.success("Yearly total revenue by ID fetched successfully", list)));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "get_yearly_total_revenue_by_id", "Success"))
                .recover(err -> handleFailure(tracingContext, "get_yearly_total_revenue_by_id", err));
    }

    @Override
    public Future<ApiResponse<List<OrderMonthlyTotalRevenue>>> getMonthlyTotalRevenueByMerchant(Integer merchantId, int year, int month) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderStatsService.getMonthlyTotalRevenueByMerchant");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));
        span.setAttribute("merchant.id", (long) merchantId);

        String cacheKey = String.format("%smonthly_revenue_merchant:%d:y:%d:m:%d", CACHE_PREFIX, merchantId, year, month);

        return redis.getJson(cacheKey, List.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("stats.cache_hit", true);
                        @SuppressWarnings("unchecked")
                        List<OrderMonthlyTotalRevenue> typed = (List<OrderMonthlyTotalRevenue>) cached;
                        return Future.succeededFuture(ApiResponse.success("Monthly total revenue by merchant fetched from cache", typed));
                    }
                    span.setAttribute("stats.cache_hit", false);
                    return repo.getMonthlyTotalRevenueByMerchant(merchantId, year, month)
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL)
                                    .map(v -> ApiResponse.success("Monthly total revenue by merchant fetched successfully", list)));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "get_monthly_total_revenue_by_merchant", "Success"))
                .recover(err -> handleFailure(tracingContext, "get_monthly_total_revenue_by_merchant", err));
    }

    @Override
    public Future<ApiResponse<List<OrderYearlyTotalRevenue>>> getYearlyTotalRevenueByMerchant(Integer merchantId, int year) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderStatsService.getYearlyTotalRevenueByMerchant");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));
        span.setAttribute("merchant.id", (long) merchantId);

        String cacheKey = String.format("%syearly_revenue_merchant:%d:y:%d", CACHE_PREFIX, merchantId, year);

        return redis.getJson(cacheKey, List.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("stats.cache_hit", true);
                        @SuppressWarnings("unchecked")
                        List<OrderYearlyTotalRevenue> typed = (List<OrderYearlyTotalRevenue>) cached;
                        return Future.succeededFuture(ApiResponse.success("Yearly total revenue by merchant fetched from cache", typed));
                    }
                    span.setAttribute("stats.cache_hit", false);
                    return repo.getYearlyTotalRevenueByMerchant(merchantId, year)
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL)
                                    .map(v -> ApiResponse.success("Yearly total revenue by merchant fetched successfully", list)));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "get_yearly_total_revenue_by_merchant", "Success"))
                .recover(err -> handleFailure(tracingContext, "get_yearly_total_revenue_by_merchant", err));
    }

    @Override
    public Future<ApiResponse<List<OrderMonthly>>> getMonthlyOrder(int year) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderStatsService.getMonthlyOrder");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        String cacheKey = String.format("%smonthly_order:y:%d", CACHE_PREFIX, year);

        return redis.getJson(cacheKey, List.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("stats.cache_hit", true);
                        @SuppressWarnings("unchecked")
                        List<OrderMonthly> typed = (List<OrderMonthly>) cached;
                        return Future.succeededFuture(ApiResponse.success("Monthly order stats fetched from cache", typed));
                    }
                    span.setAttribute("stats.cache_hit", false);
                    return repo.getMonthlyOrder(year)
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL)
                                    .map(v -> ApiResponse.success("Monthly order stats fetched successfully", list)));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "get_monthly_order", "Success"))
                .recover(err -> handleFailure(tracingContext, "get_monthly_order", err));
    }

    @Override
    public Future<ApiResponse<List<OrderYearly>>> getYearlyOrder(int year) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderStatsService.getYearlyOrder");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        String cacheKey = String.format("%syearly_order:y:%d", CACHE_PREFIX, year);

        return redis.getJson(cacheKey, List.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("stats.cache_hit", true);
                        @SuppressWarnings("unchecked")
                        List<OrderYearly> typed = (List<OrderYearly>) cached;
                        return Future.succeededFuture(ApiResponse.success("Yearly order stats fetched from cache", typed));
                    }
                    span.setAttribute("stats.cache_hit", false);
                    return repo.getYearlyOrder(year)
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL)
                                    .map(v -> ApiResponse.success("Yearly order stats fetched successfully", list)));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "get_yearly_order", "Success"))
                .recover(err -> handleFailure(tracingContext, "get_yearly_order", err));
    }

    @Override
    public Future<ApiResponse<List<OrderMonthly>>> getMonthlyOrderByMerchant(Integer merchantId, int year) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderStatsService.getMonthlyOrderByMerchant");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));
        span.setAttribute("merchant.id", (long) merchantId);

        String cacheKey = String.format("%smonthly_order_merchant:%d:y:%d", CACHE_PREFIX, merchantId, year);

        return redis.getJson(cacheKey, List.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("stats.cache_hit", true);
                        @SuppressWarnings("unchecked")
                        List<OrderMonthly> typed = (List<OrderMonthly>) cached;
                        return Future.succeededFuture(ApiResponse.success("Monthly order stats by merchant fetched from cache", typed));
                    }
                    span.setAttribute("stats.cache_hit", false);
                    return repo.getMonthlyOrderByMerchant(merchantId, year)
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL)
                                    .map(v -> ApiResponse.success("Monthly order stats by merchant fetched successfully", list)));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "get_monthly_order_by_merchant", "Success"))
                .recover(err -> handleFailure(tracingContext, "get_monthly_order_by_merchant", err));
    }

    @Override
    public Future<ApiResponse<List<OrderYearly>>> getYearlyOrderByMerchant(Integer merchantId, int year) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderStatsService.getYearlyOrderByMerchant");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));
        span.setAttribute("merchant.id", (long) merchantId);

        String cacheKey = String.format("%syearly_order_merchant:%d:y:%d", CACHE_PREFIX, merchantId, year);

        return redis.getJson(cacheKey, List.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("stats.cache_hit", true);
                        @SuppressWarnings("unchecked")
                        List<OrderYearly> typed = (List<OrderYearly>) cached;
                        return Future.succeededFuture(ApiResponse.success("Yearly order stats by merchant fetched from cache", typed));
                    }
                    span.setAttribute("stats.cache_hit", false);
                    return repo.getYearlyOrderByMerchant(merchantId, year)
                            .compose(list -> redis.setJson(cacheKey, list, CACHE_TTL)
                                    .map(v -> ApiResponse.success("Yearly order stats by merchant fetched successfully", list)));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(tracingContext, "get_yearly_order_by_merchant", "Success"))
                .recover(err -> handleFailure(tracingContext, "get_yearly_order_by_merchant", err));
    }

    private <T> Future<ApiResponse<T>> handleFailure(TracingMetrics.TracingContext tracingContext, String name, Throwable err) {
        logger.error("Failed to execute statistical query: {}", name, err);
        metrics.completeSpanError(tracingContext, name, err.getMessage());
        return Future.succeededFuture(ApiResponse.error("Failed to generate report stats: " + err.getMessage()));
    }
}
