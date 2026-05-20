package io.example.review_detail.handler;

import com.google.protobuf.Empty;
import io.example.review_detail.model.CreateReviewDetailRequest;
import io.example.review_detail.model.UpdateReviewDetailRequest;
import io.example.review_detail.service.ReviewDetailCommandService;
import io.vertx.core.Future;
import pb.review.ReviewCommon.ApiResponseReviewAll;
import pb.review.ReviewCommon.ApiResponseReviewDelete;
import pb.review_detail.ReviewDetailCommon.ApiResponseReviewDetail;
import pb.review_detail.ReviewDetailCommon.ApiResponseReviewDetailDeleteAt;
import pb.review_detail.ReviewDetailCommon.FindByIdReviewDetailRequest;

public class ReviewDetailCommandHandler implements pb.review_detail.VertxReviewDetailCommandServiceGrpcServer.ReviewDetailCommandServiceApi {
    private final ReviewDetailCommandService service;

    public ReviewDetailCommandHandler(ReviewDetailCommandService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponseReviewDetail> create(pb.review_detail.ReviewDetailCommand.CreateReviewDetailRequest req) {
        CreateReviewDetailRequest reqDto = CreateReviewDetailRequest.builder()
                .reviewId((long) req.getReviewId())
                .type(req.getType())
                .file(req.getUrl())
                .caption(req.getCaption())
                .build();

        return service.createReviewDetail(reqDto)
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
    public Future<ApiResponseReviewDetail> update(pb.review_detail.ReviewDetailCommand.UpdateReviewDetailRequest req) {
        UpdateReviewDetailRequest reqDto = UpdateReviewDetailRequest.builder()
                .reviewDetailId((long) req.getReviewDetailId())
                .type(req.getType())
                .file(req.getUrl())
                .caption(req.getCaption())
                .build();

        return service.updateReviewDetail(reqDto)
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
    public Future<ApiResponseReviewDetailDeleteAt> trashedReviewDetail(FindByIdReviewDetailRequest req) {
        return service.trashReviewDetail(req.getId())
                .map(res -> {
                    ApiResponseReviewDetailDeleteAt.Builder builder = ApiResponseReviewDetailDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponseDeleteAt(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseReviewDetailDeleteAt> restoreReviewDetail(FindByIdReviewDetailRequest req) {
        return service.restoreReviewDetail(req.getId())
                .map(res -> {
                    ApiResponseReviewDetailDeleteAt.Builder builder = ApiResponseReviewDetailDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponseDeleteAt(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseReviewDelete> deleteReviewDetailPermanent(FindByIdReviewDetailRequest req) {
        return service.deleteReviewDetailPermanently(req.getId())
                .map(res -> ApiResponseReviewDelete.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }

    @Override
    public Future<ApiResponseReviewAll> restoreAllReviewDetail(Empty req) {
        return service.restoreAllReviewDetails()
                .map(res -> ApiResponseReviewAll.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }

    @Override
    public Future<ApiResponseReviewAll> deleteAllReviewDetailPermanent(Empty req) {
        return service.deleteAllPermanentReviewDetails()
                .map(res -> ApiResponseReviewAll.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }
}
