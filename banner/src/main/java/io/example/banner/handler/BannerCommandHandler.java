package io.example.banner.handler;

import com.google.protobuf.Empty;
import io.example.banner.service.BannerCommandService;
import io.example.common.grpc.GrpcExceptionMapper;
import io.example.common.grpc.GrpcServerBinder;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.banner.BannerCommon.ApiResponseBanner;
import pb.banner.BannerCommon.ApiResponseBannerAll;
import pb.banner.BannerCommon.ApiResponseBannerDelete;
import pb.banner.BannerCommon.ApiResponseBannerDeleteAt;
import pb.banner.BannerCommon.FindByIdBannerRequest;
import pb.banner.BannerCommand.CreateBannerRequest;
import pb.banner.BannerCommand.UpdateBannerRequest;

@RequiredArgsConstructor
public class BannerCommandHandler implements pb.banner.VertxBannerCommandServiceGrpcServer.BannerCommandServiceApi {
    private final BannerCommandService service;

    @Override
    public Future<ApiResponseBanner> create(CreateBannerRequest req) {
        var reqDomain = io.example.banner.domain.requests.CreateBannerRequest.builder()
                .name(req.getName())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .isActive(req.getIsActive())
                .build();

        return service.createBanner(reqDomain)
                .map(data -> ApiResponseBanner.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.fromBannerResponse(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseBanner> update(UpdateBannerRequest req) {
        var reqDomain = io.example.banner.domain.requests.UpdateBannerRequest.builder()
                .bannerId((long) req.getBannerId())
                .name(req.getName())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .isActive(req.getIsActive())
                .build();

        return service.updateBanner(reqDomain)
                .map(data -> ApiResponseBanner.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.fromBannerResponse(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseBannerDeleteAt> trash(FindByIdBannerRequest req) {
        return service.trashBanner((long)req.getId())
                .map(data -> ApiResponseBannerDeleteAt.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.fromBannerResponseDeleteAt(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseBannerDeleteAt> restore(FindByIdBannerRequest req) {
        return service.restoreBanner((long)req.getId())
                .map(data -> ApiResponseBannerDeleteAt.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.fromBannerResponseDeleteAt(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseBannerDelete> deletePermanent(FindByIdBannerRequest req) {
        return service.deletePermanent((long) req.getId())
                .map(v -> ApiResponseBannerDelete.newBuilder()
                        .setStatus("success")
                        .setMessage("Banner deleted permanently")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseBannerAll> restoreAll(Empty req) {
        return service.restoreAllBanners()
                .map(v -> ApiResponseBannerAll.newBuilder()
                        .setStatus("success")
                        .setMessage("All banners restored successfully")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseBannerAll> deleteAll(Empty req) {
        return service.deleteAllPermanentBanners()
                .map(v -> ApiResponseBannerAll.newBuilder()
                        .setStatus("success")
                        .setMessage("All banners permanently deleted")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public pb.banner.VertxBannerCommandServiceGrpcServer.BannerCommandServiceApi bindAll(
            io.vertx.grpc.server.GrpcServer server) {
        GrpcServerBinder.bind(server, pb.banner.VertxBannerCommandServiceGrpcServer.Create, this::create);
        GrpcServerBinder.bind(server, pb.banner.VertxBannerCommandServiceGrpcServer.Update, this::update);
        GrpcServerBinder.bind(server, pb.banner.VertxBannerCommandServiceGrpcServer.Trash, this::trash);
        GrpcServerBinder.bind(server, pb.banner.VertxBannerCommandServiceGrpcServer.Restore, this::restore);
        GrpcServerBinder.bind(server, pb.banner.VertxBannerCommandServiceGrpcServer.DeletePermanent,
                this::deletePermanent);
        GrpcServerBinder.bind(server, pb.banner.VertxBannerCommandServiceGrpcServer.RestoreAll, this::restoreAll);
        GrpcServerBinder.bind(server, pb.banner.VertxBannerCommandServiceGrpcServer.DeleteAll, this::deleteAll);
        return this;
    }
}