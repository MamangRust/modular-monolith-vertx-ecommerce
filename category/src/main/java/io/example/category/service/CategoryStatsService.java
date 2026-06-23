package io.example.category.service;

import java.util.List;
import io.example.category.model.CategoriesMonthPrice;
import io.example.category.model.CategoriesMonthlyTotalPrice;
import io.example.category.model.CategoriesYearPrice;
import io.example.category.model.CategoriesYearlyTotalPrice;
import io.example.category.domain.requests.FindYearMonthTotalPricesRequest;
import io.example.category.domain.requests.FindYearTotalPricesRequest;
import io.example.category.domain.requests.FindYearCategoryRequest;
import io.vertx.core.Future;

public interface CategoryStatsService {
    Future<List<CategoriesMonthlyTotalPrice>> getMonthlyTotalPrice(FindYearMonthTotalPricesRequest req);

    Future<List<CategoriesYearlyTotalPrice>> getYearlyTotalPrice(FindYearTotalPricesRequest req);

    Future<List<CategoriesMonthPrice>> getMonthlyCategory(FindYearCategoryRequest req);

    Future<List<CategoriesYearPrice>> getYearlyCategory(FindYearCategoryRequest req);
}