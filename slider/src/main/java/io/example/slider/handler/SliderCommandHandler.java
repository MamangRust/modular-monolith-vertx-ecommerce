package io.example.slider.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.slider.domain.requests.CreateSliderRequest;
import io.example.slider.domain.requests.UpdateSliderRequest;
import io.example.slider.service.SliderCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.slider.SliderCommon.ApiResponseSlider;
import pb.slider.SliderCommon.ApiResponseSliderAll;
import pb.slider.SliderCommon.ApiResponseSliderDelete;
import pb.slider.SliderCommon.ApiResponseSliderDeleteAt;
import pb.slider.SliderCommon.FindByIdSliderRequest;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class SliderCommandHandler implements pb.slider.VertxSliderCommandServiceGrpcServer.SliderCommandServiceApi {
        private final SliderCommandService service;

        @Override
        public Future<ApiResponseSlider> create(pb.slider.SliderCommand.CreateSliderRequest req) {
                CreateSliderRequest reqDto = CreateSliderRequest.builder()
                                .name(req.getName())
                                .image(req.getImage())
                                .build();

                return service.createSlider(reqDto)
                                .map(data -> ApiResponseSlider.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseSlider> update(pb.slider.SliderCommand.UpdateSliderRequest req) {
                UpdateSliderRequest reqDto = UpdateSliderRequest.builder()
                                .sliderId((long) req.getId())
                                .name(req.getName())
                                .image(req.getImage())
                                .build();

                return service.updateSlider(reqDto)
                                .map(data -> ApiResponseSlider.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseSliderDeleteAt> trashedSlider(FindByIdSliderRequest req) {
                return service.trashSlider((long) req.getId())
                                .map(data -> ApiResponseSliderDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponseDeleteAt(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseSliderDeleteAt> restoreSlider(FindByIdSliderRequest req) {
                return service.restoreSlider((long) req.getId())
                                .map(data -> ApiResponseSliderDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponseDeleteAt(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseSliderDelete> deleteSliderPermanent(FindByIdSliderRequest req) {
                return service.deleteSliderPermanently((long) req.getId())
                                .map(v -> ApiResponseSliderDelete.newBuilder()
                                                .setStatus("success")
                                                .setMessage("Slider deleted permanently")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseSliderAll> restoreAllSlider(Empty req) {
                return service.restoreAllSliders()
                                .map(v -> ApiResponseSliderAll.newBuilder()
                                                .setStatus("success")
                                                .setMessage("All sliders restored successfully")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseSliderAll> deleteAllSliderPermanent(Empty req) {
                return service.deleteAllPermanentSliders()
                                .map(v -> ApiResponseSliderAll.newBuilder()
                                                .setStatus("success")
                                                .setMessage("All sliders permanently deleted")
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

  @Override
  public pb.slider.VertxSliderCommandServiceGrpcServer.SliderCommandServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.slider.VertxSliderCommandServiceGrpcServer.Create, this::create);
    GrpcServerBinder.bind(server, pb.slider.VertxSliderCommandServiceGrpcServer.Update, this::update);
    GrpcServerBinder.bind(server, pb.slider.VertxSliderCommandServiceGrpcServer.TrashedSlider, this::trashedSlider);
    GrpcServerBinder.bind(server, pb.slider.VertxSliderCommandServiceGrpcServer.RestoreSlider, this::restoreSlider);
    GrpcServerBinder.bind(server, pb.slider.VertxSliderCommandServiceGrpcServer.DeleteSliderPermanent, this::deleteSliderPermanent);
    GrpcServerBinder.bind(server, pb.slider.VertxSliderCommandServiceGrpcServer.RestoreAllSlider, this::restoreAllSlider);
    GrpcServerBinder.bind(server, pb.slider.VertxSliderCommandServiceGrpcServer.DeleteAllSliderPermanent, this::deleteAllSliderPermanent);
    return this;
  }
}