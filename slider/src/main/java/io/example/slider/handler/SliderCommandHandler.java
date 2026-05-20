package io.example.slider.handler;

import com.google.protobuf.Empty;
import io.example.slider.model.CreateSliderRequest;
import io.example.slider.model.UpdateSliderRequest;
import io.example.slider.service.SliderCommandService;
import io.vertx.core.Future;
import pb.slider.SliderCommon.ApiResponseSlider;
import pb.slider.SliderCommon.ApiResponseSliderAll;
import pb.slider.SliderCommon.ApiResponseSliderDelete;
import pb.slider.SliderCommon.ApiResponseSliderDeleteAt;
import pb.slider.SliderCommon.FindByIdSliderRequest;

public class SliderCommandHandler implements pb.slider.VertxSliderCommandServiceGrpcServer.SliderCommandServiceApi {
    private final SliderCommandService service;

    public SliderCommandHandler(SliderCommandService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponseSlider> create(pb.slider.SliderCommand.CreateSliderRequest req) {
        CreateSliderRequest reqDto = CreateSliderRequest.builder()
                .name(req.getName())
                .image(req.getImage())
                .build();

        return service.createSlider(reqDto)
                .map(res -> {
                    ApiResponseSlider.Builder builder = ApiResponseSlider.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponse(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseSlider> update(pb.slider.SliderCommand.UpdateSliderRequest req) {
        UpdateSliderRequest reqDto = UpdateSliderRequest.builder()
                .sliderId((long) req.getId())
                .name(req.getName())
                .image(req.getImage())
                .build();

        return service.updateSlider(reqDto)
                .map(res -> {
                    ApiResponseSlider.Builder builder = ApiResponseSlider.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponse(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseSliderDeleteAt> trashedSlider(FindByIdSliderRequest req) {
        return service.trashSlider((long) req.getId())
                .map(res -> {
                    ApiResponseSliderDeleteAt.Builder builder = ApiResponseSliderDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponseDeleteAt(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseSliderDeleteAt> restoreSlider(FindByIdSliderRequest req) {
        return service.restoreSlider((long) req.getId())
                .map(res -> {
                    ApiResponseSliderDeleteAt.Builder builder = ApiResponseSliderDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponseDeleteAt(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseSliderDelete> deleteSliderPermanent(FindByIdSliderRequest req) {
        return service.deleteSliderPermanently((long) req.getId())
                .map(res -> ApiResponseSliderDelete.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }

    @Override
    public Future<ApiResponseSliderAll> restoreAllSlider(Empty req) {
        return service.restoreAllSliders()
                .map(res -> ApiResponseSliderAll.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }

    @Override
    public Future<ApiResponseSliderAll> deleteAllSliderPermanent(Empty req) {
        return service.deleteAllPermanentSliders()
                .map(res -> ApiResponseSliderAll.newBuilder()
                        .setStatus(res.status() != null ? res.status() : "error")
                        .setMessage(res.message() != null ? res.message() : "")
                        .build());
    }
}
