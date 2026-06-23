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
import pb.slider.SliderCommand;
import pb.slider.SliderCommon;
import pb.slider.SliderQuery;
import pb.slider.VertxSliderCommandServiceGrpcClient;
import pb.slider.VertxSliderQueryServiceGrpcClient;

@Slf4j
@RequiredArgsConstructor
public class SliderProxyHandler {
        private final VertxSliderQueryServiceGrpcClient queryClient;
        private final VertxSliderCommandServiceGrpcClient commandClient;

        private static final String UPLOAD_DIRECTORY = "uploads/sliders/";

        public void findAll(RoutingContext ctx) {
                var req = SliderQuery.FindAllSliderRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findAll(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findActive(RoutingContext ctx) {
                var req = SliderQuery.FindAllSliderRequest.newBuilder()
                                .setSearch(GrpcGatewayUtils.getQueryString(ctx, "search", ""))
                                .setPage(GrpcGatewayUtils.getQueryInt(ctx, "page", 1))
                                .setPageSize(GrpcGatewayUtils.getQueryInt(ctx, "pageSize", 10))
                                .build();

                queryClient.findByActive(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void findTrashed(RoutingContext ctx) {
                var req = SliderQuery.FindAllSliderRequest.newBuilder()
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
                var req = SliderCommon.FindByIdSliderRequest.newBuilder().setId(id).build();

                queryClient.findById(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void create(RoutingContext ctx) {
                try {
                        String name = GrpcGatewayUtils.getFormString(ctx, "name", "");
                        String imageUrl = "";

                        FileUpload imageFile = GrpcGatewayUtils.getFileUpload(ctx, "imageFile");
                        if (imageFile != null) {
                                try {
                                        imageUrl = storeUploadedFile(imageFile);
                                } catch (IOException e) {
                                        ctx.fail(new BadRequestException(
                                                        "Failed to process uploaded file: " + e.getMessage()));
                                        return;
                                }
                        }

                        var req = SliderCommand.CreateSliderRequest.newBuilder()
                                        .setName(name)
                                        .setImage(imageUrl)
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
                        String name = GrpcGatewayUtils.getFormString(ctx, "name", "");
                        String existingImageUrl = GrpcGatewayUtils.getFormString(ctx, "image", "");
                        String imageUrl = existingImageUrl;

                        FileUpload imageFile = GrpcGatewayUtils.getFileUpload(ctx, "imageFile");
                        if (imageFile != null) {
                                try {
                                        imageUrl = storeUploadedFile(imageFile);
                                } catch (IOException e) {
                                        ctx.fail(new BadRequestException(
                                                        "Failed to process uploaded file: " + e.getMessage()));
                                        return;
                                }
                        }

                        var req = SliderCommand.UpdateSliderRequest.newBuilder()
                                        .setId(id)
                                        .setName(name)
                                        .setImage(imageUrl)
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
                var req = SliderCommon.FindByIdSliderRequest.newBuilder().setId(id).build();

                commandClient.trashedSlider(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restore(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = SliderCommon.FindByIdSliderRequest.newBuilder().setId(id).build();

                commandClient.restoreSlider(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deletePermanent(RoutingContext ctx) {
                int id = GrpcGatewayUtils.getSafePathInt(ctx, "id");
                var req = SliderCommon.FindByIdSliderRequest.newBuilder().setId(id).build();

                commandClient.deleteSliderPermanent(req)
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void restoreAll(RoutingContext ctx) {
                commandClient.restoreAllSlider(com.google.protobuf.Empty.getDefaultInstance())
                                .onSuccess(resp -> sendResponse(ctx, resp, 200))
                                .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
        }

        public void deleteAll(RoutingContext ctx) {
                commandClient.deleteAllSliderPermanent(com.google.protobuf.Empty.getDefaultInstance())
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