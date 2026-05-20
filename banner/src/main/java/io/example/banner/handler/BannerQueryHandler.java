package io.example.banner.handler;

import io.example.banner.service.BannerQueryService;
import io.vertx.core.Future;
import pb.banner.BannerCommon.ApiResponseBanner;
import pb.banner.BannerCommon.ApiResponsePaginationBanner;
import pb.banner.BannerCommon.ApiResponsePaginationBannerDeleteAt;
import pb.banner.BannerCommon.FindByIdBannerRequest;
import pb.banner.BannerQuery.FindAllBannerRequest;

public class BannerQueryHandler implements pb.banner.VertxBannerQueryServiceGrpcServer.BannerQueryServiceApi {
    private final BannerQueryService service;

    public BannerQueryHandler(BannerQueryService service) {
        this.service = service;
    }

    private pb.Api.PaginationMeta toMeta(io.example.common.model.PaginationMeta meta) {
        if (meta == null) {
            return pb.Api.PaginationMeta.getDefaultInstance();
        }
        return pb.Api.PaginationMeta.newBuilder()
                .setCurrentPage(meta.currentPage())
                .setPageSize(meta.pageSize())
                .setTotalPages(meta.totalPages())
                .setTotalRecords(meta.totalRecords())
                .build();
    }

    @Override
    public Future<ApiResponsePaginationBanner> findAll(FindAllBannerRequest req) {
        return service.getAllBanners(req)
                .map(resp -> ApiResponsePaginationBanner.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::fromBannerResponse).toList())
                        .setPagination(toMeta(resp.pagination()))
                        .build());
    }

    @Override
    public Future<ApiResponseBanner> findById(FindByIdBannerRequest req) {
        return service.getBannerById((long) req.getId())
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
    public Future<ApiResponsePaginationBannerDeleteAt> findByActive(FindAllBannerRequest req) {
        return service.getActiveBanners(req)
                .map(resp -> ApiResponsePaginationBannerDeleteAt.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::fromBannerResponseDeleteAt).toList())
                        .setPagination(toMeta(resp.pagination()))
                        .build());
    }

    @Override
    public Future<ApiResponsePaginationBannerDeleteAt> findByTrashed(FindAllBannerRequest req) {
        return service.getTrashedBanners(req)
                .map(resp -> ApiResponsePaginationBannerDeleteAt.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .addAllData(resp.data().stream().map(ProtoConverter::fromBannerResponseDeleteAt).toList())
                        .setPagination(toMeta(resp.pagination()))
                        .build());
    }
}
