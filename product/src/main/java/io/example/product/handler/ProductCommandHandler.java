package io.example.product.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.product.service.ProductCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.product.ProductCommon.ApiResponseProduct;
import pb.product.ProductCommon.ApiResponseProductAll;
import pb.product.ProductCommon.ApiResponseProductDelete;
import pb.product.ProductCommon.ApiResponseProductDeleteAt;
import pb.product.ProductCommon.FindByIdProductRequest;
import pb.product.ProductCommand.CreateProductRequest;
import pb.product.ProductCommand.UpdateProductCountStockRequest;
import pb.product.ProductCommand.UpdateProductRequest;
import pb.product.VertxProductCommandServiceGrpcServer.ProductCommandServiceApi;

@RequiredArgsConstructor
public class ProductCommandHandler implements ProductCommandServiceApi {
        private final ProductCommandService service;

        @Override
        public Future<ApiResponseProduct> create(CreateProductRequest req) {
                var businessReq = io.example.product.domain.requests.CreateProductRequest.builder()
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
                                .map(data -> ApiResponseProduct.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.fromProductResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseProduct> update(UpdateProductRequest req) {
                var businessReq = io.example.product.domain.requests.UpdateProductRequest.builder()
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
                                .map(data -> ApiResponseProduct.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.fromProductResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseProduct> updateProductCountStock(UpdateProductCountStockRequest req) {
                return service.updateProductCountStock(req.getProductId(), req.getStock())
                                .map(data -> ApiResponseProduct.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.fromProductResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseProductDeleteAt> trashedProduct(FindByIdProductRequest req) {
                return service.trash((long) req.getId())
                                .map(data -> ApiResponseProductDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.fromProductResponseDeleteAt(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseProductDeleteAt> restoreProduct(FindByIdProductRequest req) {
                return service.restore((long) req.getId())
                                .map(data -> ApiResponseProductDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.fromProductResponseDeleteAt(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseProductDelete> deleteProductPermanent(FindByIdProductRequest req) {
                return service.deletePermanent((long) req.getId())
                                .map(v -> ApiResponseProductDelete.newBuilder()
                                                .setStatus("success")
                                                .setMessage("Product deleted permanently")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseProductAll> restoreAllProduct(Empty req) {
                return service.restoreAll()
                                .map(v -> ApiResponseProductAll.newBuilder()
                                                .setStatus("success")
                                                .setMessage("All products restored successfully")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseProductAll> deleteAllProductPermanent(Empty req) {
                return service.deleteAllPermanent()
                                .map(v -> ApiResponseProductAll.newBuilder()
                                                .setStatus("success")
                                                .setMessage("All products permanently deleted")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }
}