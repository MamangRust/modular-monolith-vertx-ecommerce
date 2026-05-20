package io.example.product.handler;

import io.example.product.service.ProductQueryService;
import io.vertx.core.Future;
import pb.product.ProductCommon.ApiResponsePaginationProduct;
import pb.product.ProductCommon.ApiResponsePaginationProductDeleteAt;
import pb.product.ProductCommon.ApiResponseProduct;
import pb.product.ProductCommon.FindByIdProductRequest;
import pb.product.ProductQuery.FindAllProductCategoryRequest;
import pb.product.ProductQuery.FindAllProductMerchantRequest;
import pb.product.VertxProductQueryServiceGrpcServer.ProductQueryServiceApi;

public class ProductQueryHandler implements ProductQueryServiceApi {
        private final ProductQueryService service;

        public ProductQueryHandler(ProductQueryService service) {
                this.service = service;
        }

        private pb.Api.PaginationMeta toMeta(io.example.common.domain.PaginationMeta meta) {
                if (meta == null) {
                        return pb.Api.PaginationMeta.getDefaultInstance();
                }
                return pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(meta.currentPage() + 1) // Convert 0-based to 1-based for response if
                                                                        // needed
                                .setPageSize(meta.pageSize())
                                .setTotalPages(meta.totalPages())
                                .setTotalRecords(meta.totalRecords())
                                .build();
        }

        @Override
        public Future<ApiResponsePaginationProduct> findAll(pb.product.ProductQuery.FindAllProductRequest req) {
                io.example.product.model.FindAllProductRequest businessReq = io.example.product.model.FindAllProductRequest
                                .builder()
                                .page(req.getPage())
                                .pageSize(req.getPageSize())
                                .search(req.getSearch())
                                .build();

                return service.getAll(businessReq)
                                .map(resp -> ApiResponsePaginationProduct.newBuilder()
                                                .setStatus(resp.status() != null ? resp.status() : "")
                                                .setMessage(resp.message() != null ? resp.message() : "")
                                                .addAllData(resp.data().stream()
                                                                .map(ProtoConverter::fromProductResponse).toList())
                                                .setPagination(toMeta(resp.pagination()))
                                                .build());
        }

        @Override
        public Future<ApiResponsePaginationProduct> findByMerchant(FindAllProductMerchantRequest req) {
                return service.getByMerchant(
                                (long) req.getMerchantId(),
                                req.getSearch(),
                                (long) req.getCategoryId(),
                                req.getMinPrice(),
                                req.getMaxPrice(),
                                req.getPage(),
                                req.getPageSize())
                                .map(resp -> ApiResponsePaginationProduct.newBuilder()
                                                .setStatus(resp.status() != null ? resp.status() : "")
                                                .setMessage(resp.message() != null ? resp.message() : "")
                                                .addAllData(resp.data().stream()
                                                                .map(ProtoConverter::fromProductResponse).toList())
                                                .setPagination(toMeta(resp.pagination()))
                                                .build());
        }

        @Override
        public Future<ApiResponsePaginationProduct> findByCategory(FindAllProductCategoryRequest req) {
                return service.getByCategoryName(
                                req.getCategoryName(),
                                req.getSearch(),
                                req.getMinPrice(),
                                req.getMaxPrice(),
                                req.getPage(),
                                req.getPageSize())
                                .map(resp -> ApiResponsePaginationProduct.newBuilder()
                                                .setStatus(resp.status() != null ? resp.status() : "")
                                                .setMessage(resp.message() != null ? resp.message() : "")
                                                .addAllData(resp.data().stream()
                                                                .map(ProtoConverter::fromProductResponse).toList())
                                                .setPagination(toMeta(resp.pagination()))
                                                .build());
        }

        @Override
        public Future<ApiResponseProduct> findById(FindByIdProductRequest req) {
                return service.getById((long) req.getId())
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
        public Future<ApiResponsePaginationProductDeleteAt> findByActive(
                        pb.product.ProductQuery.FindAllProductRequest req) {
                io.example.product.model.FindAllProductRequest businessReq = io.example.product.model.FindAllProductRequest
                                .builder()
                                .page(req.getPage())
                                .pageSize(req.getPageSize())
                                .search(req.getSearch())
                                .build();

                return service.getActive(businessReq)
                                .map(resp -> ApiResponsePaginationProductDeleteAt.newBuilder()
                                                .setStatus(resp.status() != null ? resp.status() : "")
                                                .setMessage(resp.message() != null ? resp.message() : "")
                                                .addAllData(resp.data().stream()
                                                                .map(ProtoConverter::fromProductResponseToDeleteAt)
                                                                .toList())
                                                .setPagination(toMeta(resp.pagination()))
                                                .build());
        }

        @Override
        public Future<ApiResponsePaginationProductDeleteAt> findByTrashed(
                        pb.product.ProductQuery.FindAllProductRequest req) {
                io.example.product.model.FindAllProductRequest businessReq = io.example.product.model.FindAllProductRequest
                                .builder()
                                .page(req.getPage())
                                .pageSize(req.getPageSize())
                                .search(req.getSearch())
                                .build();

                return service.getTrashed(businessReq)
                                .map(resp -> ApiResponsePaginationProductDeleteAt.newBuilder()
                                                .setStatus(resp.status() != null ? resp.status() : "")
                                                .setMessage(resp.message() != null ? resp.message() : "")
                                                .addAllData(resp.data().stream()
                                                                .map(ProtoConverter::fromProductResponseDeleteAt)
                                                                .toList())
                                                .setPagination(toMeta(resp.pagination()))
                                                .build());
        }
}
