package io.example.transaction.repository.impl;

import io.example.transaction.model.CreateTransactionRequest;
import io.example.transaction.model.UpdateTransactionRequest;
import io.example.transaction.model.Transaction;
import io.example.transaction.repository.TransactionCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;

public class TransactionCommandRepositoryImpl implements TransactionCommandRepository {
    private final Pool client;

    public TransactionCommandRepositoryImpl(Pool client) {
        this.client = client;
    }

    @Override
    public Future<Transaction> createTransaction(CreateTransactionRequest req) {
        return client
                .preparedQuery(
                        """
                                INSERT INTO transactions (order_id, merchant_id, payment_method, amount, payment_status, created_at, updated_at)
                                VALUES ($1, $2, $3, $4, $5, NOW(), NOW())
                                RETURNING transaction_id, order_id, merchant_id, payment_method, amount, payment_status, created_at, updated_at;
                                """)
                .execute(Tuple.of(req.getOrderID(), req.getMerchantID(), req.getPaymentMethod(), req.getAmount(), req.getPaymentStatus()))
                .map(rows -> rows.iterator().hasNext() ? Transaction.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Transaction> updateTransaction(UpdateTransactionRequest req) {
        return client
                .preparedQuery(
                        """
                                UPDATE transactions
                                SET order_id = $1, merchant_id = $2, payment_method = $3, amount = $4, payment_status = $5, updated_at = NOW()
                                WHERE transaction_id = $6 AND deleted_at IS NULL
                                RETURNING transaction_id, order_id, merchant_id, payment_method, amount, payment_status, created_at, updated_at;
                                """)
                .execute(Tuple.of(req.getOrderID(), req.getMerchantID(), req.getPaymentMethod(), req.getAmount(), req.getPaymentStatus(), req.getTransactionID()))
                .map(rows -> rows.iterator().hasNext() ? Transaction.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Transaction> trashTransaction(Long transactionId) {
        return client
                .preparedQuery(
                        """
                                UPDATE transactions
                                SET deleted_at = NOW()
                                WHERE transaction_id = $1 AND deleted_at IS NULL
                                RETURNING transaction_id, order_id, merchant_id, payment_method, amount, payment_status, created_at, updated_at, deleted_at;
                                """)
                .execute(Tuple.of(transactionId))
                .map(rows -> rows.iterator().hasNext() ? Transaction.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Transaction> restoreTransaction(Long transactionId) {
        return client
                .preparedQuery(
                        """
                                UPDATE transactions
                                SET deleted_at = NULL
                                WHERE transaction_id = $1 AND deleted_at IS NOT NULL
                                RETURNING transaction_id, order_id, merchant_id, payment_method, amount, payment_status, created_at, updated_at;
                                """)
                .execute(Tuple.of(transactionId))
                .map(rows -> rows.iterator().hasNext() ? Transaction.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Boolean> deleteTransactionPermanently(Long transactionId) {
        return client
                .preparedQuery("DELETE FROM transactions WHERE transaction_id = $1 AND deleted_at IS NOT NULL;")
                .execute(Tuple.of(transactionId))
                .map(rows -> rows.rowCount() > 0);
    }

    @Override
    public Future<Boolean> deleteTransactionByOrderIdPermanently(Long orderId) {
        return client
                .preparedQuery("DELETE FROM transactions WHERE order_id = $1;")
                .execute(Tuple.of(orderId))
                .map(rows -> rows.rowCount() > 0);
    }

    @Override
    public Future<Integer> restoreAllTransactions() {
        return client
                .preparedQuery("UPDATE transactions SET deleted_at = NULL WHERE deleted_at IS NOT NULL;")
                .execute()
                .map(rows -> rows.rowCount());
    }

    @Override
    public Future<Integer> deleteAllPermanentTransactions() {
        return client
                .preparedQuery("DELETE FROM transactions WHERE deleted_at IS NOT NULL;")
                .execute()
                .map(rows -> rows.rowCount());
    }
}
