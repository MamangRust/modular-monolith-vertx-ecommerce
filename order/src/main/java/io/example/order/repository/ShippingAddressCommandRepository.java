package io.example.order.repository;

import io.example.order.domain.requests.CreateShippingAddressRequest;
import io.example.order.domain.requests.UpdateShippingAddressRequest;
import io.example.order.model.ShippingAddress;
import io.vertx.core.Future;

public interface ShippingAddressCommandRepository {
    Future<ShippingAddress> createShippingAddress(CreateShippingAddressRequest req);

    Future<ShippingAddress> updateShippingAddress(UpdateShippingAddressRequest req);

    Future<ShippingAddress> getShippingAddressByOrderID(Long orderId);

    Future<Void> deleteShippingAddressPermanently(Long orderId);

    Future<Void> deleteAllShippingAddress();
}
