package io.example.category.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.model.PagedResult;
import io.example.category.model.Category;
import io.example.category.repository.CategoryQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import pb.category.CategoryQuery;

public class CategoryQueryRepositoryImpl implements CategoryQueryRepository {

    private final Pool client;

    public CategoryQueryRepositoryImpl(Pool client) {
        this.client = client;
    }

    @Override
    public Future<PagedResult<Category>> getCategories(CategoryQuery.FindAllCategoryRequest req) {
        int page = req.getPage() > 0 ? req.getPage() : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        int offset = page * pageSize;

        return client
                .preparedQuery("""
                        SELECT
                            category_id,
                            name,
                            description,
                            slug_category,
                            image_category,
                            created_at,
                            updated_at,
                            COUNT(*) OVER () AS total_count
                        FROM categories
                        WHERE
                            deleted_at IS NULL
                            AND (
                                $1::TEXT IS NULL
                                OR name ILIKE '%' || $1 || '%'
                                OR slug_category ILIKE '%' || $1 || '%'
                            )
                        ORDER BY created_at DESC
                        LIMIT $2
                        OFFSET $3;
                        """)
                .execute(Tuple.of(normalizeSearch(req.getSearch()), pageSize, offset))
                .map(this::mapPagedCategories);
    }

    @Override
    public Future<PagedResult<Category>> getCategoriesActive(CategoryQuery.FindAllCategoryRequest req) {
        int page = req.getPage() > 0 ? req.getPage() : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        int offset = page * pageSize;

        return client
                .preparedQuery("""
                        SELECT
                            category_id,
                            name,
                            description,
                            slug_category,
                            image_category,
                            created_at,
                            updated_at,
                            deleted_at,
                            COUNT(*) OVER () AS total_count
                        FROM categories
                        WHERE
                            deleted_at IS NULL
                            AND (
                                $1::TEXT IS NULL
                                OR name ILIKE '%' || $1 || '%'
                                OR slug_category ILIKE '%' || $1 || '%'
                            )
                        ORDER BY created_at DESC
                        LIMIT $2
                        OFFSET $3;
                        """)
                .execute(Tuple.of(normalizeSearch(req.getSearch()), pageSize, offset))
                .map(this::mapPagedCategories);
    }

    @Override
    public Future<PagedResult<Category>> getCategoriesTrashed(CategoryQuery.FindAllCategoryRequest req) {
        int page = req.getPage() > 0 ? req.getPage() : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        int offset = page * pageSize;

        return client
                .preparedQuery("""
                        SELECT
                            category_id,
                            name,
                            description,
                            slug_category,
                            image_category,
                            created_at,
                            updated_at,
                            deleted_at,
                            COUNT(*) OVER () AS total_count
                        FROM categories
                        WHERE
                            deleted_at IS NOT NULL
                            AND (
                                $1::TEXT IS NULL
                                OR name ILIKE '%' || $1 || '%'
                                OR slug_category ILIKE '%' || $1 || '%'
                            )
                        ORDER BY created_at DESC
                        LIMIT $2
                        OFFSET $3;
                        """)
                .execute(Tuple.of(normalizeSearch(req.getSearch()), pageSize, offset))
                .map(this::mapPagedCategories);
    }

    @Override
    public Future<Category> getCategoryById(Long categoryId) {
        return client
                .preparedQuery("""
                        SELECT
                            category_id,
                            name,
                            description,
                            slug_category,
                            image_category,
                            created_at,
                            updated_at
                        FROM categories
                        WHERE
                            category_id = $1
                            AND deleted_at IS NULL;
                        """)
                .execute(Tuple.of(categoryId))
                .map(rows -> rows.iterator().hasNext() ? Category.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Category> getCategoryByIdTrashed(Long categoryId) {
        return client
                .preparedQuery("SELECT * FROM categories WHERE category_id = $1 AND deleted_at IS NOT NULL")
                .execute(Tuple.of(categoryId))
                .map(rows -> rows.iterator().hasNext() ? Category.fromRow(rows.iterator().next()) : null);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank())
            return null;
        return search;
    }

    private PagedResult<Category> mapPagedCategories(RowSet<Row> rows) {
        List<Category> list = new ArrayList<>();
        int total = 0;
        for (Row row : rows) {
            list.add(Category.fromRow(row));
            if (total == 0) {
                try {
                    total = row.getInteger("total_count");
                } catch (Exception ignored) {}
            }
        }
        return new PagedResult<>(list, total);
    }
}
