package io.example.order.repository.impl;

import io.example.order.model.CreateOrderRecord;
import io.example.order.model.UpdateOrderRecord;
import io.example.order.model.Order;
import io.example.order.repository.OrderCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlResult;

public class OrderCommandRepositoryImpl implements OrderCommandRepository {
    private final Pool client;

    public OrderCommandRepositoryImpl(Pool client) {
        this.client = client;
    }

    @Override
    public Future<Order> createOrder(CreateOrderRecord req) {
        return client.preparedQuery("""
                INSERT INTO orders (merchant_id, user_id, total_price, created_at, updated_at)
                VALUES ($1, $2, $3, NOW(), NOW())
                RETURNING order_id, merchant_id, user_id, total_price, created_at, updated_at
                """)
                .execute(io.vertx.sqlclient.Tuple.of(req.getMerchantId(), req.getUserId(), req.getTotalPrice()))
                .map(rows -> rows.iterator().hasNext() ? Order.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Order> updateOrder(UpdateOrderRecord req) {
        return client.preparedQuery("""
                UPDATE orders
                SET total_price = $1, updated_at = NOW()
                WHERE order_id = $2 AND deleted_at IS NULL
                RETURNING order_id, merchant_id, user_id, total_price, created_at, updated_at
                """)
                .execute(io.vertx.sqlclient.Tuple.of(req.getTotalPrice(), req.getOrderId()))
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
    public Future<Void> deleteOrderPermanently(Long orderId) {
        return client.preparedQuery("""
                DELETE FROM orders
                WHERE order_id = $1
                """)
                .execute(io.vertx.sqlclient.Tuple.of(orderId))
                .map(rows -> null);
    }

    @Override
    public Future<Integer> restoreAllOrders() {
        return client.preparedQuery("""
                UPDATE orders
                SET deleted_at = NULL
                WHERE deleted_at IS NOT NULL
                """)
                .execute()
                .map(SqlResult::rowCount);
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
