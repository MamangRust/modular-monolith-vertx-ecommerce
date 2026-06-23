package io.example.order_item.repository.impl;

import java.util.ArrayList;
import java.util.List;
import io.example.common.domain.PagedResult;
import io.example.order_item.domain.requests.FindAllOrderItemRequest;
import io.example.order_item.model.OrderItem;
import io.example.order_item.repository.OrderItemQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderItemQueryRepositoryImpl implements OrderItemQueryRepository {
    private final Pool client;

    @Override
    public Future<PagedResult<OrderItem>> getOrderItems(FindAllOrderItemRequest req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();
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
                .execute(Tuple.of(normalizeSearch(req.getSearch()), req.getPageSize(), offset))
                .map(this::mapPagedOrderItems);
    }

    @Override
    public Future<PagedResult<OrderItem>> getOrderItemsActive(FindAllOrderItemRequest req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();
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
                .execute(Tuple.of(normalizeSearch(req.getSearch()), req.getPageSize(), offset))
                .map(this::mapPagedOrderItems);
    }

    @Override
    public Future<PagedResult<OrderItem>> getOrderItemsTrashed(FindAllOrderItemRequest req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();
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
                .execute(Tuple.of(normalizeSearch(req.getSearch()), req.getPageSize(), offset))
                .map(this::mapPagedOrderItems);
    }

    @Override
    public Future<List<OrderItem>> getOrderItemsByOrder(Long orderId) {
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

    @Override
    public Future<OrderItem> findByTrashedId(Long orderItemId) {
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
                            order_item_id = $1
                            AND deleted_at IS NOT NULL
                        """)
                .execute(Tuple.of(orderItemId))
                .map(this::map);
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

    private OrderItem map(RowSet<Row> rows) {
        if (rows.rowCount() == 0) {
            return null;
        }
        return OrderItem.fromRow(rows.iterator().next());
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
