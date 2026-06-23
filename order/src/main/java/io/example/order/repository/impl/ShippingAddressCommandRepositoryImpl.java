package io.example.order.repository.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

import com.google.protobuf.Empty;
import io.example.order.model.ShippingAddress;
import io.example.order.repository.ShippingAddressCommandRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.shipping_address.ShippingAddressCommon.FindByIdShippingRequest;
import pb.shipping_address.ShippingAddressCommand.CreateShippingAddressRequest;
import pb.shipping_address.ShippingAddressCommand.UpdateShippingAddressRequest;
import pb.shipping_address.VertxShippingCommandServiceGrpcClient;
import pb.shipping_address.VertxShippingQueryServiceGrpcClient;

@RequiredArgsConstructor
public class ShippingAddressCommandRepositoryImpl implements ShippingAddressCommandRepository {
    private final VertxShippingCommandServiceGrpcClient commandClient;
    private final VertxShippingQueryServiceGrpcClient queryClient;

    @Override
    public Future<ShippingAddress> createShippingAddress(
            io.example.order.domain.requests.CreateShippingAddressRequest req) {
        CreateShippingAddressRequest request = CreateShippingAddressRequest.newBuilder()
                .setOrderId(req.getOrderId().intValue())
                .setAlamat(req.getAlamat())
                .setProvinsi(req.getProvinsi())
                .setNegara(req.getNegara())
                .setKota(req.getKota())
                .setCourier(req.getCourier())
                .setShippingMethod(req.getShippingMethod())
                .setShippingCost(req.getShippingCost())
                .build();

        return commandClient.createShipping(request)
                .map(response -> {
                    if (response != null && response.hasData()) {
                        var data = response.getData();
                        return ShippingAddress.builder()
                                .shippingAddressId((long) data.getId())
                                .orderId(data.getOrderId())
                                .alamat(data.getAlamat())
                                .provinsi(data.getProvinsi())
                                .negara(data.getNegara())
                                .kota(data.getKota())
                                .courier(req.getCourier())
                                .shippingMethod(data.getShippingMethod())
                                .shippingCost(data.getShippingCost())
                                .createdAt(parseTimestamp(data.getCreatedAt()))
                                .updatedAt(parseTimestamp(data.getUpdatedAt()))
                                .build();
                    }
                    return null;
                });
    }

    @Override
    public Future<ShippingAddress> updateShippingAddress(
            io.example.order.domain.requests.UpdateShippingAddressRequest req) {
        UpdateShippingAddressRequest request = UpdateShippingAddressRequest.newBuilder()
                .setShippingId(req.getShippingId().intValue())
                .setOrderId(req.getOrderId().intValue())
                .setAlamat(req.getAlamat())
                .setProvinsi(req.getProvinsi())
                .setNegara(req.getNegara())
                .setKota(req.getKota())
                .setCourier(req.getCourier())
                .setShippingMethod(req.getShippingMethod())
                .setShippingCost(req.getShippingCost())
                .build();

        return commandClient.updateShipping(request)
                .map(response -> {
                    if (response != null && response.hasData()) {
                        var data = response.getData();
                        return ShippingAddress.builder()
                                .shippingAddressId((long) data.getId())
                                .orderId(data.getOrderId())
                                .alamat(data.getAlamat())
                                .provinsi(data.getProvinsi())
                                .negara(data.getNegara())
                                .kota(data.getKota())
                                .courier(req.getCourier())
                                .shippingMethod(data.getShippingMethod())
                                .shippingCost(data.getShippingCost())
                                .createdAt(parseTimestamp(data.getCreatedAt()))
                                .updatedAt(parseTimestamp(data.getUpdatedAt()))
                                .build();
                    }
                    return null;
                });
    }

    @Override
    public Future<ShippingAddress> getShippingAddressByOrderID(Long orderId) {
        FindByIdShippingRequest request = FindByIdShippingRequest.newBuilder()
                .setId(orderId.intValue())
                .build();

        return queryClient.findByOrder(request)
                .map(response -> {
                    if (response != null && response.hasData()) {
                        var data = response.getData();
                        return ShippingAddress.builder()
                                .shippingAddressId((long) data.getId())
                                .orderId(data.getOrderId())
                                .alamat(data.getAlamat())
                                .provinsi(data.getProvinsi())
                                .negara(data.getNegara())
                                .kota(data.getKota())
                                .courier("") // Default empty as not returned in proto response
                                .shippingMethod(data.getShippingMethod())
                                .shippingCost(data.getShippingCost())
                                .createdAt(parseTimestamp(data.getCreatedAt()))
                                .updatedAt(parseTimestamp(data.getUpdatedAt()))
                                .build();
                    }
                    return null;
                });
    }

    @Override
    public Future<Void> deleteShippingAddressPermanently(Long orderId) {
        FindByIdShippingRequest request = FindByIdShippingRequest.newBuilder()
                .setId(orderId.intValue())
                .build();

        return commandClient.deleteShippingByOrderPermanent(request)
                .mapEmpty();
    }

    @Override
    public Future<Void> deleteAllShippingAddress() {
        return commandClient.deleteAllShippingPermanent(Empty.getDefaultInstance())
                .mapEmpty();
    }

    private Timestamp parseTimestamp(String ts) {
        if (ts == null || ts.isBlank())
            return null;
        try {
            return Timestamp.from(Instant.parse(ts));
        } catch (Exception e) {
            try {
                return Timestamp.valueOf(LocalDateTime.parse(ts.replace(" ", "T")));
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
