package io.example.apigateway.handler;

import static io.example.apigateway.utils.GrpcGatewayUtils.sendResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.example.apigateway.utils.GrpcGatewayUtils;
import io.example.common.exception.api.BadRequestException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pb.product.ProductCommand;
import pb.product.ProductCommon;
import pb.product.ProductQuery;
import pb.product.VertxProductCommandServiceGrpcClient;
import pb.product.VertxProductQueryServiceGrpcClient;

@Slf4j
@RequiredArgsConstructor
public class ProductProxyHandler {
        private final VertxProductQueryServiceGrpcClient queryClient;
        private final VertxProductCommandServiceGrpcClient commandClient;

        private static final String UPLOAD_DIRECTORY = "uploads/products/";

        public void findAll(RoutingContext ctx) {
                var req = ProductQuery.FindAllProductRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findAll(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findActive(RoutingContext ctx) {
                var req = ProductQuery.FindAllProductRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findByActive(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findTrashed(RoutingContext ctx) {
                var req = ProductQuery.FindAllProductRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findByTrashed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findById(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = ProductCommon.FindByIdProductRequest.newBuilder().setId(id).build();

                queryClient.findById(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findByMerchant(RoutingContext ctx) {
                int merchantId = GrpcGatewayUtils.getSafePathInt(ctx, "merchantId");

                var req = ProductQuery.FindAllProductMerchantRequest.newBuilder()
                                .setMerchantId(merchantId)
                                .setCategoryId(GrpcGatewayUtils.getQueryInt(ctx, "categoryId", 0))
                                .setMinPrice(GrpcGatewayUtils.getQueryInt(ctx, "minPrice", 0))
                                .setMaxPrice(GrpcGatewayUtils.getQueryInt(ctx, "maxPrice", 0))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .build();

                queryClient.findByMerchant(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findByCategory(RoutingContext ctx) {
                String categoryName = ctx.pathParam("categoryName");

                var req = ProductQuery.FindAllProductCategoryRequest.newBuilder()
                                .setCategoryName(categoryName)
                                .setMinPrice(GrpcGatewayUtils.getQueryInt(ctx, "minPrice", 0))
                                .setMaxPrice(GrpcGatewayUtils.getQueryInt(ctx, "maxPrice", 0))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .build();

                queryClient.findByCategory(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void create(RoutingContext ctx) {
                try {
                        int merchantId = GrpcGatewayUtils.getFormInteger(ctx, "merchant_id", 0);
                        int categoryId = GrpcGatewayUtils.getFormInteger(ctx, "category_id", 0);
                        String name = GrpcGatewayUtils.getFormString(ctx, "name", "");
                        String description = GrpcGatewayUtils.getFormString(ctx, "description", "");
                        int price = GrpcGatewayUtils.getFormInteger(ctx, "price", 0);
                        int countInStock = GrpcGatewayUtils.getFormInteger(ctx, "count_in_stock", 0);
                        String brand = GrpcGatewayUtils.getFormString(ctx, "brand", "");
                        int weight = GrpcGatewayUtils.getFormInteger(ctx, "weight", 0);
                        int rating = GrpcGatewayUtils.getFormInteger(ctx, "rating", 0);
                        String slugProduct = GrpcGatewayUtils.getFormString(ctx, "slug_product", "");
                        String barcode = GrpcGatewayUtils.getFormString(ctx, "barcode", "");

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

                        var req = ProductCommand.CreateProductRequest.newBuilder()
                                        .setMerchantId(merchantId)
                                        .setCategoryId(categoryId)
                                        .setName(name)
                                        .setDescription(description)
                                        .setPrice(price)
                                        .setCountInStock(countInStock)
                                        .setBrand(brand)
                                        .setWeight(weight)
                                        .setRating(rating)
                                        .setSlugProduct(slugProduct)
                                        .setImageProduct(imageUrl)
                                        .setBarcode(barcode)
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

                        int merchantId = GrpcGatewayUtils.getFormInteger(ctx, "merchant_id", 0);
                        int categoryId = GrpcGatewayUtils.getFormInteger(ctx, "category_id", 0);
                        String name = GrpcGatewayUtils.getFormString(ctx, "name", "");
                        String description = GrpcGatewayUtils.getFormString(ctx, "description", "");
                        int price = GrpcGatewayUtils.getFormInteger(ctx, "price", 0);
                        int countInStock = GrpcGatewayUtils.getFormInteger(ctx, "count_in_stock", 0);
                        String brand = GrpcGatewayUtils.getFormString(ctx, "brand", "");
                        int weight = GrpcGatewayUtils.getFormInteger(ctx, "weight", 0);
                        int rating = GrpcGatewayUtils.getFormInteger(ctx, "rating", 0);
                        String slugProduct = GrpcGatewayUtils.getFormString(ctx, "slug_product", "");
                        String barcode = GrpcGatewayUtils.getFormString(ctx, "barcode", "");

                        String existingImageUrl = GrpcGatewayUtils.getFormString(ctx, "image_product", "");
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

                        var req = ProductCommand.UpdateProductRequest.newBuilder()
                                        .setProductId(id)
                                        .setMerchantId(merchantId)
                                        .setCategoryId(categoryId)
                                        .setName(name)
                                        .setDescription(description)
                                        .setPrice(price)
                                        .setCountInStock(countInStock)
                                        .setBrand(brand)
                                        .setWeight(weight)
                                        .setRating(rating)
                                        .setSlugProduct(slugProduct)
                                        .setImageProduct(imageUrl)
                                        .setBarcode(barcode)
                                        .build();

                        commandClient.update(req)
                                        .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));

                } catch (BadRequestException e) {
                        ctx.fail(e);
                }
        }

        public void updateStock(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                JsonObject body = ctx.body().asJsonObject();

                var req = ProductCommand.UpdateProductCountStockRequest.newBuilder()
                                .setProductId(id)
                                .setStock(GrpcGatewayUtils.getJsonInteger(body, "stock", 0))
                                .build();

                commandClient.updateProductCountStock(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void trash(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = ProductCommon.FindByIdProductRequest.newBuilder().setId(id).build();

                commandClient.trashedProduct(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restore(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = ProductCommon.FindByIdProductRequest.newBuilder().setId(id).build();

                commandClient.restoreProduct(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deletePermanent(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = ProductCommon.FindByIdProductRequest.newBuilder().setId(id).build();

                commandClient.deleteProductPermanent(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restoreAll(RoutingContext ctx) {
                commandClient.restoreAllProduct(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deleteAll(RoutingContext ctx) {
                commandClient.deleteAllProductPermanent(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        private String storeUploadedFile(FileUpload fileUpload) throws IOException {
                Files.createDirectories(Paths.get(UPLOAD_DIRECTORY));

                String fileName = System.currentTimeMillis() + "_" + fileUpload.fileName();
                Path source = Paths.get(fileUpload.uploadedFileName());
                Path target = Paths.get(UPLOAD_DIRECTORY + fileName);

                Files.move(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return "/downloads/" + fileName;
        }
}