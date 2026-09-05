package io.example.category.repository.impl;

import io.example.category.model.Category;
import io.example.category.repository.CategoryCommandRepository;
import io.example.category.domain.requests.CreateCategoryRequest;
import io.example.category.domain.requests.UpdateCategoryRequest;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CategoryCommandRepositoryImpl implements CategoryCommandRepository {

    private final Pool client;

    @Override
    public Future<Category> createCategory(CreateCategoryRequest req) {
        return client
                .preparedQuery("""
                        INSERT INTO
                            categories (
                                name,
                                description,
                                slug_category,
                                image_category
                            )
                        VALUES ($1, $2, $3, $4)
                        RETURNING
                            category_id,
                            name,
                            description,
                            slug_category,
                            image_category,
                            created_at,
                            updated_at,
                            deleted_at;
                        """)
                .execute(Tuple.of(req.getName(), req.getDescription(), req.getSlugCategory(), req.getImageCategory()))
                .map(rows -> Category.fromRow(rows.iterator().next()));
    }

    @Override
    public Future<Category> updateCategory(UpdateCategoryRequest req) {
        return client
                .preparedQuery("""
                        UPDATE categories
                        SET
                            name = COALESCE(NULLIF($2, ''), name),
                            description = COALESCE(NULLIF($3, ''), description),
                            slug_category = COALESCE(NULLIF($4, ''), slug_category),
                            image_category = COALESCE(NULLIF($5, ''), image_category),
                            updated_at = CURRENT_TIMESTAMP
                        WHERE
                            category_id = $1
                            AND deleted_at IS NULL
                        RETURNING
                            category_id,
                            name,
                            description,
                            slug_category,
                            image_category,
                            created_at,
                            updated_at,
                            deleted_at;
                        """)
                .execute(Tuple.of(req.getId(), req.getName() != null ? req.getName() : "", req.getDescription() != null ? req.getDescription() : "", req.getSlugCategory() != null ? req.getSlugCategory() : "",
                        req.getImageCategory() != null ? req.getImageCategory() : ""))
                .map(rows -> rows.iterator().hasNext() ? Category.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Category> trashCategory(Long categoryId) {
        return client
                .preparedQuery("""
                        UPDATE categories
                        SET
                            deleted_at = current_timestamp
                        WHERE
                            category_id = $1
                            AND deleted_at IS NULL
                        RETURNING
                            category_id,
                            name,
                            description,
                            slug_category,
                            image_category,
                            created_at,
                            updated_at,
                            deleted_at;
                        """)
                .execute(Tuple.of(categoryId))
                .map(rows -> rows.iterator().hasNext() ? Category.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Category> restoreCategory(Long categoryId) {
        return client
                .preparedQuery("""
                        UPDATE categories
                        SET
                            deleted_at = NULL
                        WHERE
                            category_id = $1
                            AND deleted_at IS NOT NULL
                        RETURNING
                            category_id,
                            name,
                            description,
                            slug_category,
                            image_category,
                            created_at,
                            updated_at,
                            deleted_at;
                        """)
                .execute(Tuple.of(categoryId))
                .map(rows -> rows.iterator().hasNext() ? Category.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Boolean> deleteCategoryPermanently(Long categoryId) {
        return client
                .preparedQuery("DELETE FROM categories WHERE category_id = $1 AND deleted_at IS NOT NULL")
                .execute(Tuple.of(categoryId))
                .map(rows -> rows.rowCount() > 0);
    }

    @Override
    public Future<Integer> restoreAllCategories() {
        return client
                .preparedQuery("UPDATE categories SET deleted_at = NULL WHERE deleted_at IS NOT NULL")
                .execute()
                .map(RowSet::rowCount);
    }

    @Override
    public Future<Integer> deleteAllPermanentCategories() {
        return client
                .preparedQuery("DELETE FROM categories WHERE deleted_at IS NOT NULL")
                .execute()
                .map(RowSet::rowCount);
    }
}
