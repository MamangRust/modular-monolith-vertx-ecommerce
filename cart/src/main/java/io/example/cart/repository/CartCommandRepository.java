package io.example.cart.repository;

import java.util.List;
import io.example.cart.model.Cart;
import io.example.cart.model.CartCreateRecord;
import io.vertx.core.Future;

public interface CartCommandRepository {
    Future<Cart> createCart(CartCreateRecord req);
    Future<Boolean> deletePermanent(Long cartId, Integer userId);
    Future<Boolean> deleteAllPermanently(List<Long> cartIds, Integer userId);
}
