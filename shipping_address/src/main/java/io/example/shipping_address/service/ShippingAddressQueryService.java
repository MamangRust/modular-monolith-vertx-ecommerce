package io.example.shipping_address.service;

import java.util.List;

import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.shipping_address.model.FindAllShippingAddress;
import io.example.shipping_address.model.ShippingAddressResponse;
import io.example.shipping_address.model.ShippingAddressResponseDeleteAt;
import io.vertx.core.Future;

public interface ShippingAddressQueryService {
    Future<ApiResponsePagination<List<ShippingAddressResponse>>> getAllShippingAddresses(FindAllShippingAddress req);
    Future<ApiResponsePagination<List<ShippingAddressResponseDeleteAt>>> getActiveShippingAddresses(FindAllShippingAddress req);
    Future<ApiResponsePagination<List<ShippingAddressResponseDeleteAt>>> getTrashedShippingAddresses(FindAllShippingAddress req);
    Future<ApiResponse<ShippingAddressResponse>> getShippingAddressById(Integer shippingAddressId);
    Future<ApiResponse<ShippingAddressResponse>> getShippingAddressByOrderId(Integer orderId);
}
