package io.example.transaction.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.transaction.model.*;
import io.example.transaction.repository.TransactionStatsRepository;
import io.example.transaction.service.TransactionStatsService;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;

public class TransactionStatsServiceImpl implements TransactionStatsService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionStatsServiceImpl.class);

    private final TransactionStatsRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    public TransactionStatsServiceImpl(TransactionStatsRepository repo, RedisService redis, TracingMetrics metrics) {
        this.repo = repo;
        this.redis = redis;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponse<List<TransactionMonthlyAmountSuccess>>> getMonthlyAmountTransactionSuccess(int year, int month) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsService.getMonthlyAmountSuccess");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:monthly_amount_success:%d:%d", year, month);

        return redis.getJsonList(cacheKey, TransactionMonthlyAmountSuccess.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_monthly_amount_success", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Monthly success reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getMonthlyAmountTransactionSuccess(year, month)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_monthly_amount_success", "Success");
                                return ApiResponse.success("Monthly success reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get monthly success amount reports", err);
                    metrics.completeSpanError(ctx, "get_monthly_amount_success", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<TransactionYearlyAmountSuccess>>> getYearlyAmountTransactionSuccess(int year) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsService.getYearlyAmountSuccess");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:yearly_amount_success:%d", year);

        return redis.getJsonList(cacheKey, TransactionYearlyAmountSuccess.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_yearly_amount_success", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Yearly success reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getYearlyAmountTransactionSuccess(year)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_yearly_amount_success", "Success");
                                return ApiResponse.success("Yearly success reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get yearly success amount reports", err);
                    metrics.completeSpanError(ctx, "get_yearly_amount_success", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<TransactionMonthlyAmountFailed>>> getMonthlyAmountTransactionFailed(int year, int month) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsService.getMonthlyAmountFailed");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:monthly_amount_failed:%d:%d", year, month);

        return redis.getJsonList(cacheKey, TransactionMonthlyAmountFailed.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_monthly_amount_failed", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Monthly failed reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getMonthlyAmountTransactionFailed(year, month)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_monthly_amount_failed", "Success");
                                return ApiResponse.success("Monthly failed reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get monthly failed amount reports", err);
                    metrics.completeSpanError(ctx, "get_monthly_amount_failed", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<TransactionYearlyAmountFailed>>> getYearlyAmountTransactionFailed(int year) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsService.getYearlyAmountFailed");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:yearly_amount_failed:%d", year);

        return redis.getJsonList(cacheKey, TransactionYearlyAmountFailed.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_yearly_amount_failed", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Yearly failed reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getYearlyAmountTransactionFailed(year)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_yearly_amount_failed", "Success");
                                return ApiResponse.success("Yearly failed reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get yearly failed amount reports", err);
                    metrics.completeSpanError(ctx, "get_yearly_amount_failed", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<TransactionMonthlyMethod>>> getMonthlyTransactionMethodsSuccess(int year, int month) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsService.getMonthlyTransactionMethodsSuccess");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:monthly_method_success:%d:%d", year, month);

        return redis.getJsonList(cacheKey, TransactionMonthlyMethod.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_monthly_method_success", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Monthly success methods reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getMonthlyTransactionMethodsSuccess(year, month)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_monthly_method_success", "Success");
                                return ApiResponse.success("Monthly success methods reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get monthly success methods reports", err);
                    metrics.completeSpanError(ctx, "get_monthly_method_success", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<TransactionMonthlyMethod>>> getMonthlyTransactionMethodsFailed(int year, int month) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsService.getMonthlyTransactionMethodsFailed");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:monthly_method_failed:%d:%d", year, month);

        return redis.getJsonList(cacheKey, TransactionMonthlyMethod.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_monthly_method_failed", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Monthly failed methods reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getMonthlyTransactionMethodsFailed(year, month)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_monthly_method_failed", "Success");
                                return ApiResponse.success("Monthly failed methods reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get monthly failed methods reports", err);
                    metrics.completeSpanError(ctx, "get_monthly_method_failed", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<TransactionYearlyMethod>>> getYearlyTransactionMethodsSuccess(int year) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsService.getYearlyTransactionMethodsSuccess");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:yearly_method_success:%d", year);

        return redis.getJsonList(cacheKey, TransactionYearlyMethod.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_yearly_method_success", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Yearly success methods reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getYearlyTransactionMethodsSuccess(year)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_yearly_method_success", "Success");
                                return ApiResponse.success("Yearly success methods reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get yearly success methods reports", err);
                    metrics.completeSpanError(ctx, "get_yearly_method_success", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<TransactionYearlyMethod>>> getYearlyTransactionMethodsFailed(int year) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsService.getYearlyTransactionMethodsFailed");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:yearly_method_failed:%d", year);

        return redis.getJsonList(cacheKey, TransactionYearlyMethod.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_yearly_method_failed", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Yearly failed methods reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getYearlyTransactionMethodsFailed(year)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_yearly_method_failed", "Success");
                                return ApiResponse.success("Yearly failed methods reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get yearly failed methods reports", err);
                    metrics.completeSpanError(ctx, "get_yearly_method_failed", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }
}
