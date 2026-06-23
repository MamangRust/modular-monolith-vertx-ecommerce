package io.example.category.service.impl;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.category.domain.requests.FindYearCategoryByIdRequest;
import io.example.category.domain.requests.FindYearMonthTotalPriceByIdRequest;
import io.example.category.domain.requests.FindYearTotalPriceByIdRequest;
import io.example.category.model.CategoriesMonthPrice;
import io.example.category.model.CategoriesMonthlyTotalPrice;
import io.example.category.model.CategoriesYearPrice;
import io.example.category.model.CategoriesYearlyTotalPrice;
import io.example.category.repository.CategoryStatsByIdRepository;
import io.example.category.service.CategoryStatsByIdService;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CategoryStatsByIdServiceImpl implements CategoryStatsByIdService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryStatsByIdServiceImpl.class);

    private final CategoryStatsByIdRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "category:stats:byid:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Override
    public Future<List<CategoriesMonthlyTotalPrice>> getMonthlyTotalPriceById(FindYearMonthTotalPriceByIdRequest req) {
        String cacheKey = CACHE_PREFIX + "monthly_price:" + req.getCategoryId() + ":" + req.getYear() + ":"
                + req.getMonth();
        var ctx = metrics.startSpan("CategoryStatsByIdService.getMonthlyTotalPriceById");

        logger.info("Fetching monthly total price: year={}, month={}", req.getYear(), req.getMonth());

        return redis.getJsonList(cacheKey, CategoriesMonthlyTotalPrice.class)
                .compose(cached -> {
                    if (!cached.isEmpty()) {
                        logger.info("Cached data found for monthly total price: year={}, month={}", req.getYear(),
                                req.getMonth());
                        return Future.succeededFuture(cached);
                    }
                    logger.info("No cached data found for monthly total price: year={}, month={}", req.getYear(),
                            req.getMonth());
                    return repo.getMonthlyTotalPriceById(req)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyTotalPriceById", "Success"))
                .onFailure(e -> {
                    logger.error("Error fetching monthly total price: {}", e.getMessage());
                    metrics.completeSpanError(ctx, "getMonthlyTotalPriceById", e.getMessage());
                });
    }

    @Override
    public Future<List<CategoriesYearlyTotalPrice>> getYearlyTotalPriceById(FindYearTotalPriceByIdRequest req) {
        String cacheKey = CACHE_PREFIX + "yearly_price:" + req.getCategoryId() + ":" + req.getYear();
        var ctx = metrics.startSpan("CategoryStatsByIdService.getYearlyTotalPriceById");

        logger.info("Fetching yearly total price: year={}", req.getYear());

        return redis.getJsonList(cacheKey, CategoriesYearlyTotalPrice.class)
                .compose(cached -> {
                    if (!cached.isEmpty()) {
                        logger.info("Cached data found for yearly total price: year={}", req.getYear());
                        return Future.succeededFuture(cached);
                    }
                    logger.info("No cached data found for yearly total price: year={}", req.getYear());
                    return repo.getYearlyTotalPriceById(req)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyTotalPriceById", "Success"))
                .onFailure(e -> {
                    logger.error("Error fetching yearly total price: {}", e.getMessage());
                    metrics.completeSpanError(ctx, "getYearlyTotalPriceById", e.getMessage());
                });
    }

    @Override
    public Future<List<CategoriesMonthPrice>> getMonthlyCategoryById(FindYearCategoryByIdRequest req) {
        String cacheKey = CACHE_PREFIX + "monthly_category:" + req.getCategoryId() + ":" + req.getYear();
        var ctx = metrics.startSpan("CategoryStatsByIdService.getMonthlyCategoryById");

        logger.info("Fetching monthly category: year={}", req.getYear());

        return redis.getJsonList(cacheKey, CategoriesMonthPrice.class)
                .compose(cached -> {
                    if (!cached.isEmpty()) {
                        logger.info("Cached data found for monthly category: year={}", req.getYear());
                        return Future.succeededFuture(cached);
                    }
                    logger.info("No cached data found for monthly category: year={}", req.getYear());
                    return repo.getMonthlyCategoryById(req)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyCategoryById", "Success"))
                .onFailure(e -> {
                    logger.error("Error fetching monthly category: {}", e.getMessage());
                    metrics.completeSpanError(ctx, "getMonthlyCategoryById", e.getMessage());
                });
    }

    @Override
    public Future<List<CategoriesYearPrice>> getYearlyCategoryById(FindYearCategoryByIdRequest req) {
        String cacheKey = CACHE_PREFIX + "yearly_category:" + req.getCategoryId() + ":" + req.getYear();
        var ctx = metrics.startSpan("CategoryStatsByIdService.getYearlyCategoryById");

        logger.info("Fetching yearly category: year={}", req.getYear());

        return redis.getJsonList(cacheKey, CategoriesYearPrice.class)
                .compose(cached -> {
                    if (!cached.isEmpty()) {
                        logger.info("Cached data found for yearly category: year={}", req.getYear());

                        return Future.succeededFuture(cached);
                    }
                    logger.info("No cached data found for yearly category: year={}", req.getYear());
                    return repo.getYearlyCategoryById(req)
                            .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL).map(v -> res));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyCategoryById", "Success"))
                .onFailure(e -> {
                    logger.error("Error fetching yearly category: {}", e.getMessage());
                    metrics.completeSpanError(ctx, "getYearlyCategoryById", e.getMessage());
                });
    }
}