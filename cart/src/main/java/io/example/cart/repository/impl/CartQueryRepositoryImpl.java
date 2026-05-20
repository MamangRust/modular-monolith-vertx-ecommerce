package io.example.cart.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.cart.model.Cart;
import io.example.cart.repository.CartQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class CartQueryRepositoryImpl implements CartQueryRepository {
    private final Pool client;

    public CartQueryRepositoryImpl(Pool client) {
        this.client = client;
    }

    @Override
    public Future<PagedResult<Cart>> getCarts(Integer userId, String search, int page, int pageSize) {
        int offset = (page > 0 ? page - 1 : 0) * pageSize;

        String query = "SELECT cart_id, user_id, product_id, name, price, image, quantity, weight, created_at, updated_at, COUNT(*) OVER() AS total_count FROM carts WHERE user_id = $1";
        List<Object> params = new ArrayList<>();
        params.add(userId);

        int paramIndex = 2;
        if (search != null && !search.trim().isEmpty()) {
            query += " AND name ILIKE $" + paramIndex;
            params.add("%" + search.trim() + "%");
            paramIndex++;
        }

        query += " ORDER BY created_at DESC LIMIT $" + paramIndex + " OFFSET $" + (paramIndex + 1);
        params.add(pageSize);
        params.add(offset);

        return client.preparedQuery(query)
                .execute(Tuple.from(params))
                .map(this::mapPagedCarts);
    }

    private PagedResult<Cart> mapPagedCarts(RowSet<Row> rows) {
        List<Cart> carts = new ArrayList<>();
        int total = 0;

        for (Row row : rows) {
            carts.add(Cart.fromRow(row));
            if (total == 0) {
                Integer tc = row.getInteger("total_count");
                if (tc != null) {
                    total = tc;
                }
            }
        }

        return new PagedResult<>(carts, total);
    }
}
