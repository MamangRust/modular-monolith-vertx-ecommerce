package io.example.category.service;

import java.util.List;
import io.example.common.model.ApiResponse;
import io.example.category.model.CategoriesMonthPrice;
import io.example.category.model.CategoriesMonthlyTotalPrice;
import io.example.category.model.CategoriesYearPrice;
import io.example.category.model.CategoriesYearlyTotalPrice;
import io.vertx.core.Future;
import pb.category.CategoryCommon.FindYearMonthTotalPriceById;
import pb.category.CategoryCommon.FindYearTotalPriceById;
import pb.category.CategoryCommon.FindYearCategoryById;

public interface CategoryStatsByIdService {
    Future<ApiResponse<List<CategoriesMonthlyTotalPrice>>> getMonthlyTotalPriceById(FindYearMonthTotalPriceById req);
    Future<ApiResponse<List<CategoriesYearlyTotalPrice>>> getYearlyTotalPriceById(FindYearTotalPriceById req);
    Future<ApiResponse<List<CategoriesMonthPrice>>> getMonthlyCategoryById(FindYearCategoryById req);
    Future<ApiResponse<List<CategoriesYearPrice>>> getYearlyCategoryById(FindYearCategoryById req);
}
