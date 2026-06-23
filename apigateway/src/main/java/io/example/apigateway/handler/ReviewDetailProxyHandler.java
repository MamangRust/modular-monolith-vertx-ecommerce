package io.example.apigateway.handler;

import static io.example.apigateway.utils.GrpcGatewayUtils.sendResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.example.apigateway.utils.GrpcGatewayUtils;
import io.example.common.exception.api.BadRequestException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pb.review.ReviewQuery;
import pb.review_detail.ReviewDetailCommand;
import pb.review_detail.ReviewDetailCommon;
import pb.review_detail.VertxReviewDetailCommandServiceGrpcClient;
import pb.review_detail.VertxReviewDetailQueryServiceGrpcClient;

@Slf4j
@RequiredArgsConstructor
public class ReviewDetailProxyHandler {
        private final VertxReviewDetailQueryServiceGrpcClient queryClient;
        private final VertxReviewDetailCommandServiceGrpcClient commandClient;

        private static final String UPLOAD_DIRECTORY = "uploads/review-details/";

        public void findAll(RoutingContext ctx) {
                var req = ReviewQuery.FindAllReviewRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findAll(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findActive(RoutingContext ctx) {
                var req = ReviewQuery.FindAllReviewRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findByActive(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findTrashed(RoutingContext ctx) {
                var req = ReviewQuery.FindAllReviewRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findByTrashed(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findById(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = ReviewDetailCommon.FindByIdReviewDetailRequest.newBuilder().setId(id).build();

                queryClient.findById(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void create(RoutingContext ctx) {
                try {
                        int reviewId = GrpcGatewayUtils.getFormInteger(ctx, "review_id", 0);
                        String type = GrpcGatewayUtils.getFormString(ctx, "type", "");
                        String url = GrpcGatewayUtils.getFormString(ctx, "url", "");
                        String caption = GrpcGatewayUtils.getFormString(ctx, "caption", "");

                        FileUpload imageFile = GrpcGatewayUtils.getFileUpload(ctx, "imageFile");
                        if (imageFile != null) {
                                try {
                                        url = storeUploadedFile(imageFile);
                                } catch (IOException e) {
                                        ctx.fail(new BadRequestException(
                                                        "Failed to process uploaded file: " + e.getMessage()));
                                        return;
                                }
                        }

                        var req = ReviewDetailCommand.CreateReviewDetailRequest.newBuilder()
                                        .setReviewId(reviewId)
                                        .setType(type)
                                        .setUrl(url)
                                        .setCaption(caption)
                                        .build();

                        commandClient.create(req)
                                        .onSuccess(resp -> sendResponse(ctx, resp, 201))
                                        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));

                } catch (BadRequestException e) {
                        ctx.fail(e);
                }
        }

        public void update(RoutingContext ctx) {
                try {
                        int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                        String type = GrpcGatewayUtils.getFormString(ctx, "type", "");
                        String existingUrl = GrpcGatewayUtils.getFormString(ctx, "url", "");
                        String caption = GrpcGatewayUtils.getFormString(ctx, "caption", "");

                        String url = existingUrl;

                        FileUpload imageFile = GrpcGatewayUtils.getFileUpload(ctx, "imageFile");
                        if (imageFile != null) {
                                try {
                                        url = storeUploadedFile(imageFile);
                                } catch (IOException e) {
                                        ctx.fail(new BadRequestException(
                                                        "Failed to process uploaded file: " + e.getMessage()));
                                        return;
                                }
                        }

                        var req = ReviewDetailCommand.UpdateReviewDetailRequest.newBuilder()
                                        .setReviewDetailId(id)
                                        .setType(type)
                                        .setUrl(url)
                                        .setCaption(caption)
                                        .build();

                        commandClient.update(req)
                                        .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));

                } catch (BadRequestException e) {
                        ctx.fail(e);
                }
        }

        public void uploadImage(RoutingContext ctx) {
                FileUpload imageFile = GrpcGatewayUtils.getFileUpload(ctx, "imageFile");

                if (imageFile == null) {
                        ctx.fail(new BadRequestException("imageFile is required"));
                        return;
                }

                try {
                        String publicUrl = storeUploadedFile(imageFile);

                        ctx.response()
                                        .setStatusCode(200)
                                        .putHeader("Content-Type", "application/json")
                                        .end(new JsonObject()
                                                        .put("status", 200)
                                                        .put("message", "Upload successful")
                                                        .put("image_url", publicUrl)
                                                        .encode());

                } catch (IOException e) {
                        log.error("Failed to store uploaded file: {}", e.getMessage(), e);
                        ctx.fail(new BadRequestException("Failed to store uploaded file: " + e.getMessage()));
                }
        }

        public void trash(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = ReviewDetailCommon.FindByIdReviewDetailRequest.newBuilder().setId(id).build();

                commandClient.trashedReviewDetail(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restore(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = ReviewDetailCommon.FindByIdReviewDetailRequest.newBuilder().setId(id).build();

                commandClient.restoreReviewDetail(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deletePermanent(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = ReviewDetailCommon.FindByIdReviewDetailRequest.newBuilder().setId(id).build();

                commandClient.deleteReviewDetailPermanent(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restoreAll(RoutingContext ctx) {
                commandClient.restoreAllReviewDetail(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deleteAll(RoutingContext ctx) {
                commandClient.deleteAllReviewDetailPermanent(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        private String storeUploadedFile(FileUpload fileUpload) throws IOException {
                Files.createDirectories(Paths.get(UPLOAD_DIRECTORY));

                String fileName = System.currentTimeMillis() + "_" + fileUpload.fileName();
                Path source = Paths.get(fileUpload.uploadedFileName());
                Path target = Paths.get(UPLOAD_DIRECTORY + fileName);

                Files.move(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return "/downloads/" + fileName;
        }
}