package io.example.product.handler;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.product.domain.requests.FindAllProductRequest;
import io.example.product.service.ProductQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.product.ProductCommon.ApiResponsePaginationProduct;
import pb.product.ProductCommon.ApiResponsePaginationProductDeleteAt;
import pb.product.ProductCommon.ApiResponseProduct;
import pb.product.ProductCommon.FindByIdProductRequest;
import pb.product.ProductQuery.FindAllProductCategoryRequest;
import pb.product.ProductQuery.FindAllProductMerchantRequest;
import pb.product.VertxProductQueryServiceGrpcServer.ProductQueryServiceApi;

@RequiredArgsConstructor
public class ProductQueryHandler implements ProductQueryServiceApi {
        private final ProductQueryService service;

        private FindAllProductRequest toDomainReq(pb.product.ProductQuery.FindAllProductRequest req) {
                return FindAllProductRequest.builder()
                                .page(req.getPage())
                                .pageSize(req.getPageSize())
                                .search(req.getSearch())
                                .build();
        }

        private pb.Api.PaginationMeta toMeta(int totalRecords, int page, int pageSize) {
                int currentPage = page > 0 ? page : 1;
                int size = pageSize > 0 ? pageSize : 10;
                int totalPages = size > 0 ? (int) Math.ceil((double) totalRecords / size) : 0;
                return pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(currentPage)
                                .setPageSize(size)
                                .setTotalPages(totalPages)
                                .setTotalRecords(totalRecords)
                                .build();
        }

        @Override
        public Future<ApiResponsePaginationProduct> findAll(pb.product.ProductQuery.FindAllProductRequest req) {
                FindAllProductRequest domainReq = toDomainReq(req);
                return service.getAll(domainReq)
                                .map(res -> ApiResponsePaginationProduct.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::fromProductResponse).toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponsePaginationProduct> findByMerchant(FindAllProductMerchantRequest req) {
                var reqDomain = io.example.product.domain.requests.FindAllProductMerchantRequest.builder()
                                .merchantId((long) req.getMerchantId())
                                .search(req.getSearch())
                                .categoryId((long) req.getCategoryId())
                                .minPrice(req.getMinPrice())
                                .maxPrice(req.getMaxPrice())
                                .page(req.getPage() > 0 ? req.getPage() : 1)
                                .pageSize(req.getPageSize() > 0 ? req.getPageSize() : 10)
                                .build();

                return service.getByMerchant(reqDomain)
                                .map(res -> ApiResponsePaginationProduct.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::fromProductResponse).toList())
                                                .setPagination(toMeta(res.getTotalRecords(), req.getPage(),
                                                                req.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponsePaginationProduct> findByCategory(FindAllProductCategoryRequest req) {
                var reqDomain = io.example.product.domain.requests.FindAllProductCategoryRequest.builder()
                                .categoryName(req.getCategoryName())
                                .search(req.getSearch())
                                .minPrice(req.getMinPrice())
                                .maxPrice(req.getMaxPrice())
                                .page(req.getPage() > 0 ? req.getPage() : 1)
                                .pageSize(req.getPageSize() > 0 ? req.getPageSize() : 10)
                                .build();

                return service.getByCategoryName(reqDomain)
                                .map(res -> ApiResponsePaginationProduct.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::fromProductResponse).toList())
                                                .setPagination(toMeta(res.getTotalRecords(), req.getPage(),
                                                                req.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseProduct> findById(FindByIdProductRequest req) {
                return service.getById((long) req.getId())
                                .map(data -> ApiResponseProduct.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.fromProductResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponsePaginationProductDeleteAt> findByActive(
                        pb.product.ProductQuery.FindAllProductRequest req) {
                FindAllProductRequest domainReq = toDomainReq(req);
                return service.getActive(domainReq)
                                .map(res -> ApiResponsePaginationProductDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::fromProductResponseDeleteAt)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponsePaginationProductDeleteAt> findByTrashed(
                        pb.product.ProductQuery.FindAllProductRequest req) {
                FindAllProductRequest domainReq = toDomainReq(req);
                return service.getTrashed(domainReq)
                                .map(res -> ApiResponsePaginationProductDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::fromProductResponseDeleteAt)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }
}