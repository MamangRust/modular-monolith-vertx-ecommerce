package io.example.shipping_address.service;

import io.example.common.model.ApiResponse;
import io.example.shipping_address.model.CreateShippingAddressRequest;
import io.example.shipping_address.model.ShippingAddressResponse;
import io.example.shipping_address.model.ShippingAddressResponseDeleteAt;
import io.example.shipping_address.model.UpdateShippingAddressRequest;
import io.vertx.core.Future;

public interface ShippingAddressCommandService {
    Future<ApiResponse<ShippingAddressResponse>> createShippingAddress(CreateShippingAddressRequest req);
    Future<ApiResponse<ShippingAddressResponse>> updateShippingAddress(UpdateShippingAddressRequest req);
    Future<ApiResponse<ShippingAddressResponseDeleteAt>> trashShippingAddress(Integer shippingAddressId);
    Future<ApiResponse<ShippingAddressResponseDeleteAt>> restoreShippingAddress(Integer shippingAddressId);
    Future<ApiResponse<Void>> deleteShippingAddressPermanently(Integer shippingAddressId);
    Future<ApiResponse<Void>> deleteShippingAddressByOrderPermanent(Integer orderId);
    Future<ApiResponse<Void>> restoreAllShippingAddresses();
    Future<ApiResponse<Void>> deleteAllPermanentShippingAddresses();
}
