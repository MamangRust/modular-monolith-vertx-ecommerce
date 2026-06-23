package io.example.category.service.impl;

import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.category.model.CategoriesMonthPrice;
import io.example.category.model.CategoriesMonthlyTotalPrice;
import io.example.category.model.CategoriesYearPrice;
import io.example.category.model.CategoriesYearlyTotalPrice;
import io.example.category.repository.CategoryStatsRepository;
import io.example.category.service.CategoryStatsService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import io.example.category.domain.requests.FindYearMonthTotalPricesRequest;
import io.example.category.domain.requests.FindYearTotalPricesRequest;
import io.example.category.domain.requests.FindYearCategoryRequest;

@RequiredArgsConstructor
public class CategoryStatsServiceImpl implements CategoryStatsService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryStatsServiceImpl.class);

    private final CategoryStatsRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "category:stats:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Override
    public Future<List<CategoriesMonthlyTotalPrice>> getMonthlyTotalPrice(FindYearMonthTotalPricesRequest req) {
        String cacheKey = CACHE_PREFIX + "monthly_price:" + req.getYear() + ":" + req.getMonth();
        var ctx = metrics.startSpan("CategoryStatsService.getMonthlyTotalPrice");

        logger.info("Fetching monthly total price: year={}, month={}", req.getYear(), req.getMonth());

        return redis.getJsonList(cacheKey, CategoriesMonthlyTotalPrice.class)
                .compose(cached -> {
                    if (!cached.isEmpty()) {
                        logger.info("Cache hit for monthly total price: year={}, month={}", req.getYear(),
                                req.getMonth());
                        return Future.succeededFuture(cached);
                    }
                    logger.info("Cache miss for monthly total price: year={}, month={}", req.getYear(), req.getMonth());
                    return repo.getMonthlyTotalPrice(req)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyTotalPrice", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to fetch monthly total price", e);
                    metrics.completeSpanError(ctx, "getMonthlyTotalPrice", e.getMessage());
                });
    }

    @Override
    public Future<List<CategoriesYearlyTotalPrice>> getYearlyTotalPrice(FindYearTotalPricesRequest req) {
        String cacheKey = CACHE_PREFIX + "yearly_price:" + req.getYear();
        var ctx = metrics.startSpan("CategoryStatsService.getYearlyTotalPrice");

        logger.info("Fetching yearly total price: year={}", req.getYear());

        return redis.getJsonList(cacheKey, CategoriesYearlyTotalPrice.class)
                .compose(cached -> {
                    if (!cached.isEmpty()) {
                        logger.info("Cache hit for yearly total price: year={}", req.getYear());
                        return Future.succeededFuture(cached);
                    }
                    logger.info("Cache miss for yearly total price: year={}", req.getYear());
                    return repo.getYearlyTotalPrice(req)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyTotalPrice", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to fetch yearly total price", e);
                    metrics.completeSpanError(ctx, "getYearlyTotalPrice", e.getMessage());
                });
    }

    @Override
    public Future<List<CategoriesMonthPrice>> getMonthlyCategory(FindYearCategoryRequest req) {
        String cacheKey = CACHE_PREFIX + "monthly_category:" + req.getYear();
        var ctx = metrics.startSpan("CategoryStatsService.getMonthlyCategory");

        logger.info("Fetching monthly category: year={}", req.getYear());

        return redis.getJsonList(cacheKey, CategoriesMonthPrice.class)
                .compose(cached -> {
                    if (!cached.isEmpty()) {
                        logger.info("Cache hit for monthly category: year={}", req.getYear());
                        return Future.succeededFuture(cached);
                    }
                    logger.info("Cache miss for monthly category: year={}", req.getYear());
                    return repo.getMonthlyCategory(req)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyCategory", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to fetch monthly category stats", e);
                    metrics.completeSpanError(ctx, "getMonthlyCategory", e.getMessage());
                });
    }

    @Override
    public Future<List<CategoriesYearPrice>> getYearlyCategory(FindYearCategoryRequest req) {
        String cacheKey = CACHE_PREFIX + "yearly_category:" + req.getYear();
        var ctx = metrics.startSpan("CategoryStatsService.getYearlyCategory");

        logger.info("Fetching yearly category: year={}", req.getYear());

        return redis.getJsonList(cacheKey, CategoriesYearPrice.class)
                .compose(cached -> {
                    if (!cached.isEmpty()) {
                        logger.info("Cache hit for yearly category: year={}", req.getYear());
                        return Future.succeededFuture(cached);
                    }
                    logger.info("Cache miss for yearly category: year={}", req.getYear());
                    return repo.getYearlyCategory(req)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyCategory", "Success"))
                .onFailure(e -> {
                    logger.error("Failed to fetch yearly category stats", e);
                    metrics.completeSpanError(ctx, "getYearlyCategory", e.getMessage());
                });
    }
}