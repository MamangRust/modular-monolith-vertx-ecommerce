package io.example.category.repository;

import java.util.List;
import io.example.category.model.CategoriesMonthPrice;
import io.example.category.model.CategoriesMonthlyTotalPrice;
import io.example.category.model.CategoriesYearPrice;
import io.example.category.model.CategoriesYearlyTotalPrice;
import io.vertx.core.Future;
import pb.category.CategoryCommon;

public interface CategoryStatsByIdRepository {
    Future<List<CategoriesMonthlyTotalPrice>> getMonthlyTotalPriceById(CategoryCommon.FindYearMonthTotalPriceById req);
    Future<List<CategoriesYearlyTotalPrice>> getYearlyTotalPriceById(CategoryCommon.FindYearTotalPriceById req);
    Future<List<CategoriesMonthPrice>> getMonthlyCategoryById(CategoryCommon.FindYearCategoryById req);
    Future<List<CategoriesYearPrice>> getYearlyCategoryById(CategoryCommon.FindYearCategoryById req);
}
