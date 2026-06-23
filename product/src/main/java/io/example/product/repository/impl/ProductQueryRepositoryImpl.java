package io.example.product.repository.impl;

import java.util.ArrayList;
import java.util.List;
import io.example.common.domain.PagedResult;
import io.example.product.domain.requests.FindAllProductRequest;
import io.example.product.domain.requests.FindAllProductMerchantRequest;
import io.example.product.domain.requests.FindAllProductCategoryRequest;
import io.example.product.model.Product;
import io.example.product.repository.ProductQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository {
    private final Pool client;

    @Override
    public Future<PagedResult<Product>> findAll(FindAllProductRequest req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

        return client
                .preparedQuery("""
                        SELECT
                            product_id,
                            merchant_id,
                            category_id,
                            name,
                            description,
                            price,
                            count_in_stock,
                            brand,
                            weight,
                            rating,
                            slug_product,
                            image_product,
                            created_at,
                            updated_at,
                            COUNT(*) OVER () AS total_count
                        FROM products
                        WHERE
                            deleted_at IS NULL
                            AND (
                                $1::TEXT IS NULL
                                OR name ILIKE '%' || $1 || '%'
                                OR description ILIKE '%' || $1 || '%'
                                OR brand ILIKE '%' || $1 || '%'
                                OR slug_product ILIKE '%' || $1 || '%'
                            )
                        ORDER BY created_at DESC
                        LIMIT $2
                        OFFSET $3
                        """)
                .execute(Tuple.of(
                        normalizeSearch(req.getSearch()),
                        req.getPageSize(),
                        offset))
                .map(this::mapPagedProducts);
    }

    @Override
    public Future<PagedResult<Product>> findActive(FindAllProductRequest req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

        return client
                .preparedQuery("""
                        SELECT
                            product_id,
                            merchant_id,
                            category_id,
                            name,
                            description,
                            price,
                            count_in_stock,
                            brand,
                            weight,
                            rating,
                            slug_product,
                            image_product,
                            created_at,
                            updated_at,
                            deleted_at,
                            COUNT(*) OVER () AS total_count
                        FROM products
                        WHERE
                            deleted_at IS NULL
                            AND (
                                $1::TEXT IS NULL
                                OR name ILIKE '%' || $1 || '%'
                                OR description ILIKE '%' || $1 || '%'
                                OR brand ILIKE '%' || $1 || '%'
                                OR slug_product ILIKE '%' || $1 || '%'
                            )
                        ORDER BY created_at DESC
                        LIMIT $2
                        OFFSET $3
                        """)
                .execute(Tuple.of(
                        normalizeSearch(req.getSearch()),
                        req.getPageSize(),
                        offset))
                .map(this::mapPagedProducts);
    }

    @Override
    public Future<PagedResult<Product>> findTrashed(FindAllProductRequest req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

        return client
                .preparedQuery("""
                        SELECT
                            product_id,
                            merchant_id,
                            category_id,
                            name,
                            description,
                            price,
                            count_in_stock,
                            brand,
                            weight,
                            rating,
                            slug_product,
                            image_product,
                            created_at,
                            updated_at,
                            deleted_at,
                            COUNT(*) OVER () AS total_count
                        FROM products
                        WHERE
                            deleted_at IS NOT NULL
                            AND (
                                $1::TEXT IS NULL
                                OR name ILIKE '%' || $1 || '%'
                                OR description ILIKE '%' || $1 || '%'
                                OR brand ILIKE '%' || $1 || '%'
                                OR slug_product ILIKE '%' || $1 || '%'
                            )
                        ORDER BY created_at DESC
                        LIMIT $2
                        OFFSET $3
                        """)
                .execute(Tuple.of(
                        normalizeSearch(req.getSearch()),
                        req.getPageSize(),
                        offset))
                .map(this::mapPagedProducts);
    }

    @Override
    public Future<PagedResult<Product>> findByMerchant(FindAllProductMerchantRequest req) {
        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        int offset = (page - 1) * pageSize;

        return client
                .preparedQuery("""
                        WITH
                            filtered_products AS (
                                SELECT
                                    p.product_id,
                                    p.merchant_id,
                                    p.category_id,
                                    p.weight,
                                    p.rating,
                                    p.slug_product,
                                    p.name,
                                    p.description,
                                    p.price,
                                    p.count_in_stock,
                                    p.brand,
                                    p.image_product,
                                    p.created_at,
                                    p.updated_at,
                                    c.name AS category_name
                                FROM products p
                                    JOIN categories c ON p.category_id = c.category_id
                                WHERE
                                    p.deleted_at IS NULL
                                    AND p.merchant_id = $1
                                    AND (
                                        p.name ILIKE '%' || COALESCE($2, '') || '%'
                                        OR p.description ILIKE '%' || COALESCE($2, '') || '%'
                                        OR $2 IS NULL
                                    )
                                    AND (
                                        c.category_id = NULLIF($3, 0)
                                        OR NULLIF($3, 0) IS NULL
                                    )
                                    AND (
                                        p.price >= COALESCE(NULLIF($4, 0), 0)
                                        AND p.price <= COALESCE(NULLIF($5, 0), 999999999)
                                    )
                            )
                        SELECT (
                                SELECT COUNT(*)
                                FROM filtered_products
                            ) AS total_count, fp.*
                        FROM filtered_products fp
                        ORDER BY fp.created_at DESC
                        LIMIT $6
                        OFFSET $7
                        """)
                .execute(Tuple.of(
                        req.getMerchantId(),
                        req.getSearch(),
                        req.getCategoryId(),
                        req.getMinPrice(),
                        req.getMaxPrice(),
                        pageSize,
                        offset))
                .map(this::mapPagedProducts);
    }

    @Override
    public Future<PagedResult<Product>> findByCategory(FindAllProductCategoryRequest req) {
        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        int offset = (page - 1) * pageSize;

        return client
                .preparedQuery("""
                        WITH
                            filtered_products AS (
                                SELECT
                                    p.product_id,
                                    p.merchant_id,
                                    p.category_id,
                                    p.weight,
                                    p.rating,
                                    p.slug_product,
                                    p.name,
                                    p.description,
                                    p.price,
                                    p.count_in_stock,
                                    p.brand,
                                    p.image_product,
                                    p.created_at,
                                    p.updated_at,
                                    c.name AS category_name
                                FROM products p
                                    JOIN categories c ON p.category_id = c.category_id
                                WHERE
                                    p.deleted_at IS NULL
                                    AND c.name = $1
                                    AND (
                                        $2 IS NULL
                                        OR p.name ILIKE '%' || $2 || '%'
                                        OR p.description ILIKE '%' || $2 || '%'
                                    )
                                    AND (
                                        (
                                            $3 IS NULL
                                            OR p.price >= $3
                                        )
                                        AND (
                                            $4 IS NULL
                                            OR p.price <= $4
                                        )
                                    )
                            )
                        SELECT (
                                SELECT COUNT(*)
                                FROM filtered_products
                            ) AS total_count, fp.*
                        FROM filtered_products fp
                        ORDER BY fp.created_at DESC
                        LIMIT $5
                        OFFSET $6
                        """)
                .execute(Tuple.of(
                        req.getCategoryName(),
                        req.getSearch(),
                        req.getMinPrice(),
                        req.getMaxPrice(),
                        pageSize,
                        offset))
                .map(this::mapPagedProducts);
    }

    @Override
    public Future<Product> findById(Long productId) {
        return client
                .preparedQuery("""
                        SELECT
                            product_id,
                            merchant_id,
                            category_id,
                            name,
                            description,
                            price,
                            count_in_stock,
                            brand,
                            weight,
                            rating,
                            slug_product,
                            image_product,
                            created_at,
                            updated_at
                        FROM products
                        WHERE
                            product_id = $1
                            AND deleted_at IS NULL
                        """)
                .execute(Tuple.of(productId))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<Product> findByIdTrashed(Long productId) {
        return client
                .preparedQuery("""
                        SELECT
                            product_id,
                            merchant_id,
                            category_id,
                            name,
                            description,
                            price,
                            count_in_stock,
                            brand,
                            weight,
                            rating,
                            slug_product,
                            image_product,
                            created_at,
                            updated_at,
                            deleted_at
                        FROM products
                        WHERE
                            product_id = $1
                        """)
                .execute(Tuple.of(productId))
                .map(this::mapSingleOrNull);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search;
    }

    private Product mapSingleOrNull(RowSet<Row> rows) {
        return rows.iterator().hasNext() ? Product.fromRow(rows.iterator().next()) : null;
    }

    private PagedResult<Product> mapPagedProducts(RowSet<Row> rows) {
        List<Product> items = new ArrayList<>();
        int total = 0;

        for (Row row : rows) {
            items.add(Product.fromRow(row));
            if (total == 0) {
                total = row.getInteger("total_count");
            }
        }

        return new PagedResult<>(items, total);
    }
}
