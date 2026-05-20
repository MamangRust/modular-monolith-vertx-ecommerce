package io.example.category.handler;

import io.example.category.service.CategoryStatsService;
import io.vertx.core.Future;
import pb.category.CategoryCommon;

public class CategoryStatsHandler implements pb.category.VertxCategoryStatsServiceGrpcServer.CategoryStatsServiceApi {

    private final CategoryStatsService service;

    public CategoryStatsHandler(CategoryStatsService service) {
        this.service = service;
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthlyTotalPrice> findMonthlyTotalPrices(CategoryCommon.FindYearMonthTotalPrices req) {
        return service.getMonthlyTotalPrice(req.getYear(), req.getMonth())
                .map(resp -> CategoryCommon.ApiResponseCategoryMonthlyTotalPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoriesMonthlyTotalPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearlyTotalPrice> findYearlyTotalPrices(CategoryCommon.FindYearTotalPrices req) {
        return service.getYearlyTotalPrice(req.getYear())
                .map(resp -> CategoryCommon.ApiResponseCategoryYearlyTotalPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoriesYearlyTotalPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthPrice> findMonthPrice(CategoryCommon.FindYearCategory req) {
        return service.getMonthlyCategory(req.getYear())
                .map(resp -> CategoryCommon.ApiResponseCategoryMonthPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoryMonthPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearPrice> findYearPrice(CategoryCommon.FindYearCategory req) {
        return service.getYearlyCategory(req.getYear())
                .map(resp -> CategoryCommon.ApiResponseCategoryYearPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoryYearPriceResponse).toList())
                        .build());
    }
}
