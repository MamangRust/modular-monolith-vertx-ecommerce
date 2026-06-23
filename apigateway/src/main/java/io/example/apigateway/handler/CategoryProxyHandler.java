package io.example.apigateway.handler;

import static io.example.apigateway.utils.GrpcGatewayUtils.sendResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.example.apigateway.utils.GrpcGatewayUtils;
import io.example.common.exception.api.BadRequestException;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pb.category.CategoryCommand;
import pb.category.CategoryCommon;
import pb.category.CategoryQuery;
import pb.category.VertxCategoryCommandServiceGrpcClient;
import pb.category.VertxCategoryQueryServiceGrpcClient;
import pb.category.VertxCategoryStatsByIdServiceGrpcClient;
import pb.category.VertxCategoryStatsByMerchantServiceGrpcClient;
import pb.category.VertxCategoryStatsServiceGrpcClient;

@Slf4j
@RequiredArgsConstructor
public class CategoryProxyHandler {
        private final VertxCategoryQueryServiceGrpcClient queryClient;
        private final VertxCategoryCommandServiceGrpcClient commandClient;
        private final VertxCategoryStatsServiceGrpcClient statsClient;
        private final VertxCategoryStatsByIdServiceGrpcClient statsByIdClient;
        private final VertxCategoryStatsByMerchantServiceGrpcClient statsByMerchantClient;

        private static final String UPLOAD_DIRECTORY = "uploads/categories/";

        // ==========================================
        // QUERY ENDPOINTS
        // ==========================================

        public void findAll(RoutingContext ctx) {
                var req = buildPaginationRequest(ctx);
                queryClient.findAll(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findActive(RoutingContext ctx) {
                var req = buildPaginationRequest(ctx);
                queryClient.findByActive(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findTrashed(RoutingContext ctx) {
                var req = buildPaginationRequest(ctx);
                queryClient.findByTrashed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findById(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(id).build();

                queryClient.findById(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void create(RoutingContext ctx) {
                try {
                        String name = GrpcGatewayUtils.getFormString(ctx, "name", "");
                        String description = GrpcGatewayUtils.getFormString(ctx, "description", "");
                        String slugCategory = GrpcGatewayUtils.getFormString(ctx, "slugCategory", "");

                        String imageUrl = "";

                        FileUpload imageFile = GrpcGatewayUtils.getFileUpload(ctx, "imageFile");

                        if (imageFile != null) {
                                try {
                                        imageUrl = storeUploadedFile(imageFile);
                                } catch (IOException e) {
                                        ctx.fail(new BadRequestException(
                                                        "Failed to process uploaded file: " + e.getMessage()));
                                        return;
                                }
                        }

                        var req = CategoryCommand.CreateCategoryRequest.newBuilder()
                                        .setName(name)
                                        .setDescription(description)
                                        .setSlugCategory(slugCategory)
                                        .setImageCategory(imageUrl)
                                        .build();

                        commandClient.create(req)
                                        .onSuccess(resp -> sendResponse(ctx, resp, 201))
                                        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));

                } catch (BadRequestException e) {
                        ctx.fail(e);
                }
        }

        public void update(RoutingContext ctx) {
                try {
                        int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");

                        String name = GrpcGatewayUtils.getFormString(ctx, "name", "");
                        String description = GrpcGatewayUtils.getFormString(ctx, "description", "");
                        String slugCategory = GrpcGatewayUtils.getFormString(ctx, "slugCategory", "");

                        String existingImageUrl = GrpcGatewayUtils.getFormString(ctx, "image_category", "");
                        String imageUrl = existingImageUrl;

                        FileUpload imageFile = GrpcGatewayUtils.getFileUpload(ctx, "imageFile");

                        if (imageFile != null) {
                                try {
                                        imageUrl = storeUploadedFile(imageFile);
                                } catch (IOException e) {
                                        ctx.fail(new BadRequestException(
                                                        "Failed to process uploaded file: " + e.getMessage()));
                                        return;
                                }
                        }

                        var req = CategoryCommand.UpdateCategoryRequest.newBuilder()
                                        .setCategoryId(id)
                                        .setName(name)
                                        .setDescription(description)
                                        .setSlugCategory(slugCategory)
                                        .setImageCategory(imageUrl)
                                        .build();

                        commandClient.update(req)
                                        .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));

                } catch (BadRequestException e) {
                        ctx.fail(e);
                }
        }

        public void trash(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(id).build();
                commandClient.trashedCategory(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restore(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(id).build();
                commandClient.restoreCategory(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deletePermanent(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = CategoryCommon.FindByIdCategoryRequest.newBuilder().setId(id).build();
                commandClient.deleteCategoryPermanent(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restoreAll(RoutingContext ctx) {
                commandClient.restoreAllCategory(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deleteAll(RoutingContext ctx) {
                commandClient.deleteAllCategoryPermanent(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findMonthlyTotalPrices(RoutingContext ctx) {
                int year = GrpcGatewayUtils.getQueryInt(ctx, "year", 0);
                int month = GrpcGatewayUtils.getQueryInt(ctx, "month", 0);

                statsClient.findMonthlyTotalPrices(CategoryCommon.FindYearMonthTotalPrices.newBuilder()
                                .setYear(year).setMonth(month).build())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findYearlyTotalPrices(RoutingContext ctx) {
                statsClient.findYearlyTotalPrices(CategoryCommon.FindYearTotalPrices.newBuilder()
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0)).build())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findMonthPrice(RoutingContext ctx) {
                statsClient.findMonthPrice(CategoryCommon.FindYearCategory.newBuilder()
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0)).build())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findYearPrice(RoutingContext ctx) {
                statsClient.findYearPrice(CategoryCommon.FindYearCategory.newBuilder()
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0)).build())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findMonthlyTotalPricesById(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                statsByIdClient.findMonthlyTotalPricesById(CategoryCommon.FindYearMonthTotalPriceById.newBuilder()
                                .setCategoryId(id)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                .setMonth(GrpcGatewayUtils.getQueryInt(ctx, "month", 0)).build())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findYearlyTotalPricesById(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                statsByIdClient.findYearlyTotalPricesById(CategoryCommon.FindYearTotalPriceById.newBuilder()
                                .setCategoryId(id)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0)).build())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findMonthPriceById(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                statsByIdClient.findMonthPriceById(CategoryCommon.FindYearCategoryById.newBuilder()
                                .setCategoryId(id)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0)).build())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findYearPriceById(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                statsByIdClient.findYearPriceById(CategoryCommon.FindYearCategoryById.newBuilder()
                                .setCategoryId(id)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0)).build())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findMonthlyTotalPricesByMerchant(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                statsByMerchantClient
                                .findMonthlyTotalPricesByMerchant(CategoryCommon.FindYearMonthTotalPriceByMerchant
                                                .newBuilder()
                                                .setMerchantId(id)
                                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0))
                                                .setMonth(GrpcGatewayUtils.getQueryInt(ctx, "month", 0)).build())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findYearlyTotalPricesByMerchant(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                statsByMerchantClient
                                .findYearlyTotalPricesByMerchant(CategoryCommon.FindYearTotalPriceByMerchant
                                                .newBuilder()
                                                .setMerchantId(id)
                                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0)).build())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findMonthPriceByMerchant(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                statsByMerchantClient.findMonthPriceByMerchant(CategoryCommon.FindYearCategoryByMerchant.newBuilder()
                                .setMerchantId(id)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0)).build())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findYearPriceByMerchant(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                statsByMerchantClient.findYearPriceByMerchant(CategoryCommon.FindYearCategoryByMerchant.newBuilder()
                                .setMerchantId(id)
                                .setYear(GrpcGatewayUtils.getQueryInt(ctx, "year", 0)).build())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        private String storeUploadedFile(FileUpload fileUpload) throws IOException {
                Files.createDirectories(Path.of(UPLOAD_DIRECTORY));

                String originalName = fileUpload.fileName();
                String uniqueName = System.currentTimeMillis() + "_" + originalName;
                Path target = Path.of(UPLOAD_DIRECTORY + uniqueName);

                Files.move(Path.of(fileUpload.uploadedFileName()), target,
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                return "/downloads/" + uniqueName;
        }

        private CategoryQuery.FindAllCategoryRequest buildPaginationRequest(RoutingContext ctx) {
                return CategoryQuery.FindAllCategoryRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();
        }
}