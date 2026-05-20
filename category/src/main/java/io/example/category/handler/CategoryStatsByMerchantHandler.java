package io.example.category.handler;

import io.example.category.service.CategoryStatsByMerchantService;
import io.vertx.core.Future;
import pb.category.CategoryCommon;

public class CategoryStatsByMerchantHandler implements pb.category.VertxCategoryStatsByMerchantServiceGrpcServer.CategoryStatsByMerchantServiceApi {

    private final CategoryStatsByMerchantService service;

    public CategoryStatsByMerchantHandler(CategoryStatsByMerchantService service) {
        this.service = service;
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthlyTotalPrice> findMonthlyTotalPricesByMerchant(CategoryCommon.FindYearMonthTotalPriceByMerchant req) {
        return service.getMonthlyTotalPriceByMerchant(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryMonthlyTotalPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoriesMonthlyTotalPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearlyTotalPrice> findYearlyTotalPricesByMerchant(CategoryCommon.FindYearTotalPriceByMerchant req) {
        return service.getYearlyTotalPriceByMerchant(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryYearlyTotalPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoriesYearlyTotalPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthPrice> findMonthPriceByMerchant(CategoryCommon.FindYearCategoryByMerchant req) {
        return service.getMonthlyCategoryByMerchant(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryMonthPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoryMonthPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearPrice> findYearPriceByMerchant(CategoryCommon.FindYearCategoryByMerchant req) {
        return service.getYearlyCategoryByMerchant(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryYearPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoryYearPriceResponse).toList())
                        .build());
    }
}
