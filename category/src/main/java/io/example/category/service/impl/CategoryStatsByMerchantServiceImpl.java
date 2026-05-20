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
import io.example.category.repository.CategoryStatsByMerchantRepository;
import io.example.category.service.CategoryStatsByMerchantService;
import io.vertx.core.Future;
import pb.category.CategoryCommon.FindYearMonthTotalPriceByMerchant;
import pb.category.CategoryCommon.FindYearTotalPriceByMerchant;
import pb.category.CategoryCommon.FindYearCategoryByMerchant;

public class CategoryStatsByMerchantServiceImpl implements CategoryStatsByMerchantService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryStatsByMerchantServiceImpl.class);

    private final CategoryStatsByMerchantRepository repo;
    private final TracingMetrics metrics;

    public CategoryStatsByMerchantServiceImpl(CategoryStatsByMerchantRepository repo, TracingMetrics metrics) {
        this.repo = repo;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponse<List<CategoriesMonthlyTotalPrice>>> getMonthlyTotalPriceByMerchant(FindYearMonthTotalPriceByMerchant req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryStatsByMerchantService.getMonthlyTotalPriceByMerchant");
        logger.info("Fetching monthly total price by merchant: merchantId={}", req.getMerchantId());

        return repo.getMonthlyTotalPriceByMerchant(req)
                .map(res -> {
                    metrics.completeSpanSuccess(tracingContext, "getMonthlyTotalPriceByMerchant", "Fetched successfully");
                    return ApiResponse.success("Monthly total price by merchant fetched successfully", res);
                })
                .recover(err -> {
                    logger.error("Failed to fetch monthly total price by merchant", err);
                    metrics.completeSpanError(tracingContext, "getMonthlyTotalPriceByMerchant", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<List<CategoriesMonthlyTotalPrice>>error("Failed to fetch: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<CategoriesYearlyTotalPrice>>> getYearlyTotalPriceByMerchant(FindYearTotalPriceByMerchant req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryStatsByMerchantService.getYearlyTotalPriceByMerchant");
        logger.info("Fetching yearly total price by merchant: merchantId={}", req.getMerchantId());

        return repo.getYearlyTotalPriceByMerchant(req)
                .map(res -> {
                    metrics.completeSpanSuccess(tracingContext, "getYearlyTotalPriceByMerchant", "Fetched successfully");
                    return ApiResponse.success("Yearly total price by merchant fetched successfully", res);
                })
                .recover(err -> {
                    logger.error("Failed to fetch yearly total price by merchant", err);
                    metrics.completeSpanError(tracingContext, "getYearlyTotalPriceByMerchant", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<List<CategoriesYearlyTotalPrice>>error("Failed to fetch: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<CategoriesMonthPrice>>> getMonthlyCategoryByMerchant(FindYearCategoryByMerchant req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryStatsByMerchantService.getMonthlyCategoryByMerchant");
        logger.info("Fetching monthly category by merchant: merchantId={}", req.getMerchantId());

        return repo.getMonthlyCategoryByMerchant(req)
                .map(res -> {
                    metrics.completeSpanSuccess(tracingContext, "getMonthlyCategoryByMerchant", "Fetched successfully");
                    return ApiResponse.success("Monthly category stats by merchant fetched successfully", res);
                })
                .recover(err -> {
                    logger.error("Failed to fetch monthly category stats by merchant", err);
                    metrics.completeSpanError(tracingContext, "getMonthlyCategoryByMerchant", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<List<CategoriesMonthPrice>>error("Failed to fetch: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<CategoriesYearPrice>>> getYearlyCategoryByMerchant(FindYearCategoryByMerchant req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CategoryStatsByMerchantService.getYearlyCategoryByMerchant");
        logger.info("Fetching yearly category by merchant: merchantId={}", req.getMerchantId());

        return repo.getYearlyCategoryByMerchant(req)
                .map(res -> {
                    metrics.completeSpanSuccess(tracingContext, "getYearlyCategoryByMerchant", "Fetched successfully");
                    return ApiResponse.success("Yearly category stats by merchant fetched successfully", res);
                })
                .recover(err -> {
                    logger.error("Failed to fetch yearly category stats by merchant", err);
                    metrics.completeSpanError(tracingContext, "getYearlyCategoryByMerchant", err.getMessage());
                    return Future.succeededFuture(ApiResponse.<List<CategoriesYearPrice>>error("Failed to fetch: " + err.getMessage()));
                });
    }
}
