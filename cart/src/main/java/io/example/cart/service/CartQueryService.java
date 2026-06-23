package io.example.cart.service;

import io.example.common.domain.PagedResult;
import io.example.cart.model.CartResponse;
import io.example.cart.domain.requests.FindAllCartsRequest;
import io.vertx.core.Future;

public interface CartQueryService {
    Future<PagedResult<CartResponse>> findAll(FindAllCartsRequest req);
}