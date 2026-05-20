package io.example.shipping_address.repository;

import io.example.shipping_address.model.CreateShippingAddressRequest;
import io.example.shipping_address.model.ShippingAddress;
import io.example.shipping_address.model.UpdateShippingAddressRequest;
import io.vertx.core.Future;

public interface ShippingAddressCommandRepository {
    Future<ShippingAddress> createShippingAddress(CreateShippingAddressRequest req);
    Future<ShippingAddress> updateShippingAddress(UpdateShippingAddressRequest req);
    Future<ShippingAddress> trashShippingAddress(Integer shippingAddressId);
    Future<ShippingAddress> restoreShippingAddress(Integer shippingAddressId);
    Future<Void> deleteShippingAddressPermanently(Integer shippingAddressId);
    Future<Void> deleteByOrderIDPermanent(Integer orderId);
    Future<Void> restoreAllShippingAddress();
    Future<Void> deleteAllPermanentShippingAddress();
}
