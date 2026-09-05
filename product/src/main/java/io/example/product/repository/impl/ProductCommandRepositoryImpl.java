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
                            category_id = COALESCE(NULLIF($2, 0), category_id),
                            name = COALESCE(NULLIF($3, ''), name),
                            description = COALESCE(NULLIF($4, ''), description),
                            price = COALESCE(NULLIF($5, 0), price),
                            count_in_stock = COALESCE(NULLIF($6, 0), count_in_stock),
                            brand = COALESCE(NULLIF($7, ''), brand),
                            weight = COALESCE(NULLIF($8, 0), weight),
                            rating = COALESCE(NULLIF($9, 0.0), rating),
                            slug_product = COALESCE(NULLIF($10, ''), slug_product),
                            image_product = COALESCE(NULLIF($11, ''), image_product),
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
    public Future<Product> decrementStock(Integer productId, Integer quantity) {
        return client
                .preparedQuery("""
                        UPDATE products
                        SET
                            count_in_stock = count_in_stock - $2,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE
                            product_id = $1
                            AND count_in_stock >= $2
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
                .execute(Tuple.of(productId, quantity))
                .map(rows -> {
                    if (!rows.iterator().hasNext()) {
                        throw new io.example.common.exception.grpc.BadRequestException(
                                "Insufficient stock for product ID: " + productId);
                    }
                    return Product.fromRow(rows.iterator().next());
                });
    }

    @Override
    public Future<Product> incrementStock(Integer productId, Integer quantity) {
        return client
                .preparedQuery("""
                        UPDATE products
                        SET count_in_stock = count_in_stock + $2,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE product_id = $1
                          AND deleted_at IS NULL
                        RETURNING product_id, merchant_id, category_id, name, description, price,
                                  count_in_stock, brand, weight, rating, slug_product, image_product,
                                  created_at, updated_at
                        """)
                .execute(Tuple.of(productId, quantity))
                .map(rows -> {
                    if (!rows.iterator().hasNext()) {
                        throw new io.example.common.exception.grpc.NotFoundException(
                                "Product not found: " + productId);
                    }
                    return Product.fromRow(rows.iterator().next());
                });
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
