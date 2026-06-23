package io.example.shipping_address.service;

import io.example.shipping_address.domain.requests.CreateShippingAddressRequest;
import io.example.shipping_address.domain.requests.UpdateShippingAddressRequest;
import io.example.shipping_address.model.ShippingAddressResponse;
import io.example.shipping_address.model.ShippingAddressResponseDeleteAt;
import io.vertx.core.Future;

public interface ShippingAddressCommandService {
    Future<ShippingAddressResponse> createShippingAddress(CreateShippingAddressRequest req);

    Future<ShippingAddressResponse> updateShippingAddress(UpdateShippingAddressRequest req);

    Future<ShippingAddressResponseDeleteAt> trashShippingAddress(Long shippingAddressId);

    Future<ShippingAddressResponseDeleteAt> restoreShippingAddress(Long shippingAddressId);

    Future<Void> deleteShippingAddressPermanently(Long shippingAddressId);

    Future<Void> deleteShippingAddressByOrderPermanent(Long orderId);

    Future<Void> restoreAllShippingAddresses();

    Future<Void> deleteAllPermanentShippingAddresses();
}