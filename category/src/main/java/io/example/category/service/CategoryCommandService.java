package io.example.category.service;

import io.example.common.model.ApiResponse;
import io.example.category.model.CategoryResponse;
import io.example.category.model.CategoryResponseDeleteAt;
import io.vertx.core.Future;
import pb.category.CategoryCommand;

public interface CategoryCommandService {
    Future<ApiResponse<CategoryResponse>> create(CategoryCommand.CreateCategoryRequest req);
    Future<ApiResponse<CategoryResponse>> update(CategoryCommand.UpdateCategoryRequest req);
    Future<ApiResponse<CategoryResponseDeleteAt>> trash(Long id);
    Future<ApiResponse<CategoryResponseDeleteAt>> restore(Long id);
    Future<ApiResponse<Boolean>> deletePermanent(Long id);
    Future<ApiResponse<Integer>> restoreAll();
    Future<ApiResponse<Integer>> deleteAllPermanent();
}
