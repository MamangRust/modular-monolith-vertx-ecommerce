package io.example.category.handler;

import io.example.category.service.CategoryStatsService;
import io.example.common.grpc.GrpcExceptionMapper;
import io.example.category.domain.requests.FindYearMonthTotalPricesRequest;
import io.example.category.domain.requests.FindYearTotalPricesRequest;
import io.example.category.domain.requests.FindYearCategoryRequest;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.category.CategoryCommon;

@RequiredArgsConstructor
public class CategoryStatsHandler implements pb.category.VertxCategoryStatsServiceGrpcServer.CategoryStatsServiceApi {

    private final CategoryStatsService service;

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthlyTotalPrice> findMonthlyTotalPrices(
            CategoryCommon.FindYearMonthTotalPrices req) {
        return service.getMonthlyTotalPrice(FindYearMonthTotalPricesRequest.builder()
                .year(req.getYear())
                .month(req.getMonth())
                .build())
                .map(res -> CategoryCommon.ApiResponseCategoryMonthlyTotalPrice.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toCategoriesMonthlyTotalPriceResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearlyTotalPrice> findYearlyTotalPrices(
            CategoryCommon.FindYearTotalPrices req) {
        return service.getYearlyTotalPrice(FindYearTotalPricesRequest.builder()
                .year(req.getYear())
                .build())
                .map(res -> CategoryCommon.ApiResponseCategoryYearlyTotalPrice.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toCategoriesYearlyTotalPriceResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthPrice> findMonthPrice(CategoryCommon.FindYearCategory req) {
        return service.getMonthlyCategory(FindYearCategoryRequest.builder()
                .year(req.getYear())
                .build())
                .map(res -> CategoryCommon.ApiResponseCategoryMonthPrice.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toCategoryMonthPriceResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearPrice> findYearPrice(CategoryCommon.FindYearCategory req) {
        return service.getYearlyCategory(FindYearCategoryRequest.builder()
                .year(req.getYear())
                .build())
                .map(res -> CategoryCommon.ApiResponseCategoryYearPrice.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(res.stream().map(ProtoConverter::toCategoryYearPriceResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }
}