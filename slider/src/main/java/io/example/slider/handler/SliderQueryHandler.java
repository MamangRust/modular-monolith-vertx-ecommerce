package io.example.slider.handler;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.slider.domain.requests.FindAllSlider;
import io.example.slider.service.SliderQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.slider.SliderCommon.ApiResponsePaginationSlider;
import pb.slider.SliderCommon.ApiResponsePaginationSliderDeleteAt;
import pb.slider.SliderCommon.ApiResponseSlider;
import pb.slider.SliderCommon.FindByIdSliderRequest;

@RequiredArgsConstructor
public class SliderQueryHandler implements pb.slider.VertxSliderQueryServiceGrpcServer.SliderQueryServiceApi {
        private final SliderQueryService service;

        private FindAllSlider toDomainReq(pb.slider.SliderQuery.FindAllSliderRequest req) {
                return FindAllSlider.builder()
                                .page(req.getPage())
                                .pageSize(req.getPageSize())
                                .search(req.getSearch())
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
        public Future<ApiResponsePaginationSlider> findAll(pb.slider.SliderQuery.FindAllSliderRequest req) {
                FindAllSlider domainReq = toDomainReq(req);
                return service.getAllSliders(domainReq)
                                .map(res -> ApiResponsePaginationSlider.newBuilder()
                                                .setStatus("success").setMessage("OK")
                                                .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponse)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponseSlider> findById(FindByIdSliderRequest req) {
                return service.getSliderById((long) req.getId())
                                .map(data -> ApiResponseSlider.newBuilder()
                                                .setStatus("success").setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponse(data))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponsePaginationSliderDeleteAt> findByActive(
                        pb.slider.SliderQuery.FindAllSliderRequest req) {
                FindAllSlider domainReq = toDomainReq(req);
                return service.getActiveSliders(domainReq)
                                .map(res -> ApiResponsePaginationSliderDeleteAt.newBuilder()
                                                .setStatus("success").setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::toProtoResponseDeleteAt).toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }

        @Override
        public Future<ApiResponsePaginationSliderDeleteAt> findByTrashed(
                        pb.slider.SliderQuery.FindAllSliderRequest req) {
                FindAllSlider domainReq = toDomainReq(req);
                return service.getTrashedSliders(domainReq)
                                .map(res -> ApiResponsePaginationSliderDeleteAt.newBuilder()
                                                .setStatus("success").setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::toProtoResponseDeleteAt).toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(GrpcExceptionMapper::toFailedFuture);
        }
}