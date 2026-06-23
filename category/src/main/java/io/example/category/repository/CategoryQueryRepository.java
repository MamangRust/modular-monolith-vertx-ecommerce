package io.example.category.repository;

import io.example.common.domain.PagedResult;
import io.example.category.model.Category;
import io.example.category.domain.requests.FindAllCategoriesRequest;
import io.vertx.core.Future;

public interface CategoryQueryRepository {
    Future<PagedResult<Category>> getCategories(FindAllCategoriesRequest req);

    Future<PagedResult<Category>> getCategoriesActive(FindAllCategoriesRequest req);

    Future<PagedResult<Category>> getCategoriesTrashed(FindAllCategoriesRequest req);

    Future<Category> getCategoryById(Long categoryId);

    Future<Category> getCategoryByIdTrashed(Long categoryId);
}
