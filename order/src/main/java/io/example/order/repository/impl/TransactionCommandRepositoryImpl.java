package io.example.order.repository.impl;

import com.google.protobuf.Empty;
import io.example.order.repository.TransactionCommandRepository;
import io.vertx.core.Future;
import pb.transaction.TransactionCommon.FindByIdTransactionRequest;
import pb.transaction.VertxTransactionCommandServiceGrpcClient;

public class TransactionCommandRepositoryImpl implements TransactionCommandRepository {
    private final VertxTransactionCommandServiceGrpcClient client;

    public TransactionCommandRepositoryImpl(VertxTransactionCommandServiceGrpcClient client) {
        this.client = client;
    }

    @Override
    public Future<Boolean> deleteByOrderIDPermanent(Integer orderId) {
        FindByIdTransactionRequest request = FindByIdTransactionRequest.newBuilder()
                .setId(orderId)
                .build();

        return client.deleteTransactionByOrderPermanent(request)
                .map(response -> response != null);
    }

    @Override
    public Future<Boolean> deleteAll() {
        return client.deleteAllTransactionPermanent(Empty.getDefaultInstance())
                .map(response -> response != null);
    }
}
