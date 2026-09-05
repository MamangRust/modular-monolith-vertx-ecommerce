package io.example.transaction.repository.impl;

import io.example.transaction.domain.requests.CreateTransactionRequest;
import io.example.transaction.domain.requests.UpdateTransactionRequest;
import io.example.transaction.model.Transaction;
import io.example.transaction.repository.TransactionCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionCommandRepositoryImpl implements TransactionCommandRepository {
        private final Pool client;

        @Override
        public Future<Transaction> createTransaction(CreateTransactionRequest req) {
                return client
                                .preparedQuery(
                                                """
                                                                INSERT INTO transactions (order_id, merchant_id, payment_method, amount, payment_status, idempotency_key, created_at, updated_at)
                                                                VALUES ($1, $2, $3, $4, $5, $6, NOW(), NOW())
                                                                RETURNING transaction_id, order_id, merchant_id, payment_method, amount, payment_status, created_at, updated_at;
                                                                """)
                                .execute(Tuple.of(req.getOrderID(), req.getMerchantID(), req.getPaymentMethod(),
                                                req.getAmount(), req.getPaymentStatus(), req.getIdempotencyKey()))
                                .map(rows -> rows.iterator().hasNext() ? Transaction.fromRow(rows.iterator().next())
                                                : null);
        }

        @Override
        public Future<Transaction> createTransactionWithOutbox(CreateTransactionRequest req,
                        JsonObject emailPayload, String emailTopic, String emailKey,
                        JsonObject merchantPayload, String merchantTopic, String merchantKey) {
                return client.withTransaction(connection -> connection
                                .preparedQuery("""
                                                INSERT INTO transactions (order_id, merchant_id, payment_method, amount, payment_status, idempotency_key, created_at, updated_at)
                                                VALUES ($1, $2, $3, $4, $5, $6, NOW(), NOW())
                                                RETURNING transaction_id, order_id, merchant_id, payment_method, amount, payment_status, created_at, updated_at
                                                """)
                                .execute(Tuple.of(req.getOrderID(), req.getMerchantID(), req.getPaymentMethod(),
                                                req.getAmount(), req.getPaymentStatus(), req.getIdempotencyKey()))
                                .compose(rows -> {
                                        if (!rows.iterator().hasNext()) {
                                                return Future.failedFuture(new IllegalStateException(
                                                                "Transaction insert returned no row"));
                                        }
                                        Transaction transaction = Transaction.fromRow(rows.iterator().next());
                                        String txId = String.valueOf(transaction.getTransactionId());
                                        JsonObject resolvedMerchantPayload = merchantPayload.copy()
                                                        .put("transactionId", transaction.getTransactionId());
                                        return connection.preparedQuery("""
                                                        INSERT INTO outbox (aggregate_type, aggregate_id, event_type, payload, topic, key, created_at)
                                                        VALUES ($1, $2, $3, $4::jsonb, $5, $6, NOW())
                                                        ON CONFLICT (aggregate_type, aggregate_id, event_type, topic, key) DO NOTHING
                                                        """)
                                                        .execute(Tuple.of("transaction", txId, "transaction.created",
                                                                        emailPayload.encode(), emailTopic, emailKey))
                                                        .compose(ignored -> connection.preparedQuery("""
                                                                        INSERT INTO outbox (aggregate_type, aggregate_id, event_type, payload, topic, key, created_at)
                                                                        VALUES ($1, $2, $3, $4::jsonb, $5, $6, NOW())
                                                                        ON CONFLICT (aggregate_type, aggregate_id, event_type, topic, key) DO NOTHING
                                                                        """)
                                                                        .execute(Tuple.of("transaction", txId,
                                                                                        "transaction.created",
                                                                                        resolvedMerchantPayload.encode(),
                                                                                        merchantTopic, merchantKey)))
                                                        .map(ignored -> transaction);
                                }));
        }

        @Override
        public Future<Transaction> updateTransaction(UpdateTransactionRequest req) {
                return client
                                .preparedQuery(
                                                """
                                                                UPDATE transactions
                                                                SET order_id = COALESCE(NULLIF($1::INT, 0), order_id), merchant_id = COALESCE(NULLIF($2::INT, 0), merchant_id), payment_method = COALESCE(NULLIF($3, ''), payment_method), amount = COALESCE(NULLIF($4::INT, 0), amount), payment_status = COALESCE(NULLIF($5, ''), payment_status), updated_at = NOW()
                                                                WHERE transaction_id = $6 AND deleted_at IS NULL
                                                                RETURNING transaction_id, order_id, merchant_id, payment_method, amount, payment_status, created_at, updated_at;
                                                                """)
                                .execute(Tuple.of(req.getOrderID() != null ? req.getOrderID() : 0, req.getMerchantID() != null ? req.getMerchantID() : 0, req.getPaymentMethod() != null ? req.getPaymentMethod() : "",
                                                req.getAmount() != null ? req.getAmount() : 0, req.getPaymentStatus() != null ? req.getPaymentStatus() : "", req.getTransactionID()))
                                .map(rows -> rows.iterator().hasNext() ? Transaction.fromRow(rows.iterator().next())
                                                : null);
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
                                .map(rows -> rows.iterator().hasNext() ? Transaction.fromRow(rows.iterator().next())
                                                : null);
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
                                .map(rows -> rows.iterator().hasNext() ? Transaction.fromRow(rows.iterator().next())
                                                : null);
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
