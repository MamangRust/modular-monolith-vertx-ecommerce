package io.example.review_detail.handler;

import io.example.review_detail.model.FindAllReview;
import io.example.review_detail.service.ReviewDetailQueryService;
import io.vertx.core.Future;
import pb.review_detail.ReviewDetailCommon.ApiResponsePaginationReviewDetails;
import pb.review_detail.ReviewDetailCommon.ApiResponsePaginationReviewDetailsDeleteAt;
import pb.review_detail.ReviewDetailCommon.ApiResponseReviewDetail;
import pb.review_detail.ReviewDetailCommon.FindByIdReviewDetailRequest;

public class ReviewDetailQueryHandler implements pb.review_detail.VertxReviewDetailQueryServiceGrpcServer.ReviewDetailQueryServiceApi {
    private final ReviewDetailQueryService service;

    public ReviewDetailQueryHandler(ReviewDetailQueryService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponsePaginationReviewDetails> findAll(pb.review.ReviewQuery.FindAllReviewRequest req) {
        FindAllReview reqDto = FindAllReview.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getAllReviewDetails(reqDto)
                .map(res -> {
                    ApiResponsePaginationReviewDetails.Builder builder = ApiResponsePaginationReviewDetails.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream().map(ProtoConverter::toProtoResponse).toList());
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
    public Future<ApiResponseReviewDetail> findById(FindByIdReviewDetailRequest req) {
        return service.getReviewDetailById(req.getId())
                .map(res -> {
                    ApiResponseReviewDetail.Builder builder = ApiResponseReviewDetail.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponse(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponsePaginationReviewDetailsDeleteAt> findByActive(pb.review.ReviewQuery.FindAllReviewRequest req) {
        FindAllReview reqDto = FindAllReview.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getActiveReviewDetails(reqDto)
                .map(res -> {
                    ApiResponsePaginationReviewDetailsDeleteAt.Builder builder = ApiResponsePaginationReviewDetailsDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList());
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
    public Future<ApiResponsePaginationReviewDetailsDeleteAt> findByTrashed(pb.review.ReviewQuery.FindAllReviewRequest req) {
        FindAllReview reqDto = FindAllReview.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getTrashedReviewDetails(reqDto)
                .map(res -> {
                    ApiResponsePaginationReviewDetailsDeleteAt.Builder builder = ApiResponsePaginationReviewDetailsDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList());
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
}
