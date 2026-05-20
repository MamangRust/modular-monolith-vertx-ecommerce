package io.example.category.handler;

import io.example.category.service.*;
import io.vertx.core.Future;
import pb.category.CategoryCommon;
import pb.category.CategoryQuery;

public class CategoryQueryHandler implements pb.category.VertxCategoryQueryServiceGrpcServer.CategoryQueryServiceApi {

    private final CategoryQueryService queryService;
    private final CategoryStatsService statsService;
    private final CategoryStatsByIdService statsByIdService;
    private final CategoryStatsByMerchantService statsByMerchantService;

    public CategoryQueryHandler(
            CategoryQueryService queryService,
            CategoryStatsService statsService,
            CategoryStatsByIdService statsByIdService,
            CategoryStatsByMerchantService statsByMerchantService) {
        this.queryService = queryService;
        this.statsService = statsService;
        this.statsByIdService = statsByIdService;
        this.statsByMerchantService = statsByMerchantService;
    }

    private pb.Api.PaginationMeta toMeta(io.example.common.model.PaginationMeta meta) {
        if (meta == null) {
            return pb.Api.PaginationMeta.getDefaultInstance();
        }
        return pb.Api.PaginationMeta.newBuilder()
                .setCurrentPage(meta.currentPage())
                .setPageSize(meta.pageSize())
                .setTotalPages(meta.totalPages())
                .setTotalRecords(meta.totalRecords())
                .build();
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthlyTotalPrice> findMonthlyTotalPrices(CategoryCommon.FindYearMonthTotalPrices req) {
        return statsService.getMonthlyTotalPrice(req.getYear(), req.getMonth())
                .map(resp -> CategoryCommon.ApiResponseCategoryMonthlyTotalPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoriesMonthlyTotalPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearlyTotalPrice> findYearlyTotalPrices(CategoryCommon.FindYearTotalPrices req) {
        return statsService.getYearlyTotalPrice(req.getYear())
                .map(resp -> CategoryCommon.ApiResponseCategoryYearlyTotalPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoriesYearlyTotalPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthlyTotalPrice> findMonthlyTotalPricesById(CategoryCommon.FindYearMonthTotalPriceById req) {
        return statsByIdService.getMonthlyTotalPriceById(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryMonthlyTotalPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoriesMonthlyTotalPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearlyTotalPrice> findYearlyTotalPricesById(CategoryCommon.FindYearTotalPriceById req) {
        return statsByIdService.getYearlyTotalPriceById(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryYearlyTotalPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoriesYearlyTotalPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthlyTotalPrice> findMonthlyTotalPricesByMerchant(CategoryCommon.FindYearMonthTotalPriceByMerchant req) {
        return statsByMerchantService.getMonthlyTotalPriceByMerchant(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryMonthlyTotalPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoriesMonthlyTotalPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearlyTotalPrice> findYearlyTotalPricesByMerchant(CategoryCommon.FindYearTotalPriceByMerchant req) {
        return statsByMerchantService.getYearlyTotalPriceByMerchant(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryYearlyTotalPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoriesYearlyTotalPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthPrice> findMonthPrice(CategoryCommon.FindYearCategory req) {
        return statsService.getMonthlyCategory(req.getYear())
                .map(resp -> CategoryCommon.ApiResponseCategoryMonthPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoryMonthPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearPrice> findYearPrice(CategoryCommon.FindYearCategory req) {
        return statsService.getYearlyCategory(req.getYear())
                .map(resp -> CategoryCommon.ApiResponseCategoryYearPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoryYearPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthPrice> findMonthPriceByMerchant(CategoryCommon.FindYearCategoryByMerchant req) {
        return statsByMerchantService.getMonthlyCategoryByMerchant(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryMonthPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoryMonthPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearPrice> findYearPriceByMerchant(CategoryCommon.FindYearCategoryByMerchant req) {
        return statsByMerchantService.getYearlyCategoryByMerchant(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryYearPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoryYearPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryMonthPrice> findMonthPriceById(CategoryCommon.FindYearCategoryById req) {
        return statsByIdService.getMonthlyCategoryById(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryMonthPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoryMonthPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryYearPrice> findYearPriceById(CategoryCommon.FindYearCategoryById req) {
        return statsByIdService.getYearlyCategoryById(req)
                .map(resp -> CategoryCommon.ApiResponseCategoryYearPrice.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoryYearPriceResponse).toList())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponsePaginationCategory> findAll(CategoryQuery.FindAllCategoryRequest req) {
        return queryService.getAll(req)
                .map(resp -> CategoryCommon.ApiResponsePaginationCategory.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoryResponse).toList())
                        .setPagination(toMeta(resp.pagination()))
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategory> findById(CategoryCommon.FindByIdCategoryRequest req) {
        return queryService.getById((long) req.getId())
                .map(resp -> {
                    var builder = CategoryCommon.ApiResponseCategory.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.toCategoryResponse(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<CategoryCommon.ApiResponsePaginationCategoryDeleteAt> findByActive(CategoryQuery.FindAllCategoryRequest req) {
        return queryService.getActive(req)
                .map(resp -> CategoryCommon.ApiResponsePaginationCategoryDeleteAt.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoryResponseDeleteAt).toList())
                        .setPagination(toMeta(resp.pagination()))
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponsePaginationCategoryDeleteAt> findByTrashed(CategoryQuery.FindAllCategoryRequest req) {
        return queryService.getTrashed(req)
                .map(resp -> CategoryCommon.ApiResponsePaginationCategoryDeleteAt.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::toCategoryResponseDeleteAt).toList())
                        .setPagination(toMeta(resp.pagination()))
                        .build());
    }
}
