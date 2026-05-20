package io.example.transaction.repository.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.example.transaction.model.*;
import io.example.transaction.repository.TransactionStatsRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class TransactionStatsRepositoryImpl implements TransactionStatsRepository {
    private final Pool client;

    public TransactionStatsRepositoryImpl(Pool client) {
        this.client = client;
    }

    private Tuple getMonthlyTuple(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        LocalDate startPrev = start.minusYears(1);
        LocalDate endPrev = startPrev.withDayOfMonth(startPrev.lengthOfMonth());
        return Tuple.of(start, end, startPrev, endPrev);
    }

    @Override
    public Future<List<TransactionMonthlyAmountSuccess>> getMonthlyAmountTransactionSuccess(int year, int month) {
        return client
                .preparedQuery(
                        """
                                WITH monthly_data AS (
                                    SELECT EXTRACT(YEAR FROM t.created_at)::integer AS year,
                                           EXTRACT(MONTH FROM t.created_at)::integer AS month,
                                           COUNT(*) AS total_success,
                                           COALESCE(SUM(t.amount), 0)::integer AS total_amount
                                    FROM transactions t
                                    WHERE t.deleted_at IS NULL AND t.payment_status = 'success'
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
                .execute(getMonthlyTuple(year, month))
                .map(this::mapMonthlySuccess);
    }

    @Override
    public Future<List<TransactionYearlyAmountSuccess>> getYearlyAmountTransactionSuccess(int year) {
        return client
                .preparedQuery(
                        """
                                WITH yearly_data AS (
                                    SELECT EXTRACT(YEAR FROM t.created_at)::integer AS year, COUNT(*) AS total_success, COALESCE(SUM(t.amount), 0)::integer AS total_amount
                                    FROM transactions t
                                    WHERE t.deleted_at IS NULL AND t.payment_status = 'success'
                                      AND (EXTRACT(YEAR FROM t.created_at) = $1::integer OR EXTRACT(YEAR FROM t.created_at) = $1::integer - 1)
                                    GROUP BY EXTRACT(YEAR FROM t.created_at)
                                ), formatted_data AS (
                                    SELECT year::text, total_success::integer, total_amount::integer FROM yearly_data
                                    UNION ALL SELECT $1::text, 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE year = $1::integer)
                                    UNION ALL SELECT ($1::integer - 1)::text, 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE year = $1::integer - 1)
                                ) SELECT * FROM formatted_data ORDER BY year DESC;
                                """)
                .execute(Tuple.of(year))
                .map(this::mapYearlySuccess);
    }

    @Override
    public Future<List<TransactionMonthlyAmountFailed>> getMonthlyAmountTransactionFailed(int year, int month) {
        return client
                .preparedQuery(
                        """
                                WITH monthly_data AS (
                                    SELECT EXTRACT(YEAR FROM t.created_at)::integer AS year,
                                           EXTRACT(MONTH FROM t.created_at)::integer AS month,
                                           COUNT(*) AS total_failed, COALESCE(SUM(t.amount), 0)::integer AS total_amount
                                    FROM transactions t
                                    WHERE t.deleted_at IS NULL AND t.payment_status = 'failed'
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
                .execute(getMonthlyTuple(year, month))
                .map(this::mapMonthlyFailed);
    }

    @Override
    public Future<List<TransactionYearlyAmountFailed>> getYearlyAmountTransactionFailed(int year) {
        return client
                .preparedQuery(
                        """
                                WITH yearly_data AS (
                                    SELECT EXTRACT(YEAR FROM t.created_at)::integer AS year, COUNT(*) AS total_failed, COALESCE(SUM(t.amount), 0)::integer AS total_amount
                                    FROM transactions t
                                    WHERE t.deleted_at IS NULL AND t.payment_status = 'failed'
                                      AND (EXTRACT(YEAR FROM t.created_at) = $1::integer OR EXTRACT(YEAR FROM t.created_at) = $1::integer - 1)
                                    GROUP BY EXTRACT(YEAR FROM t.created_at)
                                ), formatted_data AS (
                                    SELECT year::text, total_failed::integer, total_amount::integer FROM yearly_data
                                    UNION ALL SELECT $1::text, 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE year = $1::integer)
                                    UNION ALL SELECT ($1::integer - 1)::text, 0, 0 WHERE NOT EXISTS (SELECT 1 FROM yearly_data WHERE year = $1::integer - 1)
                                ) SELECT * FROM formatted_data ORDER BY year DESC;
                                """)
                .execute(Tuple.of(year))
                .map(this::mapYearlyFailed);
    }

    @Override
    public Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsSuccess(int year, int month) {
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
                                         WHERE t.deleted_at IS NULL AND t.payment_status = 'success'
                                         GROUP BY date_trunc('month', t.created_at), t.payment_method
                                     )
                                SELECT TO_CHAR(ac.activity_month, 'Mon') AS month, ac.payment_method,
                                       COALESCE(mt.total_transactions, 0) AS total_transactions, COALESCE(mt.total_amount, 0) AS total_amount
                                FROM all_combinations ac LEFT JOIN monthly_transactions mt ON ac.activity_month = mt.activity_month AND ac.payment_method = mt.payment_method
                                ORDER BY ac.activity_month, ac.payment_method;
                                """)
                .execute(getMonthlyTuple(year, month))
                .map(this::mapMonthlyMethod);
    }

    @Override
    public Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsFailed(int year, int month) {
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
                                         WHERE t.deleted_at IS NULL AND t.payment_status = 'failed'
                                         GROUP BY date_trunc('month', t.created_at), t.payment_method
                                     )
                                SELECT TO_CHAR(ac.activity_month, 'Mon') AS month, ac.payment_method,
                                       COALESCE(mt.total_transactions, 0) AS total_transactions, COALESCE(mt.total_amount, 0) AS total_amount
                                FROM all_combinations ac LEFT JOIN monthly_transactions mt ON ac.activity_month = mt.activity_month AND ac.payment_method = mt.payment_method
                                ORDER BY ac.activity_month, ac.payment_method;
                                """)
                .execute(getMonthlyTuple(year, month))
                .map(this::mapMonthlyMethod);
    }

    @Override
    public Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsSuccess(int year) {
        Timestamp refTs = Timestamp.valueOf(LocalDateTime.of(year, 1, 1, 0, 0));
        return client
                .preparedQuery(
                        """
                                WITH year_range AS (SELECT EXTRACT(YEAR FROM $1::timestamp)::int - 1 AS start_year, EXTRACT(YEAR FROM $1::timestamp)::int AS end_year),
                                     payment_methods AS (SELECT DISTINCT payment_method FROM transactions WHERE deleted_at IS NULL),
                                     all_years AS (SELECT generate_series((SELECT start_year FROM year_range), (SELECT end_year FROM year_range))::int AS year),
                                     all_combinations AS (SELECT ay.year::text AS year, pm.payment_method FROM all_years ay CROSS JOIN payment_methods pm),
                                     yearly_transactions AS (
                                         SELECT EXTRACT(YEAR FROM t.created_at)::text AS year, t.payment_method,
                                                COUNT(t.transaction_id) AS total_transactions, COALESCE(SUM(t.amount), 0)::NUMERIC AS total_amount
                                         FROM transactions t
                                         WHERE t.deleted_at IS NULL AND t.payment_status = 'success'
                                           AND EXTRACT(YEAR FROM t.created_at) BETWEEN (SELECT start_year FROM year_range) AND (SELECT end_year FROM year_range)
                                         GROUP BY EXTRACT(YEAR FROM t.created_at), t.payment_method
                                     )
                                SELECT ac.year, ac.payment_method, COALESCE(yt.total_transactions, 0) AS total_transactions, COALESCE(yt.total_amount, 0) AS total_amount
                                FROM all_combinations ac LEFT JOIN yearly_transactions yt ON ac.year = yt.year AND ac.payment_method = yt.payment_method
                                ORDER BY ac.year, ac.payment_method;
                                """)
                .execute(Tuple.of(refTs))
                .map(this::mapYearlyMethod);
    }

    @Override
    public Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsFailed(int year) {
        Timestamp refTs = Timestamp.valueOf(LocalDateTime.of(year, 1, 1, 0, 0));
        return client
                .preparedQuery(
                        """
                                 WITH year_range AS (SELECT EXTRACT(YEAR FROM $1::timestamp)::int - 1 AS start_year, EXTRACT(YEAR FROM $1::timestamp)::int AS end_year),
                                     payment_methods AS (SELECT DISTINCT payment_method FROM transactions WHERE deleted_at IS NULL),
                                     all_years AS (SELECT generate_series((SELECT start_year FROM year_range), (SELECT end_year FROM year_range))::int AS year),
                                     all_combinations AS (SELECT ay.year::text AS year, pm.payment_method FROM all_years ay CROSS JOIN payment_methods pm),
                                     yearly_transactions AS (
                                         SELECT EXTRACT(YEAR FROM t.created_at)::text AS year, t.payment_method,
                                                COUNT(t.transaction_id) AS total_transactions, COALESCE(SUM(t.amount), 0)::NUMERIC AS total_amount
                                         FROM transactions t
                                         WHERE t.deleted_at IS NULL AND t.payment_status = 'failed'
                                           AND EXTRACT(YEAR FROM t.created_at) BETWEEN (SELECT start_year FROM year_range) AND (SELECT end_year FROM year_range)
                                         GROUP BY EXTRACT(YEAR FROM t.created_at), t.payment_method
                                     )
                                SELECT ac.year, ac.payment_method, COALESCE(yt.total_transactions, 0) AS total_transactions, COALESCE(yt.total_amount, 0) AS total_amount
                                FROM all_combinations ac LEFT JOIN yearly_transactions yt ON ac.year = yt.year AND ac.payment_method = yt.payment_method
                                ORDER BY ac.year, ac.payment_method;
                                """)
                .execute(Tuple.of(refTs))
                .map(this::mapYearlyMethod);
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
                    r.getString("year"), r.getString("month"), r.getInteger("total_success"),
                    r.getInteger("total_amount")));
        }
        return list;
    }

    private List<TransactionYearlyAmountSuccess> mapYearlySuccess(RowSet<Row> rows) {
        List<TransactionYearlyAmountSuccess> list = new ArrayList<>();
        for (Row r : rows) {
            list.add(new TransactionYearlyAmountSuccess(
                    r.getString("year"), r.getInteger("total_success"), r.getInteger("total_amount")));
        }
        return list;
    }

    private List<TransactionMonthlyAmountFailed> mapMonthlyFailed(RowSet<Row> rows) {
        List<TransactionMonthlyAmountFailed> list = new ArrayList<>();
        for (Row r : rows) {
            list.add(new TransactionMonthlyAmountFailed(
                    r.getString("year"), r.getString("month"), r.getInteger("total_failed"),
                    r.getInteger("total_amount")));
        }
        return list;
    }

    private List<TransactionYearlyAmountFailed> mapYearlyFailed(RowSet<Row> rows) {
        List<TransactionYearlyAmountFailed> list = new ArrayList<>();
        for (Row r : rows) {
            list.add(new TransactionYearlyAmountFailed(
                    r.getString("year"), r.getInteger("total_failed"), r.getInteger("total_amount")));
        }
        return list;
    }

    private List<TransactionMonthlyMethod> mapMonthlyMethod(RowSet<Row> rows) {
        List<TransactionMonthlyMethod> list = new ArrayList<>();
        for (Row r : rows) {
            list.add(new TransactionMonthlyMethod(
                    r.getString("month"), r.getString("payment_method"), r.getInteger("total_transactions"),
                    getLongFromDecimal(r, "total_amount")));
        }
        return list;
    }

    private List<TransactionYearlyMethod> mapYearlyMethod(RowSet<Row> rows) {
        List<TransactionYearlyMethod> list = new ArrayList<>();
        for (Row r : rows) {
            list.add(new TransactionYearlyMethod(
                    r.getString("year"), r.getString("payment_method"), r.getInteger("total_transactions"),
                    getLongFromDecimal(r, "total_amount")));
        }
        return list;
    }
}
