package io.example.category.service;

import io.example.common.domain.PagedResult;
import io.example.category.model.CategoryResponse;
import io.example.category.model.CategoryResponseDeleteAt;
import io.example.category.domain.requests.FindAllCategoriesRequest;
import io.vertx.core.Future;

public interface CategoryQueryService {
    Future<PagedResult<CategoryResponse>> getAll(FindAllCategoriesRequest req);

    Future<PagedResult<CategoryResponse>> getActive(FindAllCategoriesRequest req);

    Future<PagedResult<CategoryResponseDeleteAt>> getTrashed(FindAllCategoriesRequest req);

    Future<CategoryResponse> getById(Long id);
}