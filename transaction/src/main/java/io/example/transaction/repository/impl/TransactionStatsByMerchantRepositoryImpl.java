package io.example.transaction.repository.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.example.transaction.model.*;
import io.example.transaction.domain.requests.FindMonthlyMerchantStatsRequest;
import io.example.transaction.domain.requests.FindYearlyMerchantStatsRequest;
import io.example.transaction.repository.TransactionStatsByMerchantRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionStatsByMerchantRepositoryImpl implements TransactionStatsByMerchantRepository {
    private final Pool client;

    private Tuple getMonthlyTuple(int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
        LocalDateTime startPrev = start.minusYears(1);
        LocalDateTime endPrev = startPrev.withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
        return Tuple.of(start, end, startPrev, endPrev);
    }

    @Override
    public Future<List<TransactionMonthlyAmountSuccess>> getMonthlyAmountTransactionSuccessByMerchant(
            FindMonthlyMerchantStatsRequest req) {
        Tuple args = getMonthlyTuple(req.getYear(), req.getMonth()).addInteger(req.getMerchantId());
        return client
                .preparedQuery(
                        """
                                WITH monthly_data AS (
                                    SELECT EXTRACT(YEAR FROM t.created_at)::integer AS year, EXTRACT(MONTH FROM t.created_at)::integer AS month,
                                           COUNT(*) AS total_success, COALESCE(SUM(t.amount), 0)::integer AS total_amount
                                    FROM transactions t
                                    WHERE t.deleted_at IS NULL AND t.payment_status = 'success' AND t.merchant_id = $5
                                      AND ((t.created_at >= $1 AND t.created_at <= $2) OR (t.created_at >= $3 AND t.created_at <= $4))
                                    GROUP BY EXTRACT(YEAR FROM t.created_at), EXTRACT(MONTH FROM t.created_at)
                                ), formatted_data AS (
                                    SELECT year::text, TO_CHAR(TO_DATE(month::text, 'MM'), 'Mon') AS month, total_success, total_amount FROM monthly_data
                                    UNION ALL SELECT EXTRACT(YEAR FROM $1::timestamp)::text, TO_CHAR($1::timestamp, 'Mon'), 0, 0
                                    WHERE NOT EXISTS (SELECT 1 FROM monthly_data WHERE year = EXTRACT(YEAR FROM $1::timestamp)::integer AND month = EXTRACT(MONTH FROM $1::timestamp)::integer)
                                    UNION ALL SELECT EXTRACT(YEAR FROM $3::timestamp)::text, TO_CHAR($3::timestamp, 'Mon'), 0, 0
                                    WHERE NOT EXISTS (SELECT 1 FROM monthly_data WHERE year = EXTRACT(YEAR FROM $3::timestamp)::integer AND month = EXTRACT(MONTH FROM $3::timestamp)::integer)
                                ) SELECT * FROM formatted_data ORDER BY year DESC, TO_DATE(month, 'Mon') DESC;
                                """)
                .execute(args)
                .map(this::mapMonthlySuccess);
    }

    @Override
    public Future<List<TransactionYearlyAmountSuccess>> getYearlyAmountTransactionSuccessByMerchant(
            FindYearlyMerchantStatsRequest req) {
        return client
                .preparedQuery(
                        """
                                WITH yearly_data AS (
                                    SELECT EXTRACT(YEAR FROM t.created_at)::integer AS year, COUNT(*) AS total_success, COALESCE(SUM(t.amount), 0)::integer AS total_amount
                                    FROM transactions t
                                    WHERE t.deleted_at IS NULL AND t.payment_status = 'success' AND t.merchant_id = $2
                                      AND (EXTRACT(YEAR FROM t.created_at) = $1::integer OR EXTRACT(YEAR FROM t.created_at) = $1::integer - 1)
                                    GROUP BY EXTRACT(YEAR FROM t.created_at)
                                ), formatted_data AS (
                                    SELECT year::text, total_success::integer, total_amount::integer FROM yearly_data
                                    UNION ALL SELECT $1::text, 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE year = $1::integer)
                                    UNION ALL SELECT ($1::integer - 1)::text, 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE year = $1::integer - 1)
                                ) SELECT * FROM formatted_data ORDER BY year DESC;
                                """)
                .execute(Tuple.of(req.getYear(), req.getMerchantId()))
                .map(this::mapYearlySuccess);
    }

    @Override
    public Future<List<TransactionMonthlyAmountFailed>> getMonthlyAmountTransactionFailedByMerchant(
            FindMonthlyMerchantStatsRequest req) {
        Tuple args = getMonthlyTuple(req.getYear(), req.getMonth()).addInteger(req.getMerchantId());
        return client
                .preparedQuery(
                        """
                                WITH monthly_data AS (
                                    SELECT EXTRACT(YEAR FROM t.created_at)::integer AS year, EXTRACT(MONTH FROM t.created_at)::integer AS month,
                                           COUNT(*) AS total_failed, COALESCE(SUM(t.amount), 0)::integer AS total_amount
                                    FROM transactions t
                                    WHERE t.deleted_at IS NULL AND t.payment_status = 'failed' AND t.merchant_id = $5
                                      AND ((t.created_at >= $1 AND t.created_at <= $2) OR (t.created_at >= $3 AND t.created_at <= $4))
                                    GROUP BY EXTRACT(YEAR FROM t.created_at), EXTRACT(MONTH FROM t.created_at)
                                ), formatted_data AS (
                                    SELECT year::text, TO_CHAR(TO_DATE(month::text, 'MM'), 'Mon') AS month, total_failed, total_amount FROM monthly_data
                                    UNION ALL SELECT EXTRACT(YEAR FROM $1::timestamp)::text, TO_CHAR($1::timestamp, 'Mon'), 0, 0
                                    WHERE NOT EXISTS (SELECT 1 FROM monthly_data WHERE year = EXTRACT(YEAR FROM $1::timestamp)::integer AND month = EXTRACT(MONTH FROM $1::timestamp)::integer)
                                    UNION ALL SELECT EXTRACT(YEAR FROM $3::timestamp)::text, TO_CHAR($3::timestamp, 'Mon'), 0, 0
                                    WHERE NOT EXISTS (SELECT 1 FROM monthly_data WHERE year = EXTRACT(YEAR FROM $3::timestamp)::integer AND month = EXTRACT(MONTH FROM $3::timestamp)::integer)
                                ) SELECT * FROM formatted_data ORDER BY year DESC, TO_DATE(month, 'Mon') DESC;
                                """)
                .execute(args)
                .map(this::mapMonthlyFailed);
    }

    @Override
    public Future<List<TransactionYearlyAmountFailed>> getYearlyAmountTransactionFailedByMerchant(
            FindYearlyMerchantStatsRequest req) {
        return client
                .preparedQuery(
                        """
                                WITH yearly_data AS (
                                    SELECT EXTRACT(YEAR FROM t.created_at)::integer AS year, COUNT(*) AS total_failed, COALESCE(SUM(t.amount), 0)::integer AS total_amount
                                    FROM transactions t
                                    WHERE t.deleted_at IS NULL AND t.payment_status = 'failed' AND t.merchant_id = $2
                                      AND (EXTRACT(YEAR FROM t.created_at) = $1::integer OR EXTRACT(YEAR FROM t.created_at) = $1::integer - 1)
                                    GROUP BY EXTRACT(YEAR FROM t.created_at)
                                ), formatted_data AS (
                                    SELECT year::text, total_failed::integer, total_amount::integer FROM yearly_data
                                    UNION ALL SELECT $1::text, 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE year = $1::integer)
                                    UNION ALL SELECT ($1::integer - 1)::text, 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE year = $1::integer - 1)
                                ) SELECT * FROM formatted_data ORDER BY year DESC;
                                """)
                .execute(Tuple.of(req.getYear(), req.getMerchantId()))
                .map(this::mapYearlyFailed);
    }

    @Override
    public Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsByMerchantSuccess(
            FindMonthlyMerchantStatsRequest req) {
        Tuple args = getMonthlyTuple(req.getYear(), req.getMonth()).addInteger(req.getMerchantId());
        return client
                .preparedQuery(
                        """
                                WITH date_ranges AS (SELECT $1::timestamp AS range1_start, $2::timestamp AS range1_end, $3::timestamp AS range2_start, $4::timestamp AS range2_end),
                                     payment_methods AS (SELECT DISTINCT payment_method FROM transactions WHERE deleted_at IS NULL),
                                     all_months AS (
                                         SELECT generate_series(date_trunc('month', LEAST((SELECT range1_start FROM date_ranges), (SELECT range2_start FROM date_ranges))),
                                                                date_trunc('month', GREATEST((SELECT range1_end FROM date_ranges), (SELECT range2_end FROM date_ranges))), interval '1 month')::date AS activity_month
                                     ),
                                     all_combinations AS (SELECT am.activity_month, pm.payment_method FROM all_months am CROSS JOIN payment_methods pm),
                                     monthly_transactions AS (
                                         SELECT date_trunc('month', t.created_at)::date AS activity_month, t.payment_method,
                                                COUNT(t.transaction_id) AS total_transactions, COALESCE(SUM(t.amount), 0)::NUMERIC AS total_amount
                                         FROM transactions t JOIN date_ranges dr ON (t.created_at BETWEEN dr.range1_start AND dr.range1_end OR t.created_at BETWEEN dr.range2_start AND dr.range2_end)
                                         WHERE t.deleted_at IS NULL AND t.payment_status = 'success' AND t.merchant_id = $5
                                         GROUP BY date_trunc('month', t.created_at), t.payment_method
                                     )
                                SELECT TO_CHAR(ac.activity_month, 'Mon') AS month, ac.payment_method,
                                       COALESCE(mt.total_transactions, 0) AS total_transactions, COALESCE(mt.total_amount, 0) AS total_amount
                                FROM all_combinations ac LEFT JOIN monthly_transactions mt ON ac.activity_month = mt.activity_month AND ac.payment_method = mt.payment_method
                                ORDER BY ac.activity_month, ac.payment_method;
                                """)
                .execute(args)
                .map(this::mapMonthlyMethod);
    }

    @Override
    public Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsByMerchantFailed(
            FindMonthlyMerchantStatsRequest req) {
        Tuple args = getMonthlyTuple(req.getYear(), req.getMonth()).addInteger(req.getMerchantId());
        return client
                .preparedQuery(
                        """
                                WITH date_ranges AS (SELECT $1::timestamp AS range1_start, $2::timestamp AS range1_end, $3::timestamp AS range2_start, $4::timestamp AS range2_end),
                                     payment_methods AS (SELECT DISTINCT payment_method FROM transactions WHERE deleted_at IS NULL),
                                     all_months AS (
                                         SELECT generate_series(date_trunc('month', LEAST((SELECT range1_start FROM date_ranges), (SELECT range2_start FROM date_ranges))),
                                                                date_trunc('month', GREATEST((SELECT range1_end FROM date_ranges), (SELECT range2_end FROM date_ranges))), interval '1 month')::date AS activity_month
                                     ),
                                     all_combinations AS (SELECT am.activity_month, pm.payment_method FROM all_months am CROSS JOIN payment_methods pm),
                                     monthly_transactions AS (
                                         SELECT date_trunc('month', t.created_at)::date AS activity_month, t.payment_method,
                                                COUNT(t.transaction_id) AS total_transactions, COALESCE(SUM(t.amount), 0)::NUMERIC AS total_amount
                                         FROM transactions t JOIN date_ranges dr ON (t.created_at BETWEEN dr.range1_start AND dr.range1_end OR t.created_at BETWEEN dr.range2_start AND dr.range2_end)
                                         WHERE t.deleted_at IS NULL AND t.payment_status = 'failed' AND t.merchant_id = $5
                                         GROUP BY date_trunc('month', t.created_at), t.payment_method
                                     )
                                SELECT TO_CHAR(ac.activity_month, 'Mon') AS month, ac.payment_method,
                                       COALESCE(mt.total_transactions, 0) AS total_transactions, COALESCE(mt.total_amount, 0) AS total_amount
                                FROM all_combinations ac LEFT JOIN monthly_transactions mt ON ac.activity_month = mt.activity_month AND ac.payment_method = mt.payment_method
                                ORDER BY ac.activity_month, ac.payment_method;
                                """)
                .execute(args)
                .map(this::mapMonthlyMethod);
    }

    @Override
    public Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsByMerchantSuccess(
            FindYearlyMerchantStatsRequest req) {
        LocalDateTime refTs = LocalDateTime.of(req.getYear(), 1, 1, 0, 0);
        return client
                .preparedQuery(
                        """
                                WITH year_series AS (SELECT generate_series(EXTRACT(YEAR FROM $1::timestamp)::integer - 1, EXTRACT(YEAR FROM $1::timestamp)::integer, 1) AS year),
                                     yearly_transactions AS (
                                         SELECT EXTRACT(YEAR FROM t.created_at)::integer AS year, t.payment_method,
                                                COUNT(t.transaction_id) AS total_transactions, SUM(t.amount)::NUMERIC AS total_amount
                                         FROM transactions t
                                         WHERE t.deleted_at IS NULL AND t.payment_status = 'success' AND t.merchant_id = $2
                                           AND EXTRACT(YEAR FROM t.created_at) BETWEEN (EXTRACT(YEAR FROM $1::timestamp) - 1) AND EXTRACT(YEAR FROM $1::timestamp)
                                         GROUP BY year, t.payment_method
                                     ),
                                     payment_methods AS (SELECT DISTINCT payment_method FROM transactions WHERE deleted_at IS NULL)
                                SELECT ys.year::text AS year, pm.payment_method,
                                       COALESCE(yt.total_transactions, 0) AS total_transactions, COALESCE(yt.total_amount, 0) AS total_amount
                                FROM year_series ys CROSS JOIN payment_methods pm
                                LEFT JOIN yearly_transactions yt ON ys.year = yt.year AND pm.payment_method = yt.payment_method
                                ORDER BY ys.year, pm.payment_method;
                                """)
                .execute(Tuple.of(refTs, req.getMerchantId()))
                .map(this::mapYearlyMethod);
    }

    @Override
    public Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsByMerchantFailed(
            FindYearlyMerchantStatsRequest req) {
        LocalDateTime refTs = LocalDateTime.of(req.getYear(), 1, 1, 0, 0);
        return client
                .preparedQuery(
                        """
                                WITH year_series AS (SELECT generate_series(EXTRACT(YEAR FROM $1::timestamp)::integer - 1, EXTRACT(YEAR FROM $1::timestamp)::integer, 1) AS year),
                                     yearly_transactions AS (
                                         SELECT EXTRACT(YEAR FROM t.created_at)::integer AS year, t.payment_method,
                                                COUNT(t.transaction_id) AS total_transactions, SUM(t.amount)::NUMERIC AS total_amount
                                         FROM transactions t
                                         WHERE t.deleted_at IS NULL AND t.payment_status = 'failed' AND t.merchant_id = $2
                                           AND EXTRACT(YEAR FROM t.created_at) BETWEEN (EXTRACT(YEAR FROM $1::timestamp) - 1) AND EXTRACT(YEAR FROM $1::timestamp)
                                         GROUP BY year, t.payment_method
                                     ),
                                     payment_methods AS (SELECT DISTINCT payment_method FROM transactions WHERE deleted_at IS NULL)
                                SELECT ys.year::text AS year, pm.payment_method,
                                       COALESCE(yt.total_transactions, 0) AS total_transactions, COALESCE(yt.total_amount, 0) AS total_amount
                                FROM year_series ys CROSS JOIN payment_methods pm
                                LEFT JOIN yearly_transactions yt ON ys.year = yt.year AND pm.payment_method = yt.payment_method
                                ORDER BY ys.year, pm.payment_method;
                                """)
                .execute(Tuple.of(refTs, req.getMerchantId()))
                .map(this::mapYearlyMethod);
    }

    private Integer getIntegerFromNumber(Row row, String column) {
        Object value = row.getValue(column);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return value == null ? 0 : Integer.valueOf(value.toString());
    }

    private Long getLongFromDecimal(Row row, String column) {
        Object val = row.getValue(column);
        if (val == null)
            return 0L;
        if (val instanceof BigDecimal)
            return ((BigDecimal) val).longValue();
        if (val instanceof Number)
            return ((Number) val).longValue();
        return 0L;
    }

    private List<TransactionMonthlyAmountSuccess> mapMonthlySuccess(RowSet<Row> rows) {
        List<TransactionMonthlyAmountSuccess> list = new ArrayList<>();
        for (Row r : rows) {
            list.add(new TransactionMonthlyAmountSuccess(
                    r.getString("year"), r.getString("month"), getIntegerFromNumber(r, "total_success"),
                    getIntegerFromNumber(r, "total_amount")));
        }
        return list;
    }

    private List<TransactionYearlyAmountSuccess> mapYearlySuccess(RowSet<Row> rows) {
        List<TransactionYearlyAmountSuccess> list = new ArrayList<>();
        for (Row r : rows) {
            list.add(new TransactionYearlyAmountSuccess(
                    r.getString("year"), getIntegerFromNumber(r, "total_success"),
                    getIntegerFromNumber(r, "total_amount")));
        }
        return list;
    }

    private List<TransactionMonthlyAmountFailed> mapMonthlyFailed(RowSet<Row> rows) {
        List<TransactionMonthlyAmountFailed> list = new ArrayList<>();
        for (Row r : rows) {
            list.add(new TransactionMonthlyAmountFailed(
                    r.getString("year"), r.getString("month"), getIntegerFromNumber(r, "total_failed"),
                    getIntegerFromNumber(r, "total_amount")));
        }
        return list;
    }

    private List<TransactionYearlyAmountFailed> mapYearlyFailed(RowSet<Row> rows) {
        List<TransactionYearlyAmountFailed> list = new ArrayList<>();
        for (Row r : rows) {
            list.add(new TransactionYearlyAmountFailed(
                    r.getString("year"), getIntegerFromNumber(r, "total_failed"),
                    getIntegerFromNumber(r, "total_amount")));
        }
        return list;
    }

    private List<TransactionMonthlyMethod> mapMonthlyMethod(RowSet<Row> rows) {
        List<TransactionMonthlyMethod> list = new ArrayList<>();
        for (Row r : rows) {
            list.add(new TransactionMonthlyMethod(
                    r.getString("month"), r.getString("payment_method"),
                    getIntegerFromNumber(r, "total_transactions"), getLongFromDecimal(r, "total_amount")));
        }
        return list;
    }

    private List<TransactionYearlyMethod> mapYearlyMethod(RowSet<Row> rows) {
        List<TransactionYearlyMethod> list = new ArrayList<>();
        for (Row r : rows) {
            list.add(new TransactionYearlyMethod(
                    r.getString("year"), r.getString("payment_method"),
                    getIntegerFromNumber(r, "total_transactions"), getLongFromDecimal(r, "total_amount")));
        }
        return list;
    }
}
