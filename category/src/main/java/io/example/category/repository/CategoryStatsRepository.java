package io.example.category.repository;

import java.util.List;
import io.example.category.model.CategoriesMonthPrice;
import io.example.category.model.CategoriesMonthlyTotalPrice;
import io.example.category.model.CategoriesYearPrice;
import io.example.category.model.CategoriesYearlyTotalPrice;
import io.vertx.core.Future;

public interface CategoryStatsRepository {
    Future<List<CategoriesMonthlyTotalPrice>> getMonthlyTotalPrice(int year, int month);
    Future<List<CategoriesYearlyTotalPrice>> getYearlyTotalPrice(int year);
    Future<List<CategoriesMonthPrice>> getMonthlyCategory(int year);
    Future<List<CategoriesYearPrice>> getYearlyCategory(int year);
}
