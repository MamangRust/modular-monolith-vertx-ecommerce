package io.example.transaction.service;

import io.example.common.model.ApiResponse;
import io.example.transaction.model.CreateTransactionRequest;
import io.example.transaction.model.UpdateTransactionRequest;
import io.example.transaction.model.Transaction;
import io.vertx.core.Future;

public interface TransactionCommandService {
    Future<ApiResponse<Transaction>> createTransaction(CreateTransactionRequest req);
    Future<ApiResponse<Transaction>> updateTransaction(UpdateTransactionRequest req);
    Future<ApiResponse<Transaction>> trashTransaction(Long transactionId);
    Future<ApiResponse<Transaction>> restoreTransaction(Long transactionId);
    Future<ApiResponse<Void>> deleteTransactionPermanently(Long transactionId);
    Future<ApiResponse<Void>> deleteTransactionByOrderIdPermanently(Long orderId);
    Future<ApiResponse<Integer>> restoreAllTransactions();
    Future<ApiResponse<Integer>> deleteAllPermanentTransactions();
}
