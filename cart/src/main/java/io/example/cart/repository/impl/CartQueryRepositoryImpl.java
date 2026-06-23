package io.example.cart.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.cart.model.Cart;
import io.example.cart.repository.CartQueryRepository;
import io.example.cart.domain.requests.FindAllCartsRequest;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CartQueryRepositoryImpl implements CartQueryRepository {
    private final Pool client;

    @Override
    public Future<PagedResult<Cart>> getCarts(FindAllCartsRequest request) {
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;
        int offset = (page - 1) * pageSize;
        Integer userId = request.getUserId();
        String search = request.getSearch();

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
