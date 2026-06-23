package io.example.category.handler;

import com.google.protobuf.Empty;
import io.example.category.service.CategoryCommandService;
import io.example.common.grpc.GrpcExceptionMapper;
import io.example.category.domain.requests.CreateCategoryRequest;
import io.example.category.domain.requests.UpdateCategoryRequest;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.category.CategoryCommon;
import pb.category.CategoryCommand;

@RequiredArgsConstructor
public class CategoryCommandHandler
        implements pb.category.VertxCategoryCommandServiceGrpcServer.CategoryCommandServiceApi {

    private final CategoryCommandService service;

    @Override
    public Future<CategoryCommon.ApiResponseCategory> create(CategoryCommand.CreateCategoryRequest req) {
        CreateCategoryRequest domainReq = CreateCategoryRequest.builder()
                .name(req.getName())
                .description(req.getDescription())
                .slugCategory(req.getSlugCategory())
                .imageCategory(req.getImageCategory())
                .build();

        return service.create(domainReq)
                .map(resp -> CategoryCommon.ApiResponseCategory.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toCategoryResponse(resp))
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategory> update(CategoryCommand.UpdateCategoryRequest req) {
        UpdateCategoryRequest domainReq = UpdateCategoryRequest.builder()
                .id((long) req.getCategoryId())
                .name(req.getName())
                .description(req.getDescription())
                .slugCategory(req.getSlugCategory())
                .imageCategory(req.getImageCategory())
                .build();

        return service.update(domainReq)
                .map(resp -> CategoryCommon.ApiResponseCategory.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toCategoryResponse(resp))
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryDeleteAt> trashedCategory(
            CategoryCommon.FindByIdCategoryRequest req) {
        return service.trash((long) req.getId())
                .map(resp -> CategoryCommon.ApiResponseCategoryDeleteAt.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toCategoryResponseDeleteAt(resp))
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryDeleteAt> restoreCategory(
            CategoryCommon.FindByIdCategoryRequest req) {
        return service.restore((long) req.getId())
                .map(resp -> CategoryCommon.ApiResponseCategoryDeleteAt.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.toCategoryResponseDeleteAt(resp))
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryDelete> deleteCategoryPermanent(
            CategoryCommon.FindByIdCategoryRequest req) {
        return service.deletePermanent((long) req.getId())
                .map(resp -> CategoryCommon.ApiResponseCategoryDelete.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryAll> restoreAllCategory(Empty req) {
        return service.restoreAll()
                .map(count -> CategoryCommon.ApiResponseCategoryAll.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryAll> deleteAllCategoryPermanent(Empty req) {
        return service.deleteAllPermanent()
                .map(count -> CategoryCommon.ApiResponseCategoryAll.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .build())
                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
    }
}