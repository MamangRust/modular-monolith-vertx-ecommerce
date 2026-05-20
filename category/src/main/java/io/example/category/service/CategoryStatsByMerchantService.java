package io.example.category.service;

import java.util.List;
import io.example.common.model.ApiResponse;
import io.example.category.model.CategoriesMonthPrice;
import io.example.category.model.CategoriesMonthlyTotalPrice;
import io.example.category.model.CategoriesYearPrice;
import io.example.category.model.CategoriesYearlyTotalPrice;
import io.vertx.core.Future;
import pb.category.CategoryCommon.FindYearMonthTotalPriceByMerchant;
import pb.category.CategoryCommon.FindYearTotalPriceByMerchant;
import pb.category.CategoryCommon.FindYearCategoryByMerchant;

public interface CategoryStatsByMerchantService {
    Future<ApiResponse<List<CategoriesMonthlyTotalPrice>>> getMonthlyTotalPriceByMerchant(FindYearMonthTotalPriceByMerchant req);
    Future<ApiResponse<List<CategoriesYearlyTotalPrice>>> getYearlyTotalPriceByMerchant(FindYearTotalPriceByMerchant req);
    Future<ApiResponse<List<CategoriesMonthPrice>>> getMonthlyCategoryByMerchant(FindYearCategoryByMerchant req);
    Future<ApiResponse<List<CategoriesYearPrice>>> getYearlyCategoryByMerchant(FindYearCategoryByMerchant req);
}
