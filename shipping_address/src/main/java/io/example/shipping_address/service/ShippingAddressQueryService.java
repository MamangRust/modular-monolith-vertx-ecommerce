package io.example.shipping_address.service;

import io.example.common.domain.PagedResult;
import io.example.shipping_address.domain.requests.FindAllShippingAddress;
import io.example.shipping_address.model.ShippingAddressResponse;
import io.example.shipping_address.model.ShippingAddressResponseDeleteAt;
import io.vertx.core.Future;

public interface ShippingAddressQueryService {
        Future<PagedResult<ShippingAddressResponse>> getAllShippingAddresses(FindAllShippingAddress req);

        Future<PagedResult<ShippingAddressResponseDeleteAt>> getActiveShippingAddresses(FindAllShippingAddress req);

        Future<PagedResult<ShippingAddressResponseDeleteAt>> getTrashedShippingAddresses(FindAllShippingAddress req);

        Future<ShippingAddressResponse> getShippingAddressById(Long shippingAddressId);

        Future<ShippingAddressResponse> getShippingAddressByOrderId(Long orderId);
}