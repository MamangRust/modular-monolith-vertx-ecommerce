package io.example.transaction.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.transaction.domain.requests.FindAllTransaction;
import io.example.transaction.domain.requests.FindAllTransactionByMerchant;
import io.example.transaction.model.Transaction;
import io.example.transaction.repository.TransactionQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionQueryRepositoryImpl implements TransactionQueryRepository {
    private final Pool client;

    @Override
    public Future<PagedResult<Transaction>> getTransactions(FindAllTransaction req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

        return client
                .preparedQuery(
                        """
                                SELECT
                                    transaction_id, order_id, merchant_id, payment_method, amount, payment_status,
                                    created_at, updated_at,
                                    COUNT(*) OVER () AS total_count
                                FROM transactions
                                WHERE deleted_at IS NULL
                                  AND ($1::TEXT IS NULL OR payment_method ILIKE '%' || $1 || '%' OR payment_status ILIKE '%' || $1 || '%')
                                ORDER BY created_at DESC
                                LIMIT $2 OFFSET $3;
                                """)
                .execute(Tuple.of(normalizeSearch(req.getSearch()), req.getPageSize(), offset))
                .map(this::mapPagedTransactions);
    }

    @Override
    public Future<PagedResult<Transaction>> getTransactionsActive(FindAllTransaction req) {
        return getTransactions(req);
    }

    @Override
    public Future<PagedResult<Transaction>> getTransactionsTrashed(FindAllTransaction req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

        return client
                .preparedQuery(
                        """
                                SELECT
                                    transaction_id, order_id, merchant_id, payment_method, amount, payment_status,
                                    created_at, updated_at, deleted_at,
                                    COUNT(*) OVER () AS total_count
                                FROM transactions
                                WHERE deleted_at IS NOT NULL
                                  AND ($1::TEXT IS NULL OR payment_method ILIKE '%' || $1 || '%' OR payment_status ILIKE '%' || $1 || '%')
                                ORDER BY created_at DESC
                                LIMIT $2 OFFSET $3;
                                """)
                .execute(Tuple.of(normalizeSearch(req.getSearch()), req.getPageSize(), offset))
                .map(this::mapPagedTransactions);
    }

    @Override
    public Future<PagedResult<Transaction>> getTransactionByMerchant(FindAllTransactionByMerchant req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

        return client
                .preparedQuery(
                        """
                                SELECT
                                    transaction_id, order_id, merchant_id, payment_method, amount, payment_status,
                                    created_at, updated_at,
                                    COUNT(*) OVER () AS total_count
                                FROM transactions
                                WHERE deleted_at IS NULL
                                  AND ($1::TEXT IS NULL OR payment_method ILIKE '%' || $1 || '%' OR payment_status ILIKE '%' || $1 || '%')
                                  AND ($2::INT IS NULL OR merchant_id = $2)
                                ORDER BY created_at DESC
                                LIMIT $3 OFFSET $4;
                                """)
                .execute(Tuple.of(normalizeSearch(req.getSearch()), req.getMerchantId(), req.getPageSize(), offset))
                .map(this::mapPagedTransactions);
    }

    @Override
    public Future<Transaction> getTransactionById(Long transactionId) {
        return client
                .preparedQuery(
                        """
                                SELECT transaction_id, order_id, merchant_id, payment_method, amount, payment_status, created_at, updated_at
                                FROM transactions WHERE transaction_id = $1 AND deleted_at IS NULL;
                                """)
                .execute(Tuple.of(transactionId))
                .map(rows -> rows.iterator().hasNext() ? Transaction.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Transaction> getTransactionByOrderId(Long orderId) {
        return client
                .preparedQuery(
                        """
                                SELECT transaction_id, order_id, merchant_id, payment_method, amount, payment_status, created_at, updated_at
                                FROM transactions WHERE order_id = $1 AND deleted_at IS NULL;
                                """)
                .execute(Tuple.of(orderId))
                .map(rows -> rows.iterator().hasNext() ? Transaction.fromRow(rows.iterator().next()) : null);
    }

    @Override
    public Future<Transaction> findByTrashedId(Long transactionId) {
        return client
                .preparedQuery(
                        """
                                SELECT transaction_id, order_id, merchant_id, payment_method, amount, payment_status, created_at, updated_at, deleted_at
                                FROM transactions WHERE transaction_id = $1 AND deleted_at IS NOT NULL;
                                """)
                .execute(Tuple.of(transactionId))
                .map(rows -> rows.iterator().hasNext() ? Transaction.fromRow(rows.iterator().next()) : null);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank())
            return null;
        return search;
    }

    private PagedResult<Transaction> mapPagedTransactions(RowSet<Row> rows) {
        List<Transaction> list = new ArrayList<>();
        int total = 0;
        for (Row row : rows) {
            list.add(Transaction.fromRow(row));
            if (total == 0)
                total = row.getInteger("total_count");
        }
        return new PagedResult<>(list, total);
    }
}
