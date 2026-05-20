package io.example.category.repository;

import io.example.category.model.Category;
import io.vertx.core.Future;
import pb.category.CategoryCommand;

public interface CategoryCommandRepository {
    Future<Category> createCategory(CategoryCommand.CreateCategoryRequest req);
    Future<Category> updateCategory(CategoryCommand.UpdateCategoryRequest req);
    Future<Category> trashCategory(Long categoryId);
    Future<Category> restoreCategory(Long categoryId);
    Future<Void> deleteCategoryPermanently(Long categoryId);
    Future<Integer> restoreAllCategories();
    Future<Integer> deleteAllPermanentCategories();
}
