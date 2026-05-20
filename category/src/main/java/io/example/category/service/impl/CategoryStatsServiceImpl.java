package io.example.category.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.category.model.CategoriesMonthPrice;
import io.example.category.model.CategoriesMonthlyTotalPrice;
import io.example.category.model.CategoriesYearPrice;
import io.example.category.model.CategoriesYearlyTotalPrice;
import io.example.category.repository.CategoryStatsRepository;
import io.example.category.service.CategoryStatsService;
import io.vertx.core.Future;

public class CategoryStatsServiceImpl implements CategoryStatsService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryStatsServiceImpl.class);

    private final CategoryStatsRepository repo;
    private final TracingMetrics metrics;

    public CategoryStatsServiceImpl(CategoryStatsRepository repo, TracingMetrics metrics) {
        this.repo = repo;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponse<List<CategoriesMonthlyTotalPrice>>> getMonthlyTotalPrice(int year, int month) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryStatsService.getMonthlyTotalPrice");
        logger.info("Fetching monthly total price: year={}, month={}", year, month);

        return repo.getMonthlyTotalPrice(year, month)
                .map(res -> {
                    metrics.completeSpanSuccess(tracingContext, "getMonthlyTotalPrice", "Fetched successfully");
                    return ApiResponse.success("Monthly total price fetched successfully", res);
                })
                .recover(err -> {
                    logger.error("Failed to fetch monthly total price", err);
                    metrics.completeSpanError(tracingContext, "getMonthlyTotalPrice", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<List<CategoriesMonthlyTotalPrice>>error("Failed to fetch: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<CategoriesYearlyTotalPrice>>> getYearlyTotalPrice(int year) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryStatsService.getYearlyTotalPrice");
        logger.info("Fetching yearly total price: year={}", year);

        return repo.getYearlyTotalPrice(year)
                .map(res -> {
                    metrics.completeSpanSuccess(tracingContext, "getYearlyTotalPrice", "Fetched successfully");
                    return ApiResponse.success("Yearly total price fetched successfully", res);
                })
                .recover(err -> {
                    logger.error("Failed to fetch yearly total price", err);
                    metrics.completeSpanError(tracingContext, "getYearlyTotalPrice", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<List<CategoriesYearlyTotalPrice>>error("Failed to fetch: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<CategoriesMonthPrice>>> getMonthlyCategory(int year) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryStatsService.getMonthlyCategory");
        logger.info("Fetching monthly category: year={}", year);

        return repo.getMonthlyCategory(year)
                .map(res -> {
                    metrics.completeSpanSuccess(tracingContext, "getMonthlyCategory", "Fetched successfully");
                    return ApiResponse.success("Monthly category stats fetched successfully", res);
                })
                .recover(err -> {
                    logger.error("Failed to fetch monthly category stats", err);
                    metrics.completeSpanError(tracingContext, "getMonthlyCategory", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<List<CategoriesMonthPrice>>error("Failed to fetch: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<CategoriesYearPrice>>> getYearlyCategory(int year) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryStatsService.getYearlyCategory");
        logger.info("Fetching yearly category: year={}", year);

        return repo.getYearlyCategory(year)
                .map(res -> {
                    metrics.completeSpanSuccess(tracingContext, "getYearlyCategory", "Fetched successfully");
                    return ApiResponse.success("Yearly category stats fetched successfully", res);
                })
                .recover(err -> {
                    logger.error("Failed to fetch yearly category stats", err);
                    metrics.completeSpanError(tracingContext, "getYearlyCategory", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<List<CategoriesYearPrice>>error("Failed to fetch: " + err.getMessage()));
                });
    }
}
