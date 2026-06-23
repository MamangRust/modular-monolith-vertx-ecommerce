package io.example.category.handler;

import io.example.category.domain.requests.FindAllCategoriesRequest;
import io.example.category.service.CategoryQueryService;
import io.example.common.grpc.GrpcExceptionMapper;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.category.CategoryCommon;
import pb.category.CategoryQuery;

@RequiredArgsConstructor
public class CategoryQueryHandler implements pb.category.VertxCategoryQueryServiceGrpcServer.CategoryQueryServiceApi {

        private final CategoryQueryService queryService;

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
        public Future<CategoryCommon.ApiResponsePaginationCategory> findAll(CategoryQuery.FindAllCategoryRequest req) {
                FindAllCategoriesRequest domainReq = FindAllCategoriesRequest.builder()
                                .page(req.getPage())
                                .pageSize(req.getPageSize())
                                .search(req.getSearch())
                                .build();

                return queryService.getAll(domainReq)
                                .map(res -> CategoryCommon.ApiResponsePaginationCategory.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::toCategoryResponse)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
        }

        @Override
        public Future<CategoryCommon.ApiResponseCategory> findById(CategoryCommon.FindByIdCategoryRequest req) {
                return queryService.getById((long) req.getId())
                                .map(res -> CategoryCommon.ApiResponseCategory.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toCategoryResponse(res))
                                                .build())
                                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
        }

        @Override
        public Future<CategoryCommon.ApiResponsePaginationCategoryDeleteAt> findByActive(
                        CategoryQuery.FindAllCategoryRequest req) {
                FindAllCategoriesRequest domainReq = FindAllCategoriesRequest.builder()
                                .page(req.getPage())
                                .pageSize(req.getPageSize())
                                .search(req.getSearch())
                                .build();

                return queryService.getActive(domainReq)
                                .map(res -> CategoryCommon.ApiResponsePaginationCategoryDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::toCategoryResponseDeleteAt)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
        }

        @Override
        public Future<CategoryCommon.ApiResponsePaginationCategoryDeleteAt> findByTrashed(
                        CategoryQuery.FindAllCategoryRequest req) {
                FindAllCategoriesRequest domainReq = FindAllCategoriesRequest.builder()
                                .page(req.getPage())
                                .pageSize(req.getPageSize())
                                .search(req.getSearch())
                                .build();

                return queryService.getTrashed(domainReq)
                                .map(res -> CategoryCommon.ApiResponsePaginationCategoryDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::toCategoryResponseDeleteAt)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
        }
}