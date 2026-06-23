package io.example.review_detail.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.review_detail.domain.requests.CreateReviewDetailRequest;
import io.example.review_detail.domain.requests.UpdateReviewDetailRequest;
import io.example.review_detail.service.ReviewDetailCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.review.ReviewCommon.ApiResponseReviewAll;
import pb.review.ReviewCommon.ApiResponseReviewDelete;
import pb.review_detail.ReviewDetailCommon.ApiResponseReviewDetail;
import pb.review_detail.ReviewDetailCommon.ApiResponseReviewDetailDeleteAt;
import pb.review_detail.ReviewDetailCommon.FindByIdReviewDetailRequest;

@RequiredArgsConstructor
public class ReviewDetailCommandHandler
                implements pb.review_detail.VertxReviewDetailCommandServiceGrpcServer.ReviewDetailCommandServiceApi {
        private final ReviewDetailCommandService service;

        @Override
        public Future<ApiResponseReviewDetail> create(
                        pb.review_detail.ReviewDetailCommand.CreateReviewDetailRequest req) {
                CreateReviewDetailRequest reqDto = CreateReviewDetailRequest.builder()
                                .reviewId((long) req.getReviewId())
                                .type(req.getType())
                                .file(req.getUrl())
                                .caption(req.getCaption())
                                .build();

                return service.createReviewDetail(reqDto)
                                .map(data -> ApiResponseReviewDetail.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseReviewDetail> update(
                        pb.review_detail.ReviewDetailCommand.UpdateReviewDetailRequest req) {
                UpdateReviewDetailRequest reqDto = UpdateReviewDetailRequest.builder()
                                .reviewDetailId((long) req.getReviewDetailId())
                                .type(req.getType())
                                .file(req.getUrl())
                                .caption(req.getCaption())
                                .build();

                return service.updateReviewDetail(reqDto)
                                .map(data -> ApiResponseReviewDetail.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseReviewDetailDeleteAt> trashedReviewDetail(FindByIdReviewDetailRequest req) {
                return service.trashReviewDetail((long) req.getId())
                                .map(data -> ApiResponseReviewDetailDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponseDeleteAt(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseReviewDetailDeleteAt> restoreReviewDetail(FindByIdReviewDetailRequest req) {
                return service.restoreReviewDetail((long) req.getId())
                                .map(data -> ApiResponseReviewDetailDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponseDeleteAt(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseReviewDelete> deleteReviewDetailPermanent(FindByIdReviewDetailRequest req) {
                return service.deleteReviewDetailPermanently((long) req.getId())
                                .map(v -> ApiResponseReviewDelete.newBuilder()
                                                .setStatus("success")
                                                .setMessage("Review detail deleted permanently")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseReviewAll> restoreAllReviewDetail(Empty req) {
                return service.restoreAllReviewDetails()
                                .map(v -> ApiResponseReviewAll.newBuilder()
                                                .setStatus("success")
                                                .setMessage("All review details restored successfully")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseReviewAll> deleteAllReviewDetailPermanent(Empty req) {
                return service.deleteAllPermanentReviewDetails()
                                .map(v -> ApiResponseReviewAll.newBuilder()
                                                .setStatus("success")
                                                .setMessage("All review details permanently deleted")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }
}