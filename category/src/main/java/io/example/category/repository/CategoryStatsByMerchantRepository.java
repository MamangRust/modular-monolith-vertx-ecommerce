package io.example.category.repository;

import java.util.List;
import io.example.category.model.CategoriesMonthPrice;
import io.example.category.model.CategoriesMonthlyTotalPrice;
import io.example.category.model.CategoriesYearPrice;
import io.example.category.model.CategoriesYearlyTotalPrice;
import io.example.category.domain.requests.FindYearMonthTotalPriceByMerchantRequest;
import io.example.category.domain.requests.FindYearTotalPriceByMerchantRequest;
import io.example.category.domain.requests.FindYearCategoryByMerchantRequest;
import io.vertx.core.Future;

public interface CategoryStatsByMerchantRepository {
    Future<List<CategoriesMonthlyTotalPrice>> getMonthlyTotalPriceByMerchant(FindYearMonthTotalPriceByMerchantRequest req);
    Future<List<CategoriesYearlyTotalPrice>> getYearlyTotalPriceByMerchant(FindYearTotalPriceByMerchantRequest req);
    Future<List<CategoriesMonthPrice>> getMonthlyCategoryByMerchant(FindYearCategoryByMerchantRequest req);
    Future<List<CategoriesYearPrice>> getYearlyCategoryByMerchant(FindYearCategoryByMerchantRequest req);
}
