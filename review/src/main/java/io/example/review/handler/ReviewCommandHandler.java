package io.example.review.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.review.domain.requests.CreateReviewRequest;
import io.example.review.domain.requests.UpdateReviewRequest;
import io.example.review.service.ReviewCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.review.ReviewCommon.ApiResponseReview;
import pb.review.ReviewCommon.ApiResponseReviewAll;
import pb.review.ReviewCommon.ApiResponseReviewDelete;
import pb.review.ReviewCommon.ApiResponseReviewDeleteAt;
import pb.review.ReviewCommon.FindByIdReviewRequest;

@RequiredArgsConstructor
public class ReviewCommandHandler implements pb.review.VertxReviewCommandServiceGrpcServer.ReviewCommandServiceApi {
    private final ReviewCommandService service;

    @Override
    public Future<ApiResponseReview> create(pb.review.ReviewCommand.CreateReviewRequest req) {
        CreateReviewRequest reqDto = CreateReviewRequest.builder()
                .userId(req.getUserId())
                .productId((long) req.getProductId())
                .name(req.getName())
                .rating(req.getRating())
                .comment(req.getComment())
                .build();

        return service.createReview(reqDto)
                .map(data -> ApiResponseReview.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toReviewResponse(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseReview> update(pb.review.ReviewCommand.UpdateReviewRequest req) {
        UpdateReviewRequest reqDto = UpdateReviewRequest.builder()
                .reviewId((long) req.getReviewId())
                .name(req.getName())
                .rating(req.getRating())
                .comment(req.getComment())
                .build();

        return service.updateReview(reqDto)
                .map(data -> ApiResponseReview.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toReviewResponse(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseReviewDeleteAt> trashedReview(FindByIdReviewRequest req) {
        return service.trashReview((long) req.getId())
                .map(data -> ApiResponseReviewDeleteAt.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toReviewDeleteAt(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseReviewDeleteAt> restoreReview(FindByIdReviewRequest req) {
        return service.restoreReview((long) req.getId())
                .map(data -> ApiResponseReviewDeleteAt.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toReviewDeleteAt(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseReviewDelete> deleteReviewPermanent(FindByIdReviewRequest req) {
        return service.deleteReviewPermanently((long) req.getId())
                .map(v -> ApiResponseReviewDelete.newBuilder()
                        .setStatus("success")
                        .setMessage("Review deleted permanently")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseReviewAll> restoreAllReview(Empty req) {
        return service.restoreAllReviews()
                .map(v -> ApiResponseReviewAll.newBuilder()
                        .setStatus("success")
                        .setMessage("All reviews restored successfully")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseReviewAll> deleteAllReviewPermanent(Empty req) {
        return service.deleteAllPermanentReviews()
                .map(v -> ApiResponseReviewAll.newBuilder()
                        .setStatus("success")
                        .setMessage("All reviews permanently deleted")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }
}