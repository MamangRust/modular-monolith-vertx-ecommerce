package io.example.transaction.repository;

import io.example.transaction.model.ShippingAddress;
import io.vertx.core.Future;

public interface ShippingAddressQueryRepository {
    Future<ShippingAddress> findByOrderId(Integer orderId);
}
