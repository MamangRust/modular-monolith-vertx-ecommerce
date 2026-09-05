package io.example.transaction.repository;

import io.example.transaction.domain.requests.CreateTransactionRequest;
import io.example.transaction.domain.requests.UpdateTransactionRequest;
import io.example.transaction.model.Transaction;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface TransactionCommandRepository {
    Future<Transaction> createTransaction(CreateTransactionRequest req);

    /**
     * Persist the transaction row and its outbox events in one database
     * transaction. A failure rolls back both writes.
     */
    Future<Transaction> createTransactionWithOutbox(CreateTransactionRequest req,
            JsonObject emailPayload, String emailTopic, String emailKey,
            JsonObject merchantPayload, String merchantTopic, String merchantKey);

    Future<Transaction> updateTransaction(UpdateTransactionRequest req);

    Future<Transaction> trashTransaction(Long transactionId);

    Future<Transaction> restoreTransaction(Long transactionId);

    Future<Boolean> deleteTransactionPermanently(Long transactionId);

    Future<Boolean> deleteTransactionByOrderIdPermanently(Long orderId);

    Future<Integer> restoreAllTransactions();

    Future<Integer> deleteAllPermanentTransactions();
}
