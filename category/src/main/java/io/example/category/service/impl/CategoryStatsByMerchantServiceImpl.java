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
import io.example.category.repository.CategoryStatsByMerchantRepository;
import io.example.category.service.CategoryStatsByMerchantService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import io.example.category.domain.requests.FindYearMonthTotalPriceByMerchantRequest;
import io.example.category.domain.requests.FindYearTotalPriceByMerchantRequest;
import io.example.category.domain.requests.FindYearCategoryByMerchantRequest;

@RequiredArgsConstructor
public class CategoryStatsByMerchantServiceImpl implements CategoryStatsByMerchantService {
        private static final Logger logger = LoggerFactory.getLogger(CategoryStatsByMerchantServiceImpl.class);

        private final CategoryStatsByMerchantRepository repo;
        private final RedisService redis;
        private final TracingMetrics metrics;

        private static final String CACHE_PREFIX = "category:stats:bymerchant:";
        private static final Duration CACHE_TTL = Duration.ofMinutes(10);

        @Override
        public Future<List<CategoriesMonthlyTotalPrice>> getMonthlyTotalPriceByMerchant(
                        FindYearMonthTotalPriceByMerchantRequest req) {
                String cacheKey = CACHE_PREFIX + "monthly_price:" + req.getMerchantId() + ":" + req.getYear() + ":"
                                + req.getMonth();
                var ctx = metrics.startSpan("CategoryStatsByMerchantService.getMonthlyTotalPriceByMerchant");

                logger.info("Fetching monthly total price: year={}, month={}", req.getYear(), req.getMonth());

                return redis.getJsonList(cacheKey, CategoriesMonthlyTotalPrice.class)
                                .compose(cached -> {
                                        if (!cached.isEmpty()) {
                                                logger.info("Cached data found for monthly total price: year={}, month={}",
                                                                req.getYear(), req.getMonth());
                                                return Future.succeededFuture(cached);
                                        }
                                        logger.info("No cached data found for monthly total price: year={}, month={}",
                                                        req.getYear(), req.getMonth());
                                        return repo.getMonthlyTotalPriceByMerchant(req)
                                                        .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL)
                                                                        .map(v -> res));
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyTotalPriceByMerchant",
                                                "Success"))
                                .onFailure(e -> {
                                        logger.error("Failed to fetch monthly total price", e);
                                        metrics.completeSpanError(ctx, "getMonthlyTotalPriceByMerchant",
                                                        e.getMessage());
                                });
        }

        @Override
        public Future<List<CategoriesYearlyTotalPrice>> getYearlyTotalPriceByMerchant(
                        FindYearTotalPriceByMerchantRequest req) {
                String cacheKey = CACHE_PREFIX + "yearly_price:" + req.getMerchantId() + ":" + req.getYear();
                var ctx = metrics.startSpan("CategoryStatsByMerchantService.getYearlyTotalPriceByMerchant");

                logger.info("Fetching yearly total price: year={}", req.getYear());

                return redis.getJsonList(cacheKey, CategoriesYearlyTotalPrice.class)
                                .compose(cached -> {
                                        if (!cached.isEmpty()) {
                                                logger.info("Cached data found for yearly total price: year={}",
                                                                req.getYear());
                                                return Future.succeededFuture(cached);
                                        }
                                        logger.info("No cached data found for yearly total price: year={}",
                                                        req.getYear());
                                        return repo.getYearlyTotalPriceByMerchant(req)
                                                        .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL)
                                                                        .map(v -> res));
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyTotalPriceByMerchant",
                                                "Success"))
                                .onFailure(e -> {
                                        logger.error("Failed to fetch yearly total price", e);
                                        metrics.completeSpanError(ctx, "getYearlyTotalPriceByMerchant",
                                                        e.getMessage());
                                });
        }

        @Override
        public Future<List<CategoriesMonthPrice>> getMonthlyCategoryByMerchant(FindYearCategoryByMerchantRequest req) {
                String cacheKey = CACHE_PREFIX + "monthly_category:" + req.getMerchantId() + ":" + req.getYear();
                var ctx = metrics.startSpan("CategoryStatsByMerchantService.getMonthlyCategoryByMerchant");

                logger.info("Fetching monthly category: year={}, month={}", req.getYear());

                return redis.getJsonList(cacheKey, CategoriesMonthPrice.class)
                                .compose(cached -> {
                                        if (!cached.isEmpty()) {
                                                logger.info("Cached data found for monthly category: year={}",
                                                                req.getYear());
                                                return Future.succeededFuture(cached);
                                        }
                                        logger.info("No cached data found for monthly category: year={}",
                                                        req.getYear());
                                        return repo.getMonthlyCategoryByMerchant(req)
                                                        .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL)
                                                                        .map(v -> res));
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMonthlyCategoryByMerchant",
                                                "Success"))
                                .onFailure(e -> {
                                        logger.error("Failed to fetch monthly category", e);
                                        metrics.completeSpanError(ctx, "getMonthlyCategoryByMerchant",
                                                        e.getMessage());
                                });
        }

        @Override
        public Future<List<CategoriesYearPrice>> getYearlyCategoryByMerchant(FindYearCategoryByMerchantRequest req) {
                String cacheKey = CACHE_PREFIX + "yearly_category:" + req.getMerchantId() + ":" + req.getYear();
                var ctx = metrics.startSpan("CategoryStatsByMerchantService.getYearlyCategoryByMerchant");

                logger.info("Fetching yearly category: year={}", req.getYear());

                return redis.getJsonList(cacheKey, CategoriesYearPrice.class)
                                .compose(cached -> {
                                        if (!cached.isEmpty()) {
                                                logger.info("Cached data found for yearly category: year={}",
                                                                req.getYear());
                                                return Future.succeededFuture(cached);
                                        }
                                        logger.info("No cached data found for yearly category: year={}", req.getYear());
                                        return repo.getYearlyCategoryByMerchant(req)
                                                        .compose(res -> redis.setJsonList(cacheKey, res, CACHE_TTL)
                                                                        .map(v -> res));
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getYearlyCategoryByMerchant",
                                                "Success"))
                                .onFailure(e -> {
                                        logger.error("Failed to fetch yearly category", e);
                                        metrics.completeSpanError(ctx, "getYearlyCategoryByMerchant",
                                                        e.getMessage());
                                });
        }
}