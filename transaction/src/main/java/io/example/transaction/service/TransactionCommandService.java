package io.example.transaction.service;

import io.example.transaction.domain.requests.CreateTransactionRequest;
import io.example.transaction.domain.requests.UpdateTransactionRequest;
import io.example.transaction.model.Transaction;
import pb.transaction.TransactionCommon.TransactionResponseDeleteAt;
import io.vertx.core.Future;

public interface TransactionCommandService {
    Future<Transaction> createTransaction(CreateTransactionRequest req);

    Future<Transaction> updateTransaction(UpdateTransactionRequest req);

    Future<TransactionResponseDeleteAt> trashTransaction(Long transactionId);

    Future<TransactionResponseDeleteAt> restoreTransaction(Long transactionId);

    Future<Void> deleteTransactionPermanently(Long transactionId);

    Future<Void> deleteTransactionByOrderIdPermanently(Long orderId);

    Future<Void> restoreAllTransactions();

    Future<Void> deleteAllPermanentTransactions();
}