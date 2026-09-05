package io.example.transaction.repository.impl;

import io.example.transaction.repository.WalletCommandRepository;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.wallet.WalletCommon.CreditWalletRequest;
import pb.wallet.WalletCommon.DebitWalletRequest;
import pb.wallet.VertxWalletCommandServiceGrpcClient;

@RequiredArgsConstructor
public class WalletCommandRepositoryImpl implements WalletCommandRepository {
    private final VertxWalletCommandServiceGrpcClient client;

    @Override
    public Future<Integer> debit(String cardNumber, int amount) {
        DebitWalletRequest request = DebitWalletRequest.newBuilder()
                .setCardNumber(cardNumber)
                .setAmount(amount)
                .build();

        return client.debit(request)
                .map(response -> {
                    if (response != null && response.hasData()) {
                        return response.getData().getTotalBalance();
                    }
                    throw new IllegalStateException("Empty debit response from wallet service");
                });
    }

    @Override
    public Future<Integer> credit(String cardNumber, int amount) {
        CreditWalletRequest request = CreditWalletRequest.newBuilder()
                .setCardNumber(cardNumber)
                .setAmount(amount)
                .build();

        return client.credit(request)
                .map(response -> {
                    if (response != null && response.hasData()) {
                        return response.getData().getTotalBalance();
                    }
                    throw new IllegalStateException("Empty credit response from wallet service");
                });
    }
}
