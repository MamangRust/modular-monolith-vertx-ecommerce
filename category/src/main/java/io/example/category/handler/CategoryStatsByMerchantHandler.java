package io.example.category.handler;

import io.example.category.service.CategoryStatsByMerchantService;
import io.example.common.grpc.GrpcExceptionMapper;
import io.example.category.domain.requests.FindYearMonthTotalPriceByMerchantRequest;
import io.example.category.domain.requests.FindYearTotalPriceByMerchantRequest;
import io.example.category.domain.requests.FindYearCategoryByMerchantRequest;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.category.CategoryCommon;

@RequiredArgsConstructor
public class CategoryStatsByMerchantHandler
        implements pb.category.VertxCategoryStatsByMerchantServiceGrpcServer.CategoryStatsByMerchantServiceApi {

    private final CategoryStatsByMerchantService service;

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthlyTotalPrice> findMonthlyTotalPricesByMerchant(
            CategoryCommon.FindYearMonthTotalPriceByMerchant req) {
        return service.getMonthlyTotalPriceByMerchant(FindYearMonthTotalPriceByMerchantRequest.builder()
                .year(req.getYear())
                .month(req.getMonth())
                .merchantId(req.getMerchantId())
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
    public Future<CategoryCommon.ApiResponseCategoryYearlyTotalPrice> findYearlyTotalPricesByMerchant(
            CategoryCommon.FindYearTotalPriceByMerchant req) {
        return service.getYearlyTotalPriceByMerchant(FindYearTotalPriceByMerchantRequest.builder()
                .year(req.getYear())
                .merchantId(req.getMerchantId())
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
    public Future<CategoryCommon.ApiResponseCategoryMonthPrice> findMonthPriceByMerchant(
            CategoryCommon.FindYearCategoryByMerchant req) {
        return service.getMonthlyCategoryByMerchant(FindYearCategoryByMerchantRequest.builder()
                .year(req.getYear())
                .merchantId(req.getMerchantId())
                .build())
                .map(resp -> pb.category.CategoryCommon.ApiResponseCategoryMonthPrice.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(resp.stream().map(ProtoConverter::toCategoryMonthPriceResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearPrice> findYearPriceByMerchant(
            CategoryCommon.FindYearCategoryByMerchant req) {
        return service.getYearlyCategoryByMerchant(FindYearCategoryByMerchantRequest.builder()
                .year(req.getYear())
                .merchantId(req.getMerchantId())
                .build())
                .map(resp -> CategoryCommon.ApiResponseCategoryYearPrice.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .addAllData(resp.stream().map(ProtoConverter::toCategoryYearPriceResponse).toList())
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }
}