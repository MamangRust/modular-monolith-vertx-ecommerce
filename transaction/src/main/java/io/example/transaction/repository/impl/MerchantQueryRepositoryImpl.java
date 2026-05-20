package io.example.transaction.repository.impl;

import io.example.transaction.repository.MerchantQueryRepository;
import io.vertx.core.Future;
import pb.merchant.MerchantCommon.FindByIdMerchantRequest;
import pb.merchant.VertxMerchantQueryServiceGrpcClient;

public class MerchantQueryRepositoryImpl implements MerchantQueryRepository {
    private final VertxMerchantQueryServiceGrpcClient client;

    public MerchantQueryRepositoryImpl(VertxMerchantQueryServiceGrpcClient client) {
        this.client = client;
    }

    @Override
    public Future<Boolean> findById(Integer merchantId) {
        FindByIdMerchantRequest request = FindByIdMerchantRequest.newBuilder()
                .setId(merchantId)
                .build();

        return client.findById(request)
                .map(response -> response != null && response.hasData());
    }
}
