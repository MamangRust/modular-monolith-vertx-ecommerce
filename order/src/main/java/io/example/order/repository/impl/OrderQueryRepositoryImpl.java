package io.example.order.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.order.model.Order;
import io.example.order.repository.OrderQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class OrderQueryRepositoryImpl implements OrderQueryRepository {
    private final Pool client;

    public OrderQueryRepositoryImpl(Pool client) {
        this.client = client;
    }

    @Override
    public Future<PagedResult<Order>> getOrders(String search, int page, int pageSize) {
        int offset = (page > 0 ? page - 1 : 0) * pageSize;
        String normalizedSearch = (search == null || search.isBlank()) ? null : search;

        return client.preparedQuery("""
                SELECT
                    order_id,
                    user_id,
                    merchant_id,
                    total_price,
                    created_at,
                    updated_at,
                    COUNT(*) OVER () AS total_count
                FROM orders
                WHERE
                    deleted_at IS NULL
                    AND (
                        $1::TEXT IS NULL
                        OR order_id::TEXT ILIKE '%' || $1 || '%'
                    )
                ORDER BY created_at DESC
                LIMIT $2
                OFFSET $3
                """)
                .execute(Tuple.of(normalizedSearch, pageSize, offset))
                .map(this::mapPagedOrders);
    }

    @Override
    public Future<PagedResult<Order>> getOrdersActive(String search, int page, int pageSize) {
        int offset = (page > 0 ? page - 1 : 0) * pageSize;
        String normalizedSearch = (search == null || search.isBlank()) ? null : search;

        return client.preparedQuery("""
                SELECT
                    order_id,
                    user_id,
                    merchant_id,
                    total_price,
                    created_at,
                    updated_at,
                    deleted_at,
                    COUNT(*) OVER () AS total_count
                FROM orders
                WHERE
                    deleted_at IS NULL
                    AND (
                        $1::TEXT IS NULL
                        OR order_id::TEXT ILIKE '%' || $1 || '%'
                    )
                ORDER BY created_at DESC
                LIMIT $2
                OFFSET $3
                """)
                .execute(Tuple.of(normalizedSearch, pageSize, offset))
                .map(this::mapPagedOrders);
    }

    @Override
    public Future<PagedResult<Order>> getOrdersTrashed(String search, int page, int pageSize) {
        int offset = (page > 0 ? page - 1 : 0) * pageSize;
        String normalizedSearch = (search == null || search.isBlank()) ? null : search;

        return client.preparedQuery("""
                SELECT
                    order_id,
                    user_id,
                    merchant_id,
                    total_price,
                    created_at,
                    updated_at,
                    deleted_at,
                    COUNT(*) OVER () AS total_count
                FROM orders
                WHERE
                    deleted_at IS NOT NULL
                    AND (
                        $1::TEXT IS NULL
                        OR order_id::TEXT ILIKE '%' || $1 || '%'
                    )
                ORDER BY created_at DESC
                LIMIT $2
                OFFSET $3
                """)
                .execute(Tuple.of(normalizedSearch, pageSize, offset))
                .map(this::mapPagedOrders);
    }

    @Override
    public Future<PagedResult<Order>> getOrdersByMerchant(Integer merchantId, String search, int page, int pageSize) {
        int offset = (page > 0 ? page - 1 : 0) * pageSize;
        String normalizedSearch = (search == null || search.isBlank()) ? null : search;

        return client.preparedQuery("""
                SELECT
                    order_id,
                    user_id,
                    merchant_id,
                    total_price,
                    created_at,
                    updated_at,
                    COUNT(*) OVER () AS total_count
                FROM orders
                WHERE
                    deleted_at IS NULL
                    AND merchant_id = $1
                    AND (
                        $2::TEXT IS NULL
                        OR order_id::TEXT ILIKE '%' || $2 || '%'
                    )
                ORDER BY created_at DESC
                LIMIT $3
                OFFSET $4
                """)
                .execute(Tuple.of(merchantId, normalizedSearch, pageSize, offset))
                .map(this::mapPagedOrders);
    }

    @Override
    public Future<Order> getOrderById(Long orderId) {
        return client.preparedQuery("""
                SELECT
                    order_id,
                    user_id,
                    merchant_id,
                    total_price,
                    created_at,
                    updated_at
                FROM orders
                WHERE
                    order_id = $1
                    AND deleted_at IS NULL
                """)
                .execute(Tuple.of(orderId))
                .map(rows -> rows.iterator().hasNext() ? Order.fromRow(rows.iterator().next()) : null);
    }

    private PagedResult<Order> mapPagedOrders(RowSet<Row> rows) {
        List<Order> orders = new ArrayList<>();
        int total = 0;

        for (Row row : rows) {
            orders.add(Order.fromRow(row));
            if (total == 0) {
                Integer tc = row.getInteger("total_count");
                if (tc != null) {
                    total = tc;
                }
            }
        }

        return new PagedResult<>(orders, total);
    }
}
