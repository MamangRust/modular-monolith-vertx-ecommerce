package io.example.order.repository.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

import com.google.protobuf.Empty;
import io.example.order.model.ShippingAddress;
import io.example.order.repository.ShippingAddressCommandRepository;
import io.vertx.core.Future;
import pb.shipping_address.ShippingAddressCommon.FindByIdShippingRequest;
import pb.shipping_address.ShippingAddressCommand.CreateShippingAddressRequest;
import pb.shipping_address.ShippingAddressCommand.UpdateShippingAddressRequest;
import pb.shipping_address.VertxShippingCommandServiceGrpcClient;
import pb.shipping_address.VertxShippingQueryServiceGrpcClient;

public class ShippingAddressCommandRepositoryImpl implements ShippingAddressCommandRepository {
    private final VertxShippingCommandServiceGrpcClient commandClient;
    private final VertxShippingQueryServiceGrpcClient queryClient;

    public ShippingAddressCommandRepositoryImpl(VertxShippingCommandServiceGrpcClient commandClient, VertxShippingQueryServiceGrpcClient queryClient) {
        this.commandClient = commandClient;
        this.queryClient = queryClient;
    }

    @Override
    public Future<ShippingAddress> createShippingAddress(Long orderId, String alamat, String provinsi, String negara, String kota, String courier, String shippingMethod, Integer shippingCost) {
        CreateShippingAddressRequest request = CreateShippingAddressRequest.newBuilder()
                .setOrderId(orderId.intValue())
                .setAlamat(alamat)
                .setProvinsi(provinsi)
                .setNegara(negara)
                .setKota(kota)
                .setCourier(courier)
                .setShippingMethod(shippingMethod)
                .setShippingCost(shippingCost)
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
                                .courier(courier) // Preserve original courier parameter
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
    public Future<ShippingAddress> updateShippingAddress(Long shippingId, String alamat, String provinsi, String negara, String kota, String courier, String shippingMethod, Integer shippingCost) {
        UpdateShippingAddressRequest request = UpdateShippingAddressRequest.newBuilder()
                .setShippingId(shippingId.intValue())
                .setAlamat(alamat)
                .setProvinsi(provinsi)
                .setNegara(negara)
                .setKota(kota)
                .setCourier(courier)
                .setShippingMethod(shippingMethod)
                .setShippingCost(shippingCost)
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
                                .courier(courier) // Preserve original courier parameter
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
    public Future<ShippingAddress> getShippingAddressByOrderID(Integer orderId) {
        FindByIdShippingRequest request = FindByIdShippingRequest.newBuilder()
                .setId(orderId)
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
    public Future<Void> deleteShippingAddressPermanently(Integer orderId) {
        FindByIdShippingRequest request = FindByIdShippingRequest.newBuilder()
                .setId(orderId)
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
        if (ts == null || ts.isBlank()) return null;
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
