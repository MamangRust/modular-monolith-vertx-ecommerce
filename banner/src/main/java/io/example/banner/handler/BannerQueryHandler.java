package io.example.banner.handler;

import io.example.common.domain.PagedResult;
import io.example.common.grpc.GrpcExceptionMapper;
import io.example.common.grpc.GrpcServerBinder;
import io.example.banner.domain.requests.FindAllBannerRequest;
import io.example.banner.model.BannerResponse;
import io.example.banner.model.BannerResponseDeleteAt;
import io.example.banner.service.BannerQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.banner.BannerCommon.ApiResponsePaginationBanner;
import pb.banner.BannerCommon.ApiResponsePaginationBannerDeleteAt;
import pb.banner.BannerCommon.ApiResponseBanner;
import pb.banner.BannerCommon.FindByIdBannerRequest;

@RequiredArgsConstructor
public class BannerQueryHandler implements pb.banner.VertxBannerQueryServiceGrpcServer.BannerQueryServiceApi {
        private final BannerQueryService service;

        private FindAllBannerRequest toDomainReq(pb.banner.BannerQuery.FindAllBannerRequest req) {
                return FindAllBannerRequest.builder()
                                .search(req.getSearch())
                                .page(req.getPage() > 0 ? req.getPage() : 1)
                                .pageSize(req.getPageSize() > 0 ? req.getPageSize() : 10)
                                .build();
        }

        private pb.Api.PaginationMeta toMeta(int totalRecords, int page, int pageSize) {
                int currentPage = page > 0 ? page : 1;
                int size = pageSize > 0 ? pageSize : 10;
                int totalPages = size > 0 ? (int) Math.ceil((double) totalRecords / size) : 0;
                return pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(currentPage)
                                .setPageSize(size)
                                .setTotalPages(totalPages)
                                .setTotalRecords(totalRecords)
                                .build();
        }

        @Override
        public Future<ApiResponsePaginationBanner> findAll(pb.banner.BannerQuery.FindAllBannerRequest req) {
                FindAllBannerRequest domainReq = toDomainReq(req);
                Future<PagedResult<BannerResponse>> bannersFuture = service.getBanners(domainReq);

                return bannersFuture
                                .map(res -> ApiResponsePaginationBanner.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::fromBannerResponse).toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
        }

        @Override
        public Future<ApiResponseBanner> findById(FindByIdBannerRequest req) {
                return service.getBannerById((long) req.getId())
                                .map(res -> ApiResponseBanner.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.fromBannerResponse(res))
                                                .build())
                                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
        }

        @Override
        public Future<ApiResponsePaginationBannerDeleteAt> findByActive(
                        pb.banner.BannerQuery.FindAllBannerRequest req) {
                FindAllBannerRequest domainReq = toDomainReq(req);
                Future<PagedResult<BannerResponseDeleteAt>> activeBannersFuture = service.getActiveBanners(domainReq);

                return activeBannersFuture
                                .map(res -> ApiResponsePaginationBannerDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::fromBannerResponseDeleteAt)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
        }

        @Override
        public pb.banner.VertxBannerQueryServiceGrpcServer.BannerQueryServiceApi bindAll(
                        io.vertx.grpc.server.GrpcServer server) {
                GrpcServerBinder.bind(server, pb.banner.VertxBannerQueryServiceGrpcServer.FindAll, this::findAll);
                GrpcServerBinder.bind(server, pb.banner.VertxBannerQueryServiceGrpcServer.FindById, this::findById);
                GrpcServerBinder.bind(server, pb.banner.VertxBannerQueryServiceGrpcServer.FindByActive,
                                this::findByActive);
                GrpcServerBinder.bind(server, pb.banner.VertxBannerQueryServiceGrpcServer.FindByTrashed,
                                this::findByTrashed);
                return this;
        }

        @Override
        public Future<ApiResponsePaginationBannerDeleteAt> findByTrashed(
                        pb.banner.BannerQuery.FindAllBannerRequest req) {
                FindAllBannerRequest domainReq = toDomainReq(req);
                Future<PagedResult<BannerResponseDeleteAt>> trashedBannersFuture = service.getTrashedBanners(domainReq);

                return trashedBannersFuture
                                .map(res -> ApiResponsePaginationBannerDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::fromBannerResponseDeleteAt)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
        }
}