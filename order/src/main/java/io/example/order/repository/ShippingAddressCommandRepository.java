package io.example.order.repository;

import io.example.order.model.ShippingAddress;
import io.vertx.core.Future;

public interface ShippingAddressCommandRepository {
    Future<ShippingAddress> createShippingAddress(Long orderId, String alamat, String provinsi, String negara, String kota, String courier, String shippingMethod, Integer shippingCost);
    Future<ShippingAddress> updateShippingAddress(Long shippingId, String alamat, String provinsi, String negara, String kota, String courier, String shippingMethod, Integer shippingCost);
    Future<ShippingAddress> getShippingAddressByOrderID(Integer orderId);
    Future<Void> deleteShippingAddressPermanently(Integer orderId);
    Future<Void> deleteAllShippingAddress();
}
