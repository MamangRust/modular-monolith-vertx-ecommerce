package io.example.review.handler;

import io.example.review.model.FindAllReview;
import io.example.review.model.FindAllReviewByProduct;
import io.example.review.model.FindAllReviewByMerchant;
import io.example.review.service.ReviewQueryService;
import io.vertx.core.Future;

import pb.review.ReviewCommon.ApiResponsePaginationReview;
import pb.review.ReviewCommon.ApiResponsePaginationReviewDeleteAt;
import pb.review.ReviewCommon.ApiResponsePaginationReviewDetail;
import pb.review.ReviewCommon.ApiResponseReview;
import pb.review.ReviewCommon.FindByIdReviewRequest;
import pb.review.ReviewQuery.FindAllReviewRequest;

import pb.review.ReviewQuery.FindAllReviewProductRequest;
import pb.review.ReviewQuery.FindAllReviewMerchantRequest;

public class ReviewQueryHandler implements pb.review.VertxReviewQueryServiceGrpcServer.ReviewQueryServiceApi {
    private final ReviewQueryService service;

    public ReviewQueryHandler(ReviewQueryService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponsePaginationReview> findAll(FindAllReviewRequest req) {
        FindAllReview reqDto = FindAllReview.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getAllReviews(reqDto)
                .map(res -> {
                    ApiResponsePaginationReview.Builder builder = ApiResponsePaginationReview.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream().map(ProtoConverter::toReviewResponse).toList());
                    }

                    if (res.pagination() != null) {
                        builder.setPagination(pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(res.pagination().currentPage())
                                .setPageSize(res.pagination().pageSize())
                                .setTotalPages(res.pagination().totalPages())
                                .setTotalRecords(res.pagination().totalRecords())
                                .build());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponsePaginationReviewDetail> findByProduct(FindAllReviewProductRequest req) {
        FindAllReviewByProduct reqDto = FindAllReviewByProduct.builder()
                .productId((long) req.getProductId())
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getReviewByProduct(reqDto)
                .map(res -> {
                    ApiResponsePaginationReviewDetail.Builder builder = ApiResponsePaginationReviewDetail.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream().map(ProtoConverter::toReviewsDetailResponse).toList());
                    }

                    if (res.pagination() != null) {
                        builder.setPagination(pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(res.pagination().currentPage())
                                .setPageSize(res.pagination().pageSize())
                                .setTotalPages(res.pagination().totalPages())
                                .setTotalRecords(res.pagination().totalRecords())
                                .build());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponsePaginationReviewDetail> findByMerchant(FindAllReviewMerchantRequest req) {
        FindAllReviewByMerchant reqDto = FindAllReviewByMerchant.builder()
                .merchantId((long) req.getMerchantId())
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getReviewByMerchant(reqDto)
                .map(res -> {
                    ApiResponsePaginationReviewDetail.Builder builder = ApiResponsePaginationReviewDetail.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream().map(ProtoConverter::toReviewsDetailResponse).toList());
                    }

                    if (res.pagination() != null) {
                        builder.setPagination(pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(res.pagination().currentPage())
                                .setPageSize(res.pagination().pageSize())
                                .setTotalPages(res.pagination().totalPages())
                                .setTotalRecords(res.pagination().totalRecords())
                                .build());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponsePaginationReviewDeleteAt> findByActive(FindAllReviewRequest req) {
        FindAllReview reqDto = FindAllReview.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getActiveReviews(reqDto)
                .map(res -> {
                    ApiResponsePaginationReviewDeleteAt.Builder builder = ApiResponsePaginationReviewDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream().map(ProtoConverter::toReviewDeleteAt).toList());
                    }

                    if (res.pagination() != null) {
                        builder.setPagination(pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(res.pagination().currentPage())
                                .setPageSize(res.pagination().pageSize())
                                .setTotalPages(res.pagination().totalPages())
                                .setTotalRecords(res.pagination().totalRecords())
                                .build());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponsePaginationReviewDeleteAt> findByTrashed(FindAllReviewRequest req) {
        FindAllReview reqDto = FindAllReview.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getTrashedReviews(reqDto)
                .map(res -> {
                    ApiResponsePaginationReviewDeleteAt.Builder builder = ApiResponsePaginationReviewDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream().map(ProtoConverter::toReviewDeleteAt).toList());
                    }

                    if (res.pagination() != null) {
                        builder.setPagination(pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(res.pagination().currentPage())
                                .setPageSize(res.pagination().pageSize())
                                .setTotalPages(res.pagination().totalPages())
                                .setTotalRecords(res.pagination().totalRecords())
                                .build());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseReview> findById(pb.review.ReviewCommon.FindByIdReviewRequest req) {
        return service.getReviewById((long) req.getId())
                .map(res -> {
                    ApiResponseReview.Builder builder = ApiResponseReview.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toReviewResponse(res.data()));
                    }

                    return builder.build();
                });
    }
}

