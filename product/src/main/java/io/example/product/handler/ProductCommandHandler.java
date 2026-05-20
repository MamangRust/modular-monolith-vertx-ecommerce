package io.example.product.handler;

import com.google.protobuf.Empty;
import io.example.product.service.ProductCommandService;
import io.vertx.core.Future;
import pb.product.ProductCommon.*;
import pb.product.ProductCommand.*;
import pb.product.VertxProductCommandServiceGrpcServer.ProductCommandServiceApi;

public class ProductCommandHandler implements ProductCommandServiceApi {
    private final ProductCommandService service;

    public ProductCommandHandler(ProductCommandService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponseProduct> create(CreateProductRequest req) {
        io.example.product.model.CreateProductRequest businessReq = io.example.product.model.CreateProductRequest.builder()
                .merchantId((long) req.getMerchantId())
                .categoryId((long) req.getCategoryId())
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .countInStock(req.getCountInStock())
                .brand(req.getBrand())
                .weight(req.getWeight())
                .rating(req.getRating())
                .slugProduct(req.getSlugProduct())
                .imageProduct(req.getImageProduct())
                .build();

        return service.create(businessReq)
                .map(resp -> {
                    ApiResponseProduct.Builder builder = ApiResponseProduct.newBuilder()
                            .setStatus(resp.status() != null ? resp.status() : "")
                            .setMessage(resp.message() != null ? resp.message() : "");
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.fromProductResponse(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseProduct> update(UpdateProductRequest req) {
        io.example.product.model.UpdateProductRequest businessReq = io.example.product.model.UpdateProductRequest.builder()
                .productId((long) req.getProductId())
                .merchantId((long) req.getMerchantId())
                .categoryId((long) req.getCategoryId())
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .countInStock(req.getCountInStock())
                .brand(req.getBrand())
                .weight(req.getWeight())
                .rating(req.getRating())
                .slugProduct(req.getSlugProduct())
                .imageProduct(req.getImageProduct())
                .build();

        return service.update(businessReq)
                .map(resp -> {
                    ApiResponseProduct.Builder builder = ApiResponseProduct.newBuilder()
                            .setStatus(resp.status() != null ? resp.status() : "")
                            .setMessage(resp.message() != null ? resp.message() : "");
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.fromProductResponse(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseProduct> updateProductCountStock(UpdateProductCountStockRequest req) {
        return service.updateProductCountStock((long) req.getProductId(), req.getStock())
                .map(resp -> {
                    ApiResponseProduct.Builder builder = ApiResponseProduct.newBuilder()
                            .setStatus(resp.status() != null ? resp.status() : "")
                            .setMessage(resp.message() != null ? resp.message() : "");
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.fromProductResponse(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseProductDeleteAt> trashedProduct(FindByIdProductRequest req) {
        return service.trash((long) req.getId())
                .map(resp -> {
                    ApiResponseProductDeleteAt.Builder builder = ApiResponseProductDeleteAt.newBuilder()
                            .setStatus(resp.status() != null ? resp.status() : "")
                            .setMessage(resp.message() != null ? resp.message() : "");
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.fromProductResponseDeleteAt(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseProductDeleteAt> restoreProduct(FindByIdProductRequest req) {
        return service.restore((long) req.getId())
                .map(resp -> {
                    ApiResponseProductDeleteAt.Builder builder = ApiResponseProductDeleteAt.newBuilder()
                            .setStatus(resp.status() != null ? resp.status() : "")
                            .setMessage(resp.message() != null ? resp.message() : "");
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.fromProductResponseDeleteAt(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseProductDelete> deleteProductPermanent(FindByIdProductRequest req) {
        return service.deletePermanent((long) req.getId())
                .map(resp -> ApiResponseProductDelete.newBuilder()
                        .setStatus(resp.status() != null ? resp.status() : "")
                        .setMessage(resp.message() != null ? resp.message() : "")
                        .build());
    }

    @Override
    public Future<ApiResponseProductAll> restoreAllProduct(Empty req) {
        return service.restoreAll()
                .map(resp -> ApiResponseProductAll.newBuilder()
                        .setStatus(resp.status() != null ? resp.status() : "")
                        .setMessage(resp.message() != null ? resp.message() : "")
                        .build());
    }

    @Override
    public Future<ApiResponseProductAll> deleteAllProductPermanent(Empty req) {
        return service.deleteAllPermanent()
                .map(resp -> ApiResponseProductAll.newBuilder()
                        .setStatus(resp.status() != null ? resp.status() : "")
                        .setMessage(resp.message() != null ? resp.message() : "")
                        .build());
    }
}
