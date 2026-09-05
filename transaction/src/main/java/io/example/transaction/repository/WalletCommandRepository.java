package io.example.transaction.repository;

import io.vertx.core.Future;

public interface WalletCommandRepository {
    /**
     * Debit (subtract) amount from the card's wallet balance via gRPC.
     * Returns the new balance after debit.
     * Fails with gRPC error if insufficient balance or card not found.
     */
    Future<Integer> debit(String cardNumber, int amount);

    /**
     * Credit (add to) the card's wallet balance via gRPC.
     * Returns the new balance after credit.
     */
    Future<Integer> credit(String cardNumber, int amount);
}
