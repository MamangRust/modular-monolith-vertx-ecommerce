package io.example.order_item.repository.impl;

import java.util.ArrayList;
import java.util.List;
import io.example.common.domain.PagedResult;
import io.example.order_item.model.OrderItem;
import io.example.order_item.repository.OrderItemQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class OrderItemQueryRepositoryImpl implements OrderItemQueryRepository {
    private final Pool client;

    public OrderItemQueryRepositoryImpl(Pool client) {
        this.client = client;
    }

    @Override
    public Future<PagedResult<OrderItem>> getOrderItems(String search, int page, int pageSize) {
        int offset = (page > 0 ? page - 1 : 0) * pageSize;
        return client
                .preparedQuery("""
                        SELECT
                            order_item_id,
                            order_id,
                            product_id,
                            quantity,
                            price,
                            created_at,
                            updated_at,
                            deleted_at,
                            COUNT(*) OVER () AS total_count
                        FROM order_items
                        WHERE
                            deleted_at IS NULL
                            AND (
                                $1::TEXT IS NULL
                                OR order_id::TEXT ILIKE '%' || $1 || '%'
                                OR product_id::TEXT ILIKE '%' || $1 || '%'
                            )
                        ORDER BY created_at DESC
                        LIMIT $2
                        OFFSET $3
                        """)
                .execute(Tuple.of(normalizeSearch(search), pageSize, offset))
                .map(this::mapPagedOrderItems);
    }

    @Override
    public Future<PagedResult<OrderItem>> getOrderItemsActive(String search, int page, int pageSize) {
        int offset = (page > 0 ? page - 1 : 0) * pageSize;
        return client
                .preparedQuery("""
                        SELECT
                            order_item_id,
                            order_id,
                            product_id,
                            quantity,
                            price,
                            created_at,
                            updated_at,
                            deleted_at,
                            COUNT(*) OVER () AS total_count
                        FROM order_items
                        WHERE
                            deleted_at IS NULL
                            AND (
                                $1::TEXT IS NULL
                                OR order_id::TEXT ILIKE '%' || $1 || '%'
                                OR product_id::TEXT ILIKE '%' || $1 || '%'
                            )
                        ORDER BY created_at DESC
                        LIMIT $2
                        OFFSET $3
                        """)
                .execute(Tuple.of(normalizeSearch(search), pageSize, offset))
                .map(this::mapPagedOrderItems);
    }

    @Override
    public Future<PagedResult<OrderItem>> getOrderItemsTrashed(String search, int page, int pageSize) {
        int offset = (page > 0 ? page - 1 : 0) * pageSize;
        return client
                .preparedQuery("""
                        SELECT
                            order_item_id,
                            order_id,
                            product_id,
                            quantity,
                            price,
                            created_at,
                            updated_at,
                            deleted_at,
                            COUNT(*) OVER () AS total_count
                        FROM order_items
                        WHERE
                            deleted_at IS NOT NULL
                            AND (
                                $1::TEXT IS NULL
                                OR order_id::TEXT ILIKE '%' || $1 || '%'
                                OR product_id::TEXT ILIKE '%' || $1 || '%'
                            )
                        ORDER BY deleted_at DESC
                        LIMIT $2
                        OFFSET $3
                        """)
                .execute(Tuple.of(normalizeSearch(search), pageSize, offset))
                .map(this::mapPagedOrderItems);
    }

    @Override
    public Future<List<OrderItem>> getOrderItemsByOrder(Integer orderId) {
        return client
                .preparedQuery("""
                        SELECT
                            order_item_id,
                            order_id,
                            product_id,
                            quantity,
                            price,
                            created_at,
                            updated_at,
                            deleted_at
                        FROM order_items
                        WHERE
                            order_id = $1
                            AND deleted_at IS NULL
                        """)
                .execute(Tuple.of(orderId))
                .map(this::mapList);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search;
    }

    private List<OrderItem> mapList(RowSet<Row> rows) {
        List<OrderItem> items = new ArrayList<>();
        for (Row row : rows) {
            items.add(OrderItem.fromRow(row));
        }
        return items;
    }

    private PagedResult<OrderItem> mapPagedOrderItems(RowSet<Row> rows) {
        List<OrderItem> items = new ArrayList<>();
        int total = 0;

        for (Row row : rows) {
            items.add(OrderItem.fromRow(row));
            if (total == 0) {
                Integer tc = row.getInteger("total_count");
                if (tc != null) {
                    total = tc;
                }
            }
        }

        return new PagedResult<>(items, total);
    }
}
