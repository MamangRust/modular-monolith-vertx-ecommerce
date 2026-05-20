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
import io.example.transaction.repository.TransactionStatsByMerchantRepository;
import io.example.transaction.service.TransactionStatsByMerchantService;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;

public class TransactionStatsByMerchantServiceImpl implements TransactionStatsByMerchantService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionStatsByMerchantServiceImpl.class);

    private final TransactionStatsByMerchantRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    public TransactionStatsByMerchantServiceImpl(TransactionStatsByMerchantRepository repo, RedisService redis, TracingMetrics metrics) {
        this.repo = repo;
        this.redis = redis;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponse<List<TransactionMonthlyAmountSuccess>>> getMonthlyAmountTransactionSuccessByMerchant(Integer merchantId, int year, int month) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsByMerchantService.getMonthlyAmountSuccess");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:merchant:monthly_amount_success:%d:%d:%d", merchantId, year, month);

        return redis.getJsonList(cacheKey, TransactionMonthlyAmountSuccess.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_monthly_amount_success", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Merchant monthly success reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getMonthlyAmountTransactionSuccessByMerchant(merchantId, year, month)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_monthly_amount_success", "Success");
                                return ApiResponse.success("Merchant monthly success reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get merchant monthly success amount reports", err);
                    metrics.completeSpanError(ctx, "get_monthly_amount_success", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<TransactionYearlyAmountSuccess>>> getYearlyAmountTransactionSuccessByMerchant(Integer merchantId, int year) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsByMerchantService.getYearlyAmountSuccess");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:merchant:yearly_amount_success:%d:%d", merchantId, year);

        return redis.getJsonList(cacheKey, TransactionYearlyAmountSuccess.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_yearly_amount_success", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Merchant yearly success reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getYearlyAmountTransactionSuccessByMerchant(merchantId, year)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_yearly_amount_success", "Success");
                                return ApiResponse.success("Merchant yearly success reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get merchant yearly success amount reports", err);
                    metrics.completeSpanError(ctx, "get_yearly_amount_success", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<TransactionMonthlyAmountFailed>>> getMonthlyAmountTransactionFailedByMerchant(Integer merchantId, int year, int month) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsByMerchantService.getMonthlyAmountFailed");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:merchant:monthly_amount_failed:%d:%d:%d", merchantId, year, month);

        return redis.getJsonList(cacheKey, TransactionMonthlyAmountFailed.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_monthly_amount_failed", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Merchant monthly failed reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getMonthlyAmountTransactionFailedByMerchant(merchantId, year, month)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_monthly_amount_failed", "Success");
                                return ApiResponse.success("Merchant monthly failed reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get merchant monthly failed amount reports", err);
                    metrics.completeSpanError(ctx, "get_monthly_amount_failed", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<TransactionYearlyAmountFailed>>> getYearlyAmountTransactionFailedByMerchant(Integer merchantId, int year) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsByMerchantService.getYearlyAmountFailed");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:merchant:yearly_amount_failed:%d:%d", merchantId, year);

        return redis.getJsonList(cacheKey, TransactionYearlyAmountFailed.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_yearly_amount_failed", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Merchant yearly failed reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getYearlyAmountTransactionFailedByMerchant(merchantId, year)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_yearly_amount_failed", "Success");
                                return ApiResponse.success("Merchant yearly failed reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get merchant yearly failed amount reports", err);
                    metrics.completeSpanError(ctx, "get_yearly_amount_failed", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<TransactionMonthlyMethod>>> getMonthlyTransactionMethodsByMerchantSuccess(Integer merchantId, int year, int month) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsByMerchantService.getMonthlyTransactionMethodsSuccess");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:merchant:monthly_method_success:%d:%d:%d", merchantId, year, month);

        return redis.getJsonList(cacheKey, TransactionMonthlyMethod.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_monthly_method_success", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Merchant monthly success methods reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getMonthlyTransactionMethodsByMerchantSuccess(merchantId, year, month)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_monthly_method_success", "Success");
                                return ApiResponse.success("Merchant monthly success methods reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get merchant monthly success methods reports", err);
                    metrics.completeSpanError(ctx, "get_monthly_method_success", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<TransactionMonthlyMethod>>> getMonthlyTransactionMethodsByMerchantFailed(Integer merchantId, int year, int month) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsByMerchantService.getMonthlyTransactionMethodsFailed");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:merchant:monthly_method_failed:%d:%d:%d", merchantId, year, month);

        return redis.getJsonList(cacheKey, TransactionMonthlyMethod.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_monthly_method_failed", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Merchant monthly failed methods reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getMonthlyTransactionMethodsByMerchantFailed(merchantId, year, month)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_monthly_method_failed", "Success");
                                return ApiResponse.success("Merchant monthly failed methods reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get merchant monthly failed methods reports", err);
                    metrics.completeSpanError(ctx, "get_monthly_method_failed", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<TransactionYearlyMethod>>> getYearlyTransactionMethodsByMerchantSuccess(Integer merchantId, int year) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsByMerchantService.getYearlyTransactionMethodsSuccess");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:merchant:yearly_method_success:%d:%d", merchantId, year);

        return redis.getJsonList(cacheKey, TransactionYearlyMethod.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_yearly_method_success", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Merchant yearly success methods reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getYearlyTransactionMethodsByMerchantSuccess(merchantId, year)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_yearly_method_success", "Success");
                                return ApiResponse.success("Merchant yearly success methods reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get merchant yearly success methods reports", err);
                    metrics.completeSpanError(ctx, "get_yearly_method_success", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<TransactionYearlyMethod>>> getYearlyTransactionMethodsByMerchantFailed(Integer merchantId, int year) {
        TracingMetrics.TracingContext ctx = metrics.startSpan("TransactionStatsByMerchantService.getYearlyTransactionMethodsFailed");
        Span span = Span.fromContext(ctx.getContext());

        String cacheKey = String.format("report:merchant:yearly_method_failed:%d:%d", merchantId, year);

        return redis.getJsonList(cacheKey, TransactionYearlyMethod.class)
                .compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        metrics.completeSpanSuccess(ctx, "get_yearly_method_failed", "Success (from cache)");
                        return Future.succeededFuture(ApiResponse.success("Merchant yearly failed methods reports fetched (from cache)", cached));
                    }
                    span.setAttribute("cache.hit", false);
                    return repo.getYearlyTransactionMethodsByMerchantFailed(merchantId, year)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(res))
                            .map(res -> {
                                metrics.completeSpanSuccess(ctx, "get_yearly_method_failed", "Success");
                                return ApiResponse.success("Merchant yearly failed methods reports fetched", res);
                            });
                })
                .recover(err -> {
                    logger.error("Failed to get merchant yearly failed methods reports", err);
                    metrics.completeSpanError(ctx, "get_yearly_method_failed", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }
}
