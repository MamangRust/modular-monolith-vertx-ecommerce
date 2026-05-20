package io.example.category.service;

import java.util.List;
import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.category.model.CategoryResponse;
import io.example.category.model.CategoryResponseDeleteAt;
import io.vertx.core.Future;
import pb.category.CategoryQuery;

public interface CategoryQueryService {
    Future<ApiResponsePagination<List<CategoryResponse>>> getAll(CategoryQuery.FindAllCategoryRequest req);
    Future<ApiResponsePagination<List<CategoryResponse>>> getActive(CategoryQuery.FindAllCategoryRequest req);
    Future<ApiResponsePagination<List<CategoryResponseDeleteAt>>> getTrashed(CategoryQuery.FindAllCategoryRequest req);
    Future<ApiResponse<CategoryResponse>> getById(Long id);
}
