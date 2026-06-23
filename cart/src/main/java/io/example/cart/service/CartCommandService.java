package io.example.cart.service;

import io.example.cart.model.CartResponse;
import io.example.cart.domain.requests.CreateCartRequest;
import io.example.cart.domain.requests.DeleteCartRequest;
import io.vertx.core.Future;

public interface CartCommandService {
    Future<CartResponse> create(CreateCartRequest req);

    Future<Boolean> deletePermanent(DeleteCartRequest req);

    Future<Boolean> deleteAll(DeleteCartRequest req);
}