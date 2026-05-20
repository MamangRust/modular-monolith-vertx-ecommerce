package io.example.category.repository;

import java.util.List;
import io.example.category.model.CategoriesMonthPrice;
import io.example.category.model.CategoriesMonthlyTotalPrice;
import io.example.category.model.CategoriesYearPrice;
import io.example.category.model.CategoriesYearlyTotalPrice;
import io.vertx.core.Future;
import pb.category.CategoryCommon;

public interface CategoryStatsByMerchantRepository {
    Future<List<CategoriesMonthlyTotalPrice>> getMonthlyTotalPriceByMerchant(CategoryCommon.FindYearMonthTotalPriceByMerchant req);
    Future<List<CategoriesYearlyTotalPrice>> getYearlyTotalPriceByMerchant(CategoryCommon.FindYearTotalPriceByMerchant req);
    Future<List<CategoriesMonthPrice>> getMonthlyCategoryByMerchant(CategoryCommon.FindYearCategoryByMerchant req);
    Future<List<CategoriesYearPrice>> getYearlyCategoryByMerchant(CategoryCommon.FindYearCategoryByMerchant req);
}
