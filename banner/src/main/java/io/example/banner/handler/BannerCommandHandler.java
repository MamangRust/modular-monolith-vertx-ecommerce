package io.example.banner.handler;

import com.google.protobuf.Empty;
import io.example.banner.service.BannerCommandService;
import io.vertx.core.Future;
import pb.banner.BannerCommon.ApiResponseBanner;
import pb.banner.BannerCommon.ApiResponseBannerAll;
import pb.banner.BannerCommon.ApiResponseBannerDelete;
import pb.banner.BannerCommon.ApiResponseBannerDeleteAt;
import pb.banner.BannerCommon.FindByIdBannerRequest;
import pb.banner.BannerCommand.CreateBannerRequest;
import pb.banner.BannerCommand.UpdateBannerRequest;

public class BannerCommandHandler implements pb.banner.VertxBannerCommandServiceGrpcServer.BannerCommandServiceApi {
    private final BannerCommandService service;

    public BannerCommandHandler(BannerCommandService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponseBanner> create(CreateBannerRequest req) {
        return service.createBanner(req)
                .map(resp -> {
                    ApiResponseBanner.Builder builder = ApiResponseBanner.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.fromBannerResponse(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseBanner> update(UpdateBannerRequest req) {
        return service.updateBanner(req)
                .map(resp -> {
                    ApiResponseBanner.Builder builder = ApiResponseBanner.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.fromBannerResponse(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseBannerDeleteAt> trash(FindByIdBannerRequest req) {
        return service.trashBanner((long) req.getId())
                .map(resp -> {
                    ApiResponseBannerDeleteAt.Builder builder = ApiResponseBannerDeleteAt.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.fromBannerResponseDeleteAt(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseBannerDeleteAt> restore(FindByIdBannerRequest req) {
        return service.restoreBanner((long) req.getId())
                .map(resp -> {
                    ApiResponseBannerDeleteAt.Builder builder = ApiResponseBannerDeleteAt.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.fromBannerResponseDeleteAt(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseBannerDelete> deletePermanent(FindByIdBannerRequest req) {
        return service.deletePermanent((long) req.getId())
                .map(resp -> ApiResponseBannerDelete.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .build());
    }

    @Override
    public Future<ApiResponseBannerAll> restoreAll(Empty req) {
        return service.restoreAllBanners()
                .map(resp -> ApiResponseBannerAll.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .build());
    }

    @Override
    public Future<ApiResponseBannerAll> deleteAll(Empty req) {
        return service.deleteAllPermanentBanners()
                .map(resp -> ApiResponseBannerAll.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .build());
    }
}
