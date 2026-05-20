package io.example.category.handler;

import com.google.protobuf.Empty;
import io.example.category.service.CategoryCommandService;
import io.vertx.core.Future;
import pb.category.CategoryCommon;
import pb.category.CategoryCommand;

public class CategoryCommandHandler implements pb.category.VertxCategoryCommandServiceGrpcServer.CategoryCommandServiceApi {

    private final CategoryCommandService service;

    public CategoryCommandHandler(CategoryCommandService service) {
        this.service = service;
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategory> create(CategoryCommand.CreateCategoryRequest req) {
        return service.create(req)
                .map(resp -> {
                    var builder = CategoryCommon.ApiResponseCategory.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.toCategoryResponse(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategory> update(CategoryCommand.UpdateCategoryRequest req) {
        return service.update(req)
                .map(resp -> {
                    var builder = CategoryCommon.ApiResponseCategory.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.toCategoryResponse(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryDeleteAt> trashedCategory(CategoryCommon.FindByIdCategoryRequest req) {
        return service.trash((long) req.getId())
                .map(resp -> {
                    var builder = CategoryCommon.ApiResponseCategoryDeleteAt.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.toCategoryResponseDeleteAt(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryDeleteAt> restoreCategory(CategoryCommon.FindByIdCategoryRequest req) {
        return service.restore((long) req.getId())
                .map(resp -> {
                    var builder = CategoryCommon.ApiResponseCategoryDeleteAt.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.toCategoryResponseDeleteAt(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryDelete> deleteCategoryPermanent(CategoryCommon.FindByIdCategoryRequest req) {
        return service.deletePermanent((long) req.getId())
                .map(resp -> CategoryCommon.ApiResponseCategoryDelete.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryAll> restoreAllCategory(Empty req) {
        return service.restoreAll()
                .map(resp -> CategoryCommon.ApiResponseCategoryAll.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .build());
    }

    @Override
    public Future<CategoryCommon.ApiResponseCategoryAll> deleteAllCategoryPermanent(Empty req) {
        return service.deleteAllPermanent()
                .map(resp -> CategoryCommon.ApiResponseCategoryAll.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .build());
    }
}
