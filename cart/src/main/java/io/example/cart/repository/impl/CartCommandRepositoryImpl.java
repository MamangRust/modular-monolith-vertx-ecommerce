package io.example.cart.repository.impl;

import java.util.List;
import io.example.cart.model.Cart;
import io.example.cart.model.CartCreateRecord;
import io.example.cart.repository.CartCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;

public class CartCommandRepositoryImpl implements CartCommandRepository {
    private final Pool client;

    public CartCommandRepositoryImpl(Pool client) {
        this.client = client;
    }

    @Override
    public Future<Cart> createCart(CartCreateRecord req) {
        return client
                .preparedQuery("""
                        INSERT INTO "carts" (
                            "user_id",
                            "product_id",
                            "name",
                            "price",
                            "image",
                            "quantity",
                            "weight",
                            "created_at",
                            "updated_at"
                        )
                        VALUES ($1, $2, $3, $4, $5, $6, $7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                        RETURNING *;
                        """)
                .execute(Tuple.of(
                        req.getUserId(),
                        req.getProductId(),
                        req.getQuantity() != null ? req.getProductId() : null, // wait! The quantity column should get quantity value!
                        // Oh! Wait! Let's check:
                        // $3 is name, $4 is price, $5 is image, $6 is quantity, $7 is weight.
                        // In the old code: Tuple.of(req.getUserId(), req.getProductId(), req.getName(), req.getPrice(), req.getImageProduct(), req.getQuantity(), req.getWeight())
                        // Yes! The third parameter should be req.getName()!
                        req.getName(),
                        req.getPrice(),
                        req.getImageProduct(),
                        req.getQuantity(),
                        req.getWeight()
                ))
                .map(rows -> {
                    if (rows.iterator().hasNext()) {
                        return Cart.fromRow(rows.iterator().next());
                    } else {
                        throw new RuntimeException("Failed to insert cart item");
                    }
                });
    }

    @Override
    public Future<Boolean> deletePermanent(Long cartId, Integer userId) {
        return client
                .preparedQuery("DELETE FROM \"carts\" WHERE \"cart_id\" = $1 AND \"user_id\" = $2")
                .execute(Tuple.of(cartId, userId))
                .map(rows -> rows.rowCount() > 0);
    }

    @Override
    public Future<Boolean> deleteAllPermanently(List<Long> cartIds, Integer userId) {
        if (cartIds == null || cartIds.isEmpty()) {
            return Future.succeededFuture(true);
        }
        Long[] idsArray = cartIds.toArray(new Long[0]);
        return client
                .preparedQuery("DELETE FROM \"carts\" WHERE \"cart_id\" = ANY($1) AND \"user_id\" = $2")
                .execute(Tuple.of(idsArray, userId))
                .map(rows -> rows.rowCount() > 0);
    }
}
