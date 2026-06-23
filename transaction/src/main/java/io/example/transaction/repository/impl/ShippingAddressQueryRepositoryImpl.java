package io.example.transaction.repository.impl;

import io.example.transaction.model.ShippingAddress;
import io.example.transaction.repository.ShippingAddressQueryRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.shipping_address.ShippingAddressCommon.FindByIdShippingRequest;
import pb.shipping_address.VertxShippingQueryServiceGrpcClient;

@RequiredArgsConstructor
public class ShippingAddressQueryRepositoryImpl implements ShippingAddressQueryRepository {
    private final VertxShippingQueryServiceGrpcClient client;

    @Override
    public Future<ShippingAddress> findByOrderId(Integer orderId) {
        FindByIdShippingRequest request = FindByIdShippingRequest.newBuilder()
                .setId(orderId)
                .build();

        return client.findByOrder(request)
                .map(response -> {
                    if (response != null && response.hasData()) {
                        var data = response.getData();
                        return ShippingAddress.builder()
                                .id(data.getId())
                                .orderId(data.getOrderId())
                                .shippingCost(data.getShippingCost())
                                .build();
                    }
                    return null;
                });
    }
}
