package io.example.transaction.repository;

import io.example.common.domain.PagedResult;
import io.example.transaction.domain.requests.FindAllTransaction;
import io.example.transaction.domain.requests.FindAllTransactionByMerchant;
import io.example.transaction.model.Transaction;
import io.vertx.core.Future;

public interface TransactionQueryRepository {
    Future<PagedResult<Transaction>> getTransactions(FindAllTransaction req);

    Future<PagedResult<Transaction>> getTransactionsActive(FindAllTransaction req);

    Future<PagedResult<Transaction>> getTransactionsTrashed(FindAllTransaction req);

    Future<PagedResult<Transaction>> getTransactionByMerchant(FindAllTransactionByMerchant req);

    Future<Transaction> getTransactionById(Long transactionId);

    Future<Transaction> findByTrashedId(Long transactionId);

    Future<Transaction> getTransactionByOrderId(Long orderId);

    Future<Transaction> getTransactionByIdempotencyKey(String idempotencyKey);
}
