package io.example.order.repository.impl;

import com.google.protobuf.Empty;
import io.example.order.repository.TransactionCommandRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.transaction.TransactionCommon.FindByIdTransactionRequest;
import pb.transaction.VertxTransactionCommandServiceGrpcClient;

@RequiredArgsConstructor
public class TransactionCommandRepositoryImpl implements TransactionCommandRepository {
    private final VertxTransactionCommandServiceGrpcClient client;

    @Override
    public Future<Boolean> deleteByOrderIDPermanent(Long orderId) {
        FindByIdTransactionRequest request = FindByIdTransactionRequest.newBuilder()
                .setId(orderId.intValue())
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
