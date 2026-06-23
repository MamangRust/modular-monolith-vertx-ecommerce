package io.example.review_detail.handler;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.review_detail.domain.requests.FindAllReview;
import io.example.review_detail.service.ReviewDetailQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.review_detail.ReviewDetailCommon.ApiResponsePaginationReviewDetails;
import pb.review_detail.ReviewDetailCommon.ApiResponsePaginationReviewDetailsDeleteAt;
import pb.review_detail.ReviewDetailCommon.ApiResponseReviewDetail;
import pb.review_detail.ReviewDetailCommon.FindByIdReviewDetailRequest;

@RequiredArgsConstructor
public class ReviewDetailQueryHandler
                implements pb.review_detail.VertxReviewDetailQueryServiceGrpcServer.ReviewDetailQueryServiceApi {
        private final ReviewDetailQueryService service;

        private FindAllReview toDomainReq(pb.review.ReviewQuery.FindAllReviewRequest req) {
                return FindAllReview.builder()
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
        public Future<ApiResponsePaginationReviewDetails> findAll(pb.review.ReviewQuery.FindAllReviewRequest req) {
                FindAllReview domainReq = toDomainReq(req);
                return service.getAllReviewDetails(domainReq)
                                .map(res -> ApiResponsePaginationReviewDetails.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponse)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseReviewDetail> findById(FindByIdReviewDetailRequest req) {
                return service.getReviewDetailById((long) req.getId())
                                .map(data -> ApiResponseReviewDetail.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponsePaginationReviewDetailsDeleteAt> findByActive(
                        pb.review.ReviewQuery.FindAllReviewRequest req) {
                FindAllReview domainReq = toDomainReq(req);
                return service.getActiveReviewDetails(domainReq)
                                .map(res -> ApiResponsePaginationReviewDetailsDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::toProtoResponseDeleteAt).toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponsePaginationReviewDetailsDeleteAt> findByTrashed(
                        pb.review.ReviewQuery.FindAllReviewRequest req) {
                FindAllReview domainReq = toDomainReq(req);
                return service.getTrashedReviewDetails(domainReq)
                                .map(res -> ApiResponsePaginationReviewDetailsDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::toProtoResponseDeleteAt).toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }
}