package io.example.order_item.repository.impl;

import java.util.ArrayList;
import java.util.List;
import io.example.order_item.domain.requests.CreateOrderItemRecordRequest;
import io.example.order_item.domain.requests.UpdateOrderItemRecordRequest;
import io.example.order_item.model.OrderItem;
import io.example.order_item.repository.OrderItemCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderItemCommandRepositoryImpl implements OrderItemCommandRepository {
    private final Pool client;

    @Override
    public Future<OrderItem> createOrderItem(CreateOrderItemRecordRequest req) {
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
                .execute(Tuple.of(req.getOrderId(), req.getProductId(), req.getQuantity(), req.getPrice()))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<OrderItem> updateOrderItem(UpdateOrderItemRecordRequest req) {
        return client
                .preparedQuery("""
                        UPDATE order_items
                        SET
                            quantity = COALESCE(NULLIF($2::INT, 0), quantity),
                            price = COALESCE(NULLIF($3::INT, 0), price),
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
                .execute(Tuple.of(req.getOrderItemId() != null ? req.getOrderItemId().longValue() : null,
                        req.getQuantity() != null ? req.getQuantity() : 0, req.getPrice() != null ? req.getPrice() : 0))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<List<OrderItem>> trashOrderItem(Long orderId) {
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
    public Future<List<OrderItem>> restoreOrderItem(Long orderId) {
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
    public Future<Boolean> deleteOrderItemPermanently(Long orderItemId) {
        return client
                .preparedQuery("""
                        DELETE FROM order_items
                        WHERE
                            order_item_id = $1
                            AND deleted_at IS NOT NULL
                        """)
                .execute(Tuple.of(orderItemId))
                .map(rows -> rows.rowCount() > 0);
    }

    @Override
    public Future<Boolean> deleteOrderItemByOrderPermanent(Long orderId) {
        return client
                .preparedQuery("""
                        DELETE FROM order_items
                        WHERE
                            order_id = $1
                        """)
                .execute(Tuple.of(orderId))
                .map(rows -> rows.rowCount() > 0);
    }

    @Override
    public Future<Integer> restoreAllOrderItems() {
        return client
                .preparedQuery("""
                        UPDATE order_items
                        SET
                            deleted_at = NULL
                        WHERE
                            deleted_at IS NOT NULL
                        """)
                .execute()
                .map(RowSet::rowCount);
    }

    @Override
    public Future<Integer> deleteAllPermanentOrderItems() {
        return client
                .preparedQuery("DELETE FROM order_items WHERE deleted_at IS NOT NULL")
                .execute()
                .map(RowSet::rowCount);
    }

    @Override
    public Future<Integer> calculateTotalPrice(Long orderId) {
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
