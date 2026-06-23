package io.example.cart.repository;

import io.example.common.domain.PagedResult;
import io.example.cart.model.Cart;
import io.example.cart.domain.requests.FindAllCartsRequest;
import io.vertx.core.Future;

public interface CartQueryRepository {
    Future<PagedResult<Cart>> getCarts(FindAllCartsRequest request);
}
