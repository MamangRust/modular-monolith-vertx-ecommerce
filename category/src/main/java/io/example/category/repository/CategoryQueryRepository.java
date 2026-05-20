package io.example.category.repository;

import io.example.common.model.PagedResult;
import io.example.category.model.Category;
import io.vertx.core.Future;
import pb.category.CategoryQuery;

public interface CategoryQueryRepository {
    Future<PagedResult<Category>> getCategories(CategoryQuery.FindAllCategoryRequest req);
    Future<PagedResult<Category>> getCategoriesActive(CategoryQuery.FindAllCategoryRequest req);
    Future<PagedResult<Category>> getCategoriesTrashed(CategoryQuery.FindAllCategoryRequest req);
    Future<Category> getCategoryById(Long categoryId);
    Future<Category> getCategoryByIdTrashed(Long categoryId);
}
