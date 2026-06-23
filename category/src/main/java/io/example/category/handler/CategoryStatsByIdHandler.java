package io.example.category.handler;

import io.example.category.service.CategoryStatsByIdService;
import io.example.common.grpc.GrpcExceptionMapper;
import io.example.category.domain.requests.FindYearMonthTotalPriceByIdRequest;
import io.example.category.domain.requests.FindYearTotalPriceByIdRequest;
import io.example.category.domain.requests.FindYearCategoryByIdRequest;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.category.CategoryCommon;

@RequiredArgsConstructor
public class CategoryStatsByIdHandler
        implements pb.category.VertxCategoryStatsByIdServiceGrpcServer.CategoryStatsByIdServiceApi {

    private final CategoryStatsByIdService service;

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthlyTotalPrice> findMonthlyTotalPricesById(
            CategoryCommon.FindYearMonthTotalPriceById req) {
        return service.getMonthlyTotalPriceById(FindYearMonthTotalPriceByIdRequest.builder()
                .year(req.getYear())
                .month(req.getMonth())
                .categoryId((long) req.getCategoryId())
                .build())
                .map(resp -> CategoryCommon.ApiResponseCategoryMonthlyTotalPrice.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(resp.stream().map(ProtoConverter::toCategoriesMonthlyTotalPriceResponse)
                                .toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearlyTotalPrice> findYearlyTotalPricesById(
            CategoryCommon.FindYearTotalPriceById req) {
        return service.getYearlyTotalPriceById(FindYearTotalPriceByIdRequest.builder()
                .year(req.getYear())
                .categoryId((long) req.getCategoryId())
                .build())
                .map(resp -> CategoryCommon.ApiResponseCategoryYearlyTotalPrice.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(
                                resp.stream().map(ProtoConverter::toCategoriesYearlyTotalPriceResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthPrice> findMonthPriceById(
            CategoryCommon.FindYearCategoryById req) {
        return service.getMonthlyCategoryById(FindYearCategoryByIdRequest.builder()
                .year(req.getYear())
                .categoryId((long) req.getCategoryId())
                .build())
                .map(resp -> CategoryCommon.ApiResponseCategoryMonthPrice.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(resp.stream().map(ProtoConverter::toCategoryMonthPriceResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearPrice> findYearPriceById(
            CategoryCommon.FindYearCategoryById req) {
        return service.getYearlyCategoryById(FindYearCategoryByIdRequest.builder()
                .year(req.getYear())
                .categoryId((long) req.getCategoryId())
                .build())
                .map(resp -> CategoryCommon.ApiResponseCategoryYearPrice.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(resp.stream().map(ProtoConverter::toCategoryYearPriceResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }
}