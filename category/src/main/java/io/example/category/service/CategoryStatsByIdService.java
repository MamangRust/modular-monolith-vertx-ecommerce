package io.example.category.service;

import java.util.List;
import io.example.category.model.CategoriesMonthPrice;
import io.example.category.model.CategoriesMonthlyTotalPrice;
import io.example.category.model.CategoriesYearPrice;
import io.example.category.model.CategoriesYearlyTotalPrice;
import io.example.category.domain.requests.FindYearMonthTotalPriceByIdRequest;
import io.example.category.domain.requests.FindYearTotalPriceByIdRequest;
import io.example.category.domain.requests.FindYearCategoryByIdRequest;
import io.vertx.core.Future;

public interface CategoryStatsByIdService {
    Future<List<CategoriesMonthlyTotalPrice>> getMonthlyTotalPriceById(FindYearMonthTotalPriceByIdRequest req);

    Future<List<CategoriesYearlyTotalPrice>> getYearlyTotalPriceById(FindYearTotalPriceByIdRequest req);

    Future<List<CategoriesMonthPrice>> getMonthlyCategoryById(FindYearCategoryByIdRequest req);

    Future<List<CategoriesYearPrice>> getYearlyCategoryById(FindYearCategoryByIdRequest req);
}