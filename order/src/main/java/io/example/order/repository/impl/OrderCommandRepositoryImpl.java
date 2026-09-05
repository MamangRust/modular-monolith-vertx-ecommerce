package io.example.order.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.order.domain.requests.CreateOrderRecordRequest;
import io.example.order.domain.requests.UpdateOrderRecordRequest;
import io.example.order.model.Order;
import io.example.order.repository.OrderCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderCommandRepositoryImpl implements OrderCommandRepository {
    private final Pool client;

    @Override
    public Future<Order> createOrder(CreateOrderRecordRequest req) {
        return client.preparedQuery("""
                INSERT INTO orders (merchant_id, user_id, total_price, created_at, updated_at)
                VALUES ($1, $2, $3, NOW(), NOW())
                RETURNING order_id, merchant_id, user_id, total_price, created_at, updated_at
                """)
                .execute(io.vertx.sqlclient.Tuple.of(req.getMerchantId(), req.getUserId(), req.getTotalPrice()))
                .map(rows -> rows.iterator().hasNext() ? Order.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Order> updateOrder(UpdateOrderRecordRequest req) {
        return client.preparedQuery("""
                UPDATE orders
                SET total_price = COALESCE(NULLIF($1::INT, 0), total_price), updated_at = NOW()
                WHERE order_id = $2 AND deleted_at IS NULL
                RETURNING order_id, merchant_id, user_id, total_price, created_at, updated_at
                """)
                .execute(io.vertx.sqlclient.Tuple.of(req.getTotalPrice() != null ? req.getTotalPrice() : 0, req.getOrderId()))
                .map(rows -> rows.iterator().hasNext() ? Order.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Order> trashOrder(Long orderId) {
        return client.preparedQuery("""
                UPDATE orders
                SET deleted_at = NOW()
                WHERE order_id = $1 AND deleted_at IS NULL
                RETURNING order_id, merchant_id, user_id, total_price, created_at, updated_at, deleted_at
                """)
                .execute(io.vertx.sqlclient.Tuple.of(orderId))
                .map(rows -> rows.iterator().hasNext() ? Order.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Order> restoreOrder(Long orderId) {
        return client.preparedQuery("""
                UPDATE orders
                SET deleted_at = NULL
                WHERE order_id = $1 AND deleted_at IS NOT NULL
                RETURNING order_id, merchant_id, user_id, total_price, created_at, updated_at, deleted_at
                """)
                .execute(io.vertx.sqlclient.Tuple.of(orderId))
                .map(rows -> rows.iterator().hasNext() ? Order.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Boolean> deleteOrderPermanently(Long orderId) {
        return client.preparedQuery("""
                DELETE FROM orders
                WHERE order_id = $1
                  AND deleted_at IS NOT NULL
                """)
                .execute(io.vertx.sqlclient.Tuple.of(orderId))
                .map(rows -> rows.rowCount() > 0);
    }

    @Override
    public Future<List<Order>> findAllTrashed() {
        return client.preparedQuery("""
                SELECT
                    order_id,
                    user_id,
                    merchant_id,
                    total_price,
                    created_at,
                    updated_at,
                    deleted_at
                FROM orders
                WHERE
                    deleted_at IS NOT NULL
                ORDER BY order_id
                """)
                .execute()
                .map(this::mapOrders);
    }

    private List<Order> mapOrders(RowSet<Row> rows) {
        List<Order> orders = new ArrayList<>();
        for (Row row : rows) {
            orders.add(Order.fromRow(row));
        }
        return orders;
    }

    @Override
    public Future<Integer> deleteAllPermanentOrders() {
        return client.preparedQuery("""
                DELETE FROM orders
                WHERE deleted_at IS NOT NULL
                """)
                .execute()
                .map(SqlResult::rowCount);
    }
}
