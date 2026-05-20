package io.example.transaction.service;

import io.example.common.model.ApiResponse;
import io.example.common.model.PagedResult;
import io.example.transaction.model.FindAllTransaction;
import io.example.transaction.model.FindAllTransactionByMerchant;
import io.example.transaction.model.Transaction;
import io.vertx.core.Future;

public interface TransactionQueryService {
    Future<ApiResponse<PagedResult<Transaction>>> getTransactions(FindAllTransaction req);
    Future<ApiResponse<PagedResult<Transaction>>> getTransactionsActive(FindAllTransaction req);
    Future<ApiResponse<PagedResult<Transaction>>> getTransactionsTrashed(FindAllTransaction req);
    Future<ApiResponse<PagedResult<Transaction>>> getTransactionByMerchant(FindAllTransactionByMerchant req);
    Future<ApiResponse<Transaction>> getTransactionById(Long transactionId);
    Future<ApiResponse<Transaction>> getTransactionByOrderId(Long orderId);
}
