package io.example.apigateway.handler;

import io.example.apigateway.utils.ProtoMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pb.product.ProductCommon;
import pb.product.ProductCommand;
import pb.product.ProductQuery;
import pb.product.VertxProductQueryServiceGrpcClient;
import pb.product.VertxProductCommandServiceGrpcClient;

public class ProductProxyHandler {
    private final VertxProductQueryServiceGrpcClient queryClient;
    private final VertxProductCommandServiceGrpcClient commandClient;

    public ProductProxyHandler(VertxProductQueryServiceGrpcClient queryClient, VertxProductCommandServiceGrpcClient commandClient) {
        this.queryClient = queryClient;
        this.commandClient = commandClient;
    }

    public void findAll(RoutingContext ctx) {
        var req = ProductQuery.FindAllProductRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findAll(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findActive(RoutingContext ctx) {
        var req = ProductQuery.FindAllProductRequest.newBuilder()
                .setSearch(ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "")
                .setPage(ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1)
                .setPageSize(ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10)
                .build();

        queryClient.findByActive(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findTrashed(RoutingContext ctx) {
        var req = ProductQuery.FindAllProductRequest.newBuilder()
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
        var req = ProductCommon.FindByIdProductRequest.newBuilder().setId(id).build();

        queryClient.findById(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findByMerchant(RoutingContext ctx) {
        int merchantId = Integer.parseInt(ctx.pathParam("merchantId"));
        int categoryId = ctx.queryParams().contains("categoryId") ? Integer.parseInt(ctx.queryParams().get("categoryId")) : 0;
        int minPrice = ctx.queryParams().contains("minPrice") ? Integer.parseInt(ctx.queryParams().get("minPrice")) : 0;
        int maxPrice = ctx.queryParams().contains("maxPrice") ? Integer.parseInt(ctx.queryParams().get("maxPrice")) : 0;
        int page = ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1;
        int pageSize = ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10;
        String search = ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "";

        var req = ProductQuery.FindAllProductMerchantRequest.newBuilder()
                .setMerchantId(merchantId)
                .setCategoryId(categoryId)
                .setMinPrice(minPrice)
                .setMaxPrice(maxPrice)
                .setPage(page)
                .setPageSize(pageSize)
                .setSearch(search)
                .build();

        queryClient.findByMerchant(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void findByCategory(RoutingContext ctx) {
        String categoryName = ctx.pathParam("categoryName");
        int minPrice = ctx.queryParams().contains("minPrice") ? Integer.parseInt(ctx.queryParams().get("minPrice")) : 0;
        int maxPrice = ctx.queryParams().contains("maxPrice") ? Integer.parseInt(ctx.queryParams().get("maxPrice")) : 0;
        int page = ctx.queryParams().contains("page") ? Integer.parseInt(ctx.queryParams().get("page")) : 1;
        int pageSize = ctx.queryParams().contains("pageSize") ? Integer.parseInt(ctx.queryParams().get("pageSize")) : 10;
        String search = ctx.queryParams().get("search") != null ? ctx.queryParams().get("search") : "";

        var req = ProductQuery.FindAllProductCategoryRequest.newBuilder()
                .setCategoryName(categoryName)
                .setMinPrice(minPrice)
                .setMaxPrice(maxPrice)
                .setPage(page)
                .setPageSize(pageSize)
                .setSearch(search)
                .build();

        queryClient.findByCategory(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void create(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        var req = ProductCommand.CreateProductRequest.newBuilder()
                .setMerchantId(body.getInteger("merchant_id", 0))
                .setCategoryId(body.getInteger("category_id", 0))
                .setName(body.getString("name", ""))
                .setDescription(body.getString("description", ""))
                .setPrice(body.getInteger("price", 0))
                .setCountInStock(body.getInteger("count_in_stock", 0))
                .setBrand(body.getString("brand", ""))
                .setWeight(body.getInteger("weight", 0))
                .setRating(body.getInteger("rating", 0))
                .setSlugProduct(body.getString("slug_product", ""))
                .setImageProduct(body.getString("image_product", ""))
                .setBarcode(body.getString("barcode", ""))
                .build();

        commandClient.create(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 201))
                .onFailure(ctx::fail);
    }

    public void update(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        JsonObject body = ctx.body().asJsonObject();
        var req = ProductCommand.UpdateProductRequest.newBuilder()
                .setProductId(id)
                .setMerchantId(body.getInteger("merchant_id", 0))
                .setCategoryId(body.getInteger("category_id", 0))
                .setName(body.getString("name", ""))
                .setDescription(body.getString("description", ""))
                .setPrice(body.getInteger("price", 0))
                .setCountInStock(body.getInteger("count_in_stock", 0))
                .setBrand(body.getString("brand", ""))
                .setWeight(body.getInteger("weight", 0))
                .setRating(body.getInteger("rating", 0))
                .setSlugProduct(body.getString("slug_product", ""))
                .setImageProduct(body.getString("image_product", ""))
                .setBarcode(body.getString("barcode", ""))
                .build();

        commandClient.update(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void updateStock(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        JsonObject body = ctx.body().asJsonObject();
        var req = ProductCommand.UpdateProductCountStockRequest.newBuilder()
                .setProductId(id)
                .setStock(body.getInteger("stock", 0))
                .build();

        commandClient.updateProductCountStock(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void trash(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = ProductCommon.FindByIdProductRequest.newBuilder().setId(id).build();

        commandClient.trashedProduct(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void restore(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = ProductCommon.FindByIdProductRequest.newBuilder().setId(id).build();

        commandClient.restoreProduct(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void deletePermanent(RoutingContext ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        var req = ProductCommon.FindByIdProductRequest.newBuilder().setId(id).build();

        commandClient.deleteProductPermanent(req)
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void restoreAll(RoutingContext ctx) {
        commandClient.restoreAllProduct(com.google.protobuf.Empty.getDefaultInstance())
                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                .onFailure(ctx::fail);
    }

    public void deleteAll(RoutingContext ctx) {
        commandClient.deleteAllProductPermanent(com.google.protobuf.Empty.getDefaultInstance())
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
