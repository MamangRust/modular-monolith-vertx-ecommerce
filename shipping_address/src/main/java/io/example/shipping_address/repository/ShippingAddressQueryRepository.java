package io.example.shipping_address.repository;

import io.example.common.domain.PagedResult;
import io.example.shipping_address.model.FindAllShippingAddress;
import io.example.shipping_address.model.ShippingAddress;
import io.vertx.core.Future;

public interface ShippingAddressQueryRepository {
    Future<PagedResult<ShippingAddress>> getShippingAddresses(FindAllShippingAddress req);
    Future<PagedResult<ShippingAddress>> getShippingAddressActive(FindAllShippingAddress req);
    Future<PagedResult<ShippingAddress>> getShippingAddressTrashed(FindAllShippingAddress req);
    Future<ShippingAddress> getShippingByID(Integer shippingAddressId);
    Future<ShippingAddress> getShippingAddressByOrderID(Integer orderId);
}
