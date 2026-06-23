package io.example.product.repository.impl;

import io.example.product.domain.requests.CreateProductRequest;
import io.example.product.domain.requests.UpdateProductRequest;
import io.example.product.model.Product;
import io.example.product.repository.ProductCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductCommandRepositoryImpl implements ProductCommandRepository {
    private final Pool client;

    @Override
    public Future<Product> create(CreateProductRequest req) {
        return client
                .preparedQuery("""
                        INSERT INTO
                            products (
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
                                image_product
                            )
                        VALUES (
                                $1,
                                $2,
                                $3,
                                $4,
                                $5,
                                $6,
                                $7,
                                $8,
                                $9,
                                $10,
                                $11
                            )
                        RETURNING
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
                        """)
                .execute(Tuple.of(
                        req.getMerchantId(),
                        req.getCategoryId(),
                        req.getName(),
                        req.getDescription(),
                        req.getPrice(),
                        req.getCountInStock(),
                        req.getBrand(),
                        req.getWeight(),
                        req.getRating() != null ? req.getRating().floatValue() : 0.0f,
                        req.getSlugProduct(),
                        req.getImageProduct()))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<Product> update(UpdateProductRequest req) {
        return client
                .preparedQuery("""
                        UPDATE products
                        SET
                            category_id = $2,
                            name = $3,
                            description = $4,
                            price = $5,
                            count_in_stock = $6,
                            brand = $7,
                            weight = $8,
                            rating = $9,
                            slug_product = $10,
                            image_product = $11,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE
                            product_id = $1
                            AND deleted_at IS NULL
                        RETURNING
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
                        """)
                .execute(Tuple.of(
                        req.getProductId(),
                        req.getCategoryId(),
                        req.getName(),
                        req.getDescription(),
                        req.getPrice(),
                        req.getCountInStock(),
                        req.getBrand(),
                        req.getWeight(),
                        req.getRating() != null ? req.getRating().floatValue() : 0.0f,
                        req.getSlugProduct(),
                        req.getImageProduct()))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<Product> updateProductCountStock(Integer productId, Integer countInStock) {
        return client
                .preparedQuery("""
                        UPDATE products
                        SET
                            count_in_stock = $2,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE
                            product_id = $1
                            AND deleted_at IS NULL
                        RETURNING
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
                        """)
                .execute(Tuple.of(productId, countInStock))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<Product> trash(Long productId) {
        return client
                .preparedQuery("""
                        UPDATE products
                        SET
                            deleted_at = CURRENT_TIMESTAMP,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE
                            product_id = $1
                            AND deleted_at IS NULL
                        RETURNING
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
                        """)
                .execute(Tuple.of(productId))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<Product> restore(Long productId) {
        return client
                .preparedQuery("""
                        UPDATE products
                        SET
                            deleted_at = NULL,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE
                            product_id = $1
                            AND deleted_at IS NOT NULL
                        RETURNING
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
                        """)
                .execute(Tuple.of(productId))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<Boolean> deletePermanent(Long productId) {
        return client
                .preparedQuery("""
                        DELETE FROM products
                        WHERE
                            product_id = $1
                            AND deleted_at IS NOT NULL
                        """)
                .execute(Tuple.of(productId))
                .map(rows -> rows.rowCount() > 0);
    }

    @Override
    public Future<Integer> restoreAll() {
        return client
                .preparedQuery("""
                        UPDATE products
                        SET
                            deleted_at = NULL,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE
                            deleted_at IS NOT NULL
                        """)
                .execute()
                .map(RowSet::rowCount);
    }

    @Override
    public Future<Integer> deleteAll() {
        return client
                .preparedQuery("DELETE FROM products WHERE deleted_at IS NOT NULL")
                .execute()
                .map(RowSet::rowCount);
    }

    private Product mapSingleOrNull(RowSet<Row> rows) {
        return rows.iterator().hasNext() ? Product.fromRow(rows.iterator().next()) : null;
    }
}
