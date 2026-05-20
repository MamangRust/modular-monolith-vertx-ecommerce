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
import io.example.category.repository.CategoryStatsByIdRepository;
import io.example.category.service.CategoryStatsByIdService;
import io.vertx.core.Future;
import pb.category.CategoryCommon.FindYearMonthTotalPriceById;
import pb.category.CategoryCommon.FindYearTotalPriceById;
import pb.category.CategoryCommon.FindYearCategoryById;

public class CategoryStatsByIdServiceImpl implements CategoryStatsByIdService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryStatsByIdServiceImpl.class);

    private final CategoryStatsByIdRepository repo;
    private final TracingMetrics metrics;

    public CategoryStatsByIdServiceImpl(CategoryStatsByIdRepository repo, TracingMetrics metrics) {
        this.repo = repo;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponse<List<CategoriesMonthlyTotalPrice>>> getMonthlyTotalPriceById(FindYearMonthTotalPriceById req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryStatsByIdService.getMonthlyTotalPriceById");
        logger.info("Fetching monthly total price by ID: categoryId={}", req.getCategoryId());

        return repo.getMonthlyTotalPriceById(req)
                .map(res -> {
                    metrics.completeSpanSuccess(tracingContext, "getMonthlyTotalPriceById", "Fetched successfully");
                    return ApiResponse.success("Monthly total price by ID fetched successfully", res);
                })
                .recover(err -> {
                    logger.error("Failed to fetch monthly total price by ID", err);
                    metrics.completeSpanError(tracingContext, "getMonthlyTotalPriceById", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<List<CategoriesMonthlyTotalPrice>>error("Failed to fetch: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<CategoriesYearlyTotalPrice>>> getYearlyTotalPriceById(FindYearTotalPriceById req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryStatsByIdService.getYearlyTotalPriceById");
        logger.info("Fetching yearly total price by ID: categoryId={}", req.getCategoryId());

        return repo.getYearlyTotalPriceById(req)
                .map(res -> {
                    metrics.completeSpanSuccess(tracingContext, "getYearlyTotalPriceById", "Fetched successfully");
                    return ApiResponse.success("Yearly total price by ID fetched successfully", res);
                })
                .recover(err -> {
                    logger.error("Failed to fetch yearly total price by ID", err);
                    metrics.completeSpanError(tracingContext, "getYearlyTotalPriceById", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<List<CategoriesYearlyTotalPrice>>error("Failed to fetch: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<CategoriesMonthPrice>>> getMonthlyCategoryById(FindYearCategoryById req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryStatsByIdService.getMonthlyCategoryById");
        logger.info("Fetching monthly category by ID: categoryId={}", req.getCategoryId());

        return repo.getMonthlyCategoryById(req)
                .map(res -> {
                    metrics.completeSpanSuccess(tracingContext, "getMonthlyCategoryById", "Fetched successfully");
                    return ApiResponse.success("Monthly category stats by ID fetched successfully", res);
                })
                .recover(err -> {
                    logger.error("Failed to fetch monthly category stats by ID", err);
                    metrics.completeSpanError(tracingContext, "getMonthlyCategoryById", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<List<CategoriesMonthPrice>>error("Failed to fetch: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<CategoriesYearPrice>>> getYearlyCategoryById(FindYearCategoryById req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryStatsByIdService.getYearlyCategoryById");
        logger.info("Fetching yearly category by ID: categoryId={}", req.getCategoryId());

        return repo.getYearlyCategoryById(req)
                .map(res -> {
                    metrics.completeSpanSuccess(tracingContext, "getYearlyCategoryById", "Fetched successfully");
                    return ApiResponse.success("Yearly category stats by ID fetched successfully", res);
                })
                .recover(err -> {
                    logger.error("Failed to fetch yearly category stats by ID", err);
                    metrics.completeSpanError(tracingContext, "getYearlyCategoryById", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<List<CategoriesYearPrice>>error("Failed to fetch: " + err.getMessage()));
                });
    }
}
