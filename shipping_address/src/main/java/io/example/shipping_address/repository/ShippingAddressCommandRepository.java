package io.example.shipping_address.repository;

import io.example.shipping_address.domain.requests.CreateShippingAddressRequest;
import io.example.shipping_address.model.ShippingAddress;
import io.example.shipping_address.domain.requests.UpdateShippingAddressRequest;
import io.vertx.core.Future;

public interface ShippingAddressCommandRepository {
    Future<ShippingAddress> createShippingAddress(CreateShippingAddressRequest req);

    Future<ShippingAddress> updateShippingAddress(UpdateShippingAddressRequest req);

    Future<ShippingAddress> trashShippingAddress(Long shippingAddressId);

    Future<ShippingAddress> restoreShippingAddress(Long shippingAddressId);

    Future<Boolean> deleteShippingAddressPermanently(Long shippingAddressId);

    Future<Boolean> deleteByOrderIDPermanent(Long orderId);

    Future<Integer> restoreAllShippingAddress();

    Future<Integer> deleteAllPermanentShippingAddress();
}
