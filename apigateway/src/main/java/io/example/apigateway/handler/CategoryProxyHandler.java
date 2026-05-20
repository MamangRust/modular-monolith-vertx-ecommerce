package io.example.apigateway.handler;

import io.example.apigateway.utils.ProtoMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pb.category.CategoryCommon;
import pb.category.CategoryCommand;
import pb.category.CategoryQuery;
import pb.category.VertxCategoryQueryServiceGrpcClient;
import pb.category.VertxCategoryCommandServiceGrpcClient;
import pb.category.VertxCategoryStatsServiceGrpcClient;
import pb.category.VertxCategoryStatsByIdServiceGrpcClient;
import pb.category.VertxCategoryStatsByMerchantServiceGrpcClient;

public class CategoryProxyHandler {
    private final VertxCategoryQueryServiceGrpcClient queryClient;
    private final VertxCategoryCommandServiceGrpcClient commandClient;
    private final VertxCategoryStatsServiceGrpcClient statsClient;
    private final VertxCategoryStatsByIdServiceGrpcClient statsByIdClient;
    private final VertxCategoryStatsByMerchantServiceGrpcClient statsByMerchantClient;

    public CategoryProxyHandler(
            VertxCategoryQueryServiceGrpcClient queryClient,
            VertxCategoryCommandServiceGrpcClient commandClient,
            VertxCategoryStatsServiceGrpcClient statsClient,
            VertxCategoryStatsByIdServiceGrpcClient statsByIdClient,
            VertxCategoryStatsByMerchantServiceGrpcClient statsByMerchantClient) {
        this.queryClient = queryClient;
        this.commandClient = commandClient;
        this.statsClient = statsClient;
        this.statsByIdClient = statsByIdClient;
        this.statsByMerchantClient = statsByMerchantClient;
    }

    // Standard Category Listings
    public void findAll(RoutingContext ctx) {
        var req = CategoryQuery.FindAllCategoryRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findAll(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findActive(RoutingContext ctx) {
        var req = CategoryQuery.FindAllCategoryRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findByActive(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findTrashed(RoutingContext ctx) {
        var req = CategoryQuery.FindAllCategoryRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findByTrashed(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findById(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(id).build();

        queryClient.findById(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    // Mutator Commands
    public void create(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        var req = CategoryCommand.CreateCategoryRequest.newBuilder()
                .setName(body.getString("name", ""))
                .setDescription(body.getString("description", ""))
                .setImageCategory(body.getString("image_category", ""))
                .build();

        commandClient.create(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 201))
                .onFailure(ctx::fail);
    }

    public void update(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        JsonObject body = ctx.body().asJsonObject();
        var req = CategoryCommand.UpdateCategoryRequest.newBuilder()
                .setCategoryId(id)
                .setName(body.getString("name", ""))
                .setDescription(body.getString("description", ""))
                .setImageCategory(body.getString("image_category", ""))
                .build();

        commandClient.update(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void trash(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(id).build();

        commandClient.trashedCategory(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void restore(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(id).build();

        commandClient.restoreCategory(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void deletePermanent(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(id).build();

        commandClient.deleteCategoryPermanent(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void restoreAll(RoutingContext ctx) {
        commandClient.restoreAllCategory(com.google.protobuf.Empty.getDefaultInstance())
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void deleteAll(RoutingContext ctx) {
        commandClient.deleteAllCategoryPermanent(com.google.protobuf.Empty.getDefaultInstance())
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    // General Stats Mappings
    public void findMonthlyTotalPrices(RoutingContext ctx) {
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;
        int month = ctx.queryParams().contains("month") ? Integer.parseInt(ctx.queryParams().get("month")) : 0;

        var req = CategoryCommon.FindYearMonthTotalPrices.newBuilder()
                .setYear(year)
                .setMonth(month)
                .build();

        statsClient.findMonthlyTotalPrices(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findYearlyTotalPrices(RoutingContext ctx) {
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;

        var req = CategoryCommon.FindYearTotalPrices.newBuilder()
                .setYear(year)
                .build();

        statsClient.findYearlyTotalPrices(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findMonthPrice(RoutingContext ctx) {
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;

        var req = CategoryCommon.FindYearCategory.newBuilder()
                .setYear(year)
                .build();

        statsClient.findMonthPrice(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findYearPrice(RoutingContext ctx) {
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;

        var req = CategoryCommon.FindYearCategory.newBuilder()
                .setYear(year)
                .build();

        statsClient.findYearPrice(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    // Stats By Category ID
    public void findMonthlyTotalPricesById(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;
        int month = ctx.queryParams().contains("month") ? Integer.parseInt(ctx.queryParams().get("month")) : 0;

        var req = CategoryCommon.FindYearMonthTotalPriceById.newBuilder()
                .setCategoryId(id)
                .setYear(year)
                .setMonth(month)
                .build();

        statsByIdClient.findMonthlyTotalPricesById(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findYearlyTotalPricesById(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;

        var req = CategoryCommon.FindYearTotalPriceById.newBuilder()
                .setCategoryId(id)
                .setYear(year)
                .build();

        statsByIdClient.findYearlyTotalPricesById(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findMonthPriceById(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;

        var req = CategoryCommon.FindYearCategoryById.newBuilder()
                .setCategoryId(id)
                .setYear(year)
                .build();

        statsByIdClient.findMonthPriceById(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findYearPriceById(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;

        var req = CategoryCommon.FindYearCategoryById.newBuilder()
                .setCategoryId(id)
                .setYear(year)
                .build();

        statsByIdClient.findYearPriceById(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    // Stats By Merchant ID
    public void findMonthlyTotalPricesByMerchant(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;
        int month = ctx.queryParams().contains("month") ? Integer.parseInt(ctx.queryParams().get("month")) : 0;

        var req = CategoryCommon.FindYearMonthTotalPriceByMerchant.newBuilder()
                .setMerchantId(id)
                .setYear(year)
                .setMonth(month)
                .build();

        statsByMerchantClient.findMonthlyTotalPricesByMerchant(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findYearlyTotalPricesByMerchant(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;

        var req = CategoryCommon.FindYearTotalPriceByMerchant.newBuilder()
                .setMerchantId(id)
                .setYear(year)
                .build();

        statsByMerchantClient.findYearlyTotalPricesByMerchant(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findMonthPriceByMerchant(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;

        var req = CategoryCommon.FindYearCategoryByMerchant.newBuilder()
                .setMerchantId(id)
                .setYear(year)
                .build();

        statsByMerchantClient.findMonthPriceByMerchant(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findYearPriceByMerchant(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        int year = ctx.queryParams().contains("year") ? Integer.parseInt(ctx.queryParams().get("year")) : 0;

        var req = CategoryCommon.FindYearCategoryByMerchant.newBuilder()
                .setMerchantId(id)
                .setYear(year)
                .build();

        statsByMerchantClient.findYearPriceByMerchant(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    private void sendResponse(RoutingContext ctx, com.google.protobuf.MessageOrBuilder proto, int defaultStatus) {
        JsonObject json = ProtoMapper.toJson(proto);
        int status = json.getInteger("status", defaultStatus);
        ctx.response()
                .setStatusCode(status == 0 ? defaultStatus : status)
                .putHeader("Content-Type", "application/json")
                .end(json.encode());
    }
}
