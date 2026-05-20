package io.example.category.handler;

import io.example.category.service.CategoryStatsByIdService;
import io.vertx.core.Future;
import pb.category.CategoryCommon;

public class CategoryStatsByIdHandler implements pb.category.VertxCategoryStatsByIdServiceGrpcServer.CategoryStatsByIdServiceApi {

    private final CategoryStatsByIdService service;

    public CategoryStatsByIdHandler(CategoryStatsByIdService service) {
        this.service = service;
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthlyTotalPrice> findMonthlyTotalPricesById(CategoryCommon.FindYearMonthTotalPriceById req) {
        return service.getMonthlyTotalPriceById(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryMonthlyTotalPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoriesMonthlyTotalPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearlyTotalPrice> findYearlyTotalPricesById(CategoryCommon.FindYearTotalPriceById req) {
        return service.getYearlyTotalPriceById(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryYearlyTotalPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoriesYearlyTotalPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthPrice> findMonthPriceById(CategoryCommon.FindYearCategoryById req) {
        return service.getMonthlyCategoryById(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryMonthPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoryMonthPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearPrice> findYearPriceById(CategoryCommon.FindYearCategoryById req) {
        return service.getYearlyCategoryById(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryYearPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoryYearPriceResponse).toList())
                        .build());
    }
}
