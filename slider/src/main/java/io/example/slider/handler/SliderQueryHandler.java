package io.example.slider.handler;

import io.example.slider.model.FindAllSlider;
import io.example.slider.service.SliderQueryService;
import io.vertx.core.Future;
import pb.slider.SliderCommon.ApiResponsePaginationSlider;
import pb.slider.SliderCommon.ApiResponsePaginationSliderDeleteAt;
import pb.slider.SliderCommon.ApiResponseSlider;
import pb.slider.SliderCommon.FindByIdSliderRequest;

public class SliderQueryHandler implements pb.slider.VertxSliderQueryServiceGrpcServer.SliderQueryServiceApi {
    private final SliderQueryService service;

    public SliderQueryHandler(SliderQueryService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponsePaginationSlider> findAll(pb.slider.SliderQuery.FindAllSliderRequest req) {
        FindAllSlider reqDto = FindAllSlider.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getAllSliders(reqDto)
                .map(res -> {
                    ApiResponsePaginationSlider.Builder builder = ApiResponsePaginationSlider.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream().map(ProtoConverter::toProtoResponse).toList());
                    }

                    if (res.pagination() != null) {
                        builder.setPagination(pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(res.pagination().currentPage())
                                .setPageSize(res.pagination().pageSize())
                                .setTotalPages(res.pagination().totalPages())
                                .setTotalRecords(res.pagination().totalRecords())
                                .build());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseSlider> findById(FindByIdSliderRequest req) {
        return service.getSliderById((long) req.getId())
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
    public Future<ApiResponsePaginationSliderDeleteAt> findByTrashed(pb.slider.SliderQuery.FindAllSliderRequest req) {
        FindAllSlider reqDto = FindAllSlider.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getTrashedSliders(reqDto)
                .map(res -> {
                    ApiResponsePaginationSliderDeleteAt.Builder builder = ApiResponsePaginationSliderDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList());
                    }

                    if (res.pagination() != null) {
                        builder.setPagination(pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(res.pagination().currentPage())
                                .setPageSize(res.pagination().pageSize())
                                .setTotalPages(res.pagination().totalPages())
                                .setTotalRecords(res.pagination().totalRecords())
                                .build());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponsePaginationSliderDeleteAt> findByActive(pb.slider.SliderQuery.FindAllSliderRequest req) {
        FindAllSlider reqDto = FindAllSlider.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getActiveSliders(reqDto)
                .map(res -> {
                    ApiResponsePaginationSliderDeleteAt.Builder builder = ApiResponsePaginationSliderDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.addAllData(res.data().stream().map(ProtoConverter::toProtoResponseDeleteAt).toList());
                    }

                    if (res.pagination() != null) {
                        builder.setPagination(pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(res.pagination().currentPage())
                                .setPageSize(res.pagination().pageSize())
                                .setTotalPages(res.pagination().totalPages())
                                .setTotalRecords(res.pagination().totalRecords())
                                .build());
                    }

                    return builder.build();
                });
    }
}
