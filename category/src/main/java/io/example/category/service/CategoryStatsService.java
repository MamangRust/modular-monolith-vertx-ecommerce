package io.example.category.service;

import java.util.List;
import io.example.common.model.ApiResponse;
import io.example.category.model.CategoriesMonthPrice;
import io.example.category.model.CategoriesMonthlyTotalPrice;
import io.example.category.model.CategoriesYearPrice;
import io.example.category.model.CategoriesYearlyTotalPrice;
import io.vertx.core.Future;

public interface CategoryStatsService {
    Future<ApiResponse<List<CategoriesMonthlyTotalPrice>>> getMonthlyTotalPrice(int year, int month);
    Future<ApiResponse<List<CategoriesYearlyTotalPrice>>> getYearlyTotalPrice(int year);
    Future<ApiResponse<List<CategoriesMonthPrice>>> getMonthlyCategory(int year);
    Future<ApiResponse<List<CategoriesYearPrice>>> getYearlyCategory(int year);
}
