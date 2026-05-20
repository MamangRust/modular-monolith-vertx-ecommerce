package io.example.order_item.repository.impl;

import java.util.ArrayList;
import java.util.List;
import io.example.order_item.model.OrderItem;
import io.example.order_item.repository.OrderItemCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class OrderItemCommandRepositoryImpl implements OrderItemCommandRepository {
    private final Pool client;

    public OrderItemCommandRepositoryImpl(Pool client) {
        this.client = client;
    }

    @Override
    public Future<OrderItem> createOrderItem(Integer orderId, Integer productId, Integer quantity, Integer price) {
        return client
                .preparedQuery("""
                        INSERT INTO order_items (
                            order_id,
                            product_id,
                            quantity,
                            price
                        )
                        VALUES ($1, $2, $3, $4)
                        RETURNING
                            order_item_id,
                            order_id,
                            product_id,
                            quantity,
                            price,
                            created_at,
                            updated_at,
                            deleted_at
                        """)
                .execute(Tuple.of(orderId, productId, quantity, price))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<OrderItem> updateOrderItem(Integer orderItemId, Integer quantity, Integer price) {
        return client
                .preparedQuery("""
                        UPDATE order_items
                        SET
                            quantity = $2,
                            price = $3,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE
                            order_item_id = $1
                            AND deleted_at IS NULL
                        RETURNING
                            order_item_id,
                            order_id,
                            product_id,
                            quantity,
                            price,
                            created_at,
                            updated_at,
                            deleted_at
                        """)
                .execute(Tuple.of(orderItemId != null ? orderItemId.longValue() : null, quantity, price))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<List<OrderItem>> trashOrderItem(Integer orderId) {
        return client
                .preparedQuery("""
                        UPDATE order_items
                        SET
                            deleted_at = CURRENT_TIMESTAMP
                        WHERE
                            order_id = $1
                            AND deleted_at IS NULL
                        RETURNING
                            order_item_id,
                            order_id,
                            product_id,
                            quantity,
                            price,
                            created_at,
                            updated_at,
                            deleted_at
                        """)
                .execute(Tuple.of(orderId))
                .map(this::mapList);
    }

    @Override
    public Future<List<OrderItem>> restoreOrderItem(Integer orderId) {
        return client
                .preparedQuery("""
                        UPDATE order_items
                        SET
                            deleted_at = NULL
                        WHERE
                            order_id = $1
                            AND deleted_at IS NOT NULL
                        RETURNING
                            order_item_id,
                            order_id,
                            product_id,
                            quantity,
                            price,
                            created_at,
                            updated_at,
                            deleted_at
                        """)
                .execute(Tuple.of(orderId))
                .map(this::mapList);
    }

    @Override
    public Future<Void> deleteOrderItemPermanently(Integer orderItemId) {
        return client
                .preparedQuery("""
                        DELETE FROM order_items
                        WHERE
                            order_item_id = $1
                            AND deleted_at IS NOT NULL
                        """)
                .execute(Tuple.of(orderItemId != null ? orderItemId.longValue() : null))
                .mapEmpty();
    }

    @Override
    public Future<Void> deleteOrderItemByOrderPermanent(Integer orderId) {
        return client
                .preparedQuery("""
                        DELETE FROM order_items
                        WHERE
                            order_id = $1
                            AND deleted_at IS NOT NULL
                        """)
                .execute(Tuple.of(orderId))
                .mapEmpty();
    }

    @Override
    public Future<Void> restoreAllOrderItems() {
        return client
                .preparedQuery("""
                        UPDATE order_items
                        SET
                            deleted_at = NULL
                        WHERE
                            deleted_at IS NOT NULL
                        """)
                .execute()
                .mapEmpty();
    }

    @Override
    public Future<Void> deleteAllPermanentOrderItems() {
        return client
                .preparedQuery("DELETE FROM order_items WHERE deleted_at IS NOT NULL")
                .execute()
                .mapEmpty();
    }

    @Override
    public Future<Integer> calculateTotalPrice(Integer orderId) {
        return client
                .preparedQuery("""
                        SELECT COALESCE(SUM(quantity * price), 0)::int AS total_price
                        FROM order_items
                        WHERE
                            order_id = $1
                            AND deleted_at IS NULL
                        """)
                .execute(Tuple.of(orderId))
                .map(rows -> {
                    if (rows.iterator().hasNext()) {
                        return rows.iterator().next().getInteger("total_price");
                    }
                    return 0;
                });
    }

    private OrderItem mapSingleOrNull(RowSet<Row> rows) {
        return rows.iterator().hasNext() ? OrderItem.fromRow(rows.iterator().next()) : null;
    }

    private List<OrderItem> mapList(RowSet<Row> rows) {
        List<OrderItem> items = new ArrayList<>();
        for (Row row : rows) {
            items.add(OrderItem.fromRow(row));
        }
        return items;
    }
}
