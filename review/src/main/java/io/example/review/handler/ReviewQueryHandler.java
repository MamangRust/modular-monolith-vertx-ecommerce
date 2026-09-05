package io.example.review.handler;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.review.domain.requests.FindAllReview;
import io.example.review.domain.requests.FindAllReviewByMerchant;
import io.example.review.domain.requests.FindAllReviewByProduct;
import io.example.review.service.ReviewQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.review.ReviewCommon.ApiResponsePaginationReview;
import pb.review.ReviewCommon.ApiResponsePaginationReviewDeleteAt;
import pb.review.ReviewCommon.ApiResponsePaginationReviewDetail;
import pb.review.ReviewCommon.ApiResponseReview;
import pb.review.ReviewQuery.FindAllReviewMerchantRequest;
import pb.review.ReviewQuery.FindAllReviewProductRequest;
import pb.review.ReviewQuery.FindAllReviewRequest;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class ReviewQueryHandler implements pb.review.VertxReviewQueryServiceGrpcServer.ReviewQueryServiceApi {
        private final ReviewQueryService service;

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
        public Future<ApiResponsePaginationReview> findAll(FindAllReviewRequest req) {
                FindAllReview reqDto = FindAllReview.builder().page(req.getPage()).pageSize(req.getPageSize())
                                .search(req.getSearch()).build();
                return service.getAllReviews(reqDto)
                                .map(res -> ApiResponsePaginationReview.newBuilder()
                                                .setStatus("success").setMessage("OK")
                                                .addAllData(res.getData().stream().map(ProtoConverter::toReviewResponse)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), reqDto.getPage(),
                                                                reqDto.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponsePaginationReviewDetail> findByProduct(FindAllReviewProductRequest req) {
                FindAllReviewByProduct reqDto = FindAllReviewByProduct.builder().productId((long) req.getProductId())
                                .page(req.getPage()).pageSize(req.getPageSize()).build();
                return service.getReviewByProduct(reqDto)
                                .map(res -> ApiResponsePaginationReviewDetail.newBuilder()
                                                .setStatus("success").setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::toReviewsDetailResponse).toList())
                                                .setPagination(toMeta(res.getTotalRecords(), reqDto.getPage(),
                                                                reqDto.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponsePaginationReviewDetail> findByMerchant(FindAllReviewMerchantRequest req) {
                FindAllReviewByMerchant reqDto = FindAllReviewByMerchant.builder()
                                .merchantId((long) req.getMerchantId())
                                .page(req.getPage()).pageSize(req.getPageSize()).build();
                return service.getReviewByMerchant(reqDto)
                                .map(res -> ApiResponsePaginationReviewDetail.newBuilder()
                                                .setStatus("success").setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::toReviewsDetailResponse).toList())
                                                .setPagination(toMeta(res.getTotalRecords(), reqDto.getPage(),
                                                                reqDto.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponsePaginationReviewDeleteAt> findByActive(FindAllReviewRequest req) {
                FindAllReview reqDto = FindAllReview.builder().page(req.getPage()).pageSize(req.getPageSize())
                                .search(req.getSearch()).build();
                return service.getActiveReviews(reqDto)
                                .map(res -> ApiResponsePaginationReviewDeleteAt.newBuilder()
                                                .setStatus("success").setMessage("OK")
                                                .addAllData(res.getData().stream().map(ProtoConverter::toReviewDeleteAt)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), reqDto.getPage(),
                                                                reqDto.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponsePaginationReviewDeleteAt> findByTrashed(FindAllReviewRequest req) {
                FindAllReview reqDto = FindAllReview.builder().page(req.getPage()).pageSize(req.getPageSize())
                                .search(req.getSearch()).build();
                return service.getTrashedReviews(reqDto)
                                .map(res -> ApiResponsePaginationReviewDeleteAt.newBuilder()
                                                .setStatus("success").setMessage("OK")
                                                .addAllData(res.getData().stream().map(ProtoConverter::toReviewDeleteAt)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), reqDto.getPage(),
                                                                reqDto.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseReview> findById(pb.review.ReviewCommon.FindByIdReviewRequest req) {
                return service.getReviewById((long) req.getId())
                                .map(data -> ApiResponseReview.newBuilder()
                                                .setStatus("success").setMessage("OK")
                                                .setData(ProtoConverter.toReviewResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

  @Override
  public pb.review.VertxReviewQueryServiceGrpcServer.ReviewQueryServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.review.VertxReviewQueryServiceGrpcServer.FindAll, this::findAll);
    GrpcServerBinder.bind(server, pb.review.VertxReviewQueryServiceGrpcServer.FindByProduct, this::findByProduct);
    GrpcServerBinder.bind(server, pb.review.VertxReviewQueryServiceGrpcServer.FindByMerchant, this::findByMerchant);
    GrpcServerBinder.bind(server, pb.review.VertxReviewQueryServiceGrpcServer.FindByActive, this::findByActive);
    GrpcServerBinder.bind(server, pb.review.VertxReviewQueryServiceGrpcServer.FindByTrashed, this::findByTrashed);
    GrpcServerBinder.bind(server, pb.review.VertxReviewQueryServiceGrpcServer.FindById, this::findById);
    return this;
  }
}