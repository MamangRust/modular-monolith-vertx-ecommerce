package io.example.review.handler;

import com.google.protobuf.Empty;
import io.example.review.model.CreateReviewRequest;
import io.example.review.model.UpdateReviewRequest;
import io.example.review.service.ReviewCommandService;
import io.vertx.core.Future;

import pb.review.ReviewCommon.ApiResponseReview;
import pb.review.ReviewCommon.ApiResponseReviewDeleteAt;
import pb.review.ReviewCommon.ApiResponseReviewDelete;
import pb.review.ReviewCommon.ApiResponseReviewAll;
import pb.review.ReviewCommon.FindByIdReviewRequest;

public class ReviewCommandHandler implements pb.review.VertxReviewCommandServiceGrpcServer.ReviewCommandServiceApi {
    private final ReviewCommandService service;

    public ReviewCommandHandler(ReviewCommandService service) {
        this.service = service;
    }

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

    @Override
    public Future<ApiResponseReview> update(pb.review.ReviewCommand.UpdateReviewRequest req) {
        UpdateReviewRequest reqDto = UpdateReviewRequest.builder()
                .reviewId((long) req.getReviewId())
                .name(req.getName())
                .rating(req.getRating())
                .comment(req.getComment())
                .build();

        return service.updateReview(reqDto)
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

    @Override
    public Future<ApiResponseReviewDeleteAt> trashedReview(FindByIdReviewRequest req) {
        return service.trashReview((long) req.getId())
                .map(res -> {
                    ApiResponseReviewDeleteAt.Builder builder = ApiResponseReviewDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toReviewDeleteAt(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseReviewDeleteAt> restoreReview(FindByIdReviewRequest req) {
        return service.restoreReview((long) req.getId())
                .map(res -> {
                    ApiResponseReviewDeleteAt.Builder builder = ApiResponseReviewDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toReviewDeleteAt(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseReviewDelete> deleteReviewPermanent(FindByIdReviewRequest req) {
        return service.deleteReviewPermanently((long) req.getId())
                .map(res -> ApiResponseReviewDelete.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }

    @Override
    public Future<ApiResponseReviewAll> restoreAllReview(Empty req) {
        return service.restoreAllReviews()
                .map(res -> ApiResponseReviewAll.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }

    @Override
    public Future<ApiResponseReviewAll> deleteAllReviewPermanent(Empty req) {
        return service.deleteAllPermanentReviews()
                .map(res -> ApiResponseReviewAll.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }
}
