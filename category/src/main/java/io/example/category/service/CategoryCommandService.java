package io.example.category.service;

import io.example.category.domain.requests.CreateCategoryRequest;
import io.example.category.domain.requests.UpdateCategoryRequest;
import io.example.category.model.CategoryResponse;
import io.example.category.model.CategoryResponseDeleteAt;
import io.vertx.core.Future;

public interface CategoryCommandService {
    Future<CategoryResponse> create(CreateCategoryRequest req);

    Future<CategoryResponse> update(UpdateCategoryRequest req);

    Future<CategoryResponseDeleteAt> trash(Long id);

    Future<CategoryResponseDeleteAt> restore(Long id);

    Future<Boolean> deletePermanent(Long id);

    Future<Integer> restoreAll();

    Future<Integer> deleteAllPermanent();
}