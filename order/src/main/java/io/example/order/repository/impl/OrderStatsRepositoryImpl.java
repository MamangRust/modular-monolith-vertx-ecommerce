package io.example.order.repository.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.example.order.model.OrderMonthly;
import io.example.order.model.OrderMonthlyTotalRevenue;
import io.example.order.model.OrderYearly;
import io.example.order.model.OrderYearlyTotalRevenue;
import io.example.order.repository.OrderStatsRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class OrderStatsRepositoryImpl implements OrderStatsRepository {
    private final Pool client;

    public OrderStatsRepositoryImpl(Pool client) {
        this.client = client;
    }

    private Tuple getMonthlyDateRange(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        LocalDate startPrev = start.minusYears(1);
        LocalDate endPrev = startPrev.withDayOfMonth(startPrev.lengthOfMonth());
        return Tuple.of(start, end, startPrev, endPrev);
    }

    @Override
    public Future<List<OrderMonthlyTotalRevenue>> getMonthlyTotalRevenue(int year, int month) {
        return client.preparedQuery("""
                WITH monthly_revenue AS (
                    SELECT EXTRACT(YEAR FROM o.created_at)::TEXT AS year, EXTRACT(MONTH FROM o.created_at)::integer AS month,
                           COALESCE(SUM(o.total_price), 0)::INTEGER AS total_revenue
                    FROM orders o JOIN order_items oi ON o.order_id = oi.order_id
                    WHERE o.deleted_at IS NULL AND oi.deleted_at IS NULL
                      AND ( (o.created_at >= $1 AND o.created_at <= $2) OR (o.created_at >= $3 AND o.created_at <= $4) )
                    GROUP BY EXTRACT(YEAR FROM o.created_at), EXTRACT(MONTH FROM o.created_at)
                ), all_months AS (
                    SELECT EXTRACT(YEAR FROM $1)::TEXT AS year, EXTRACT(MONTH FROM $1)::integer AS month, TO_CHAR($1, 'FMMonth') AS month_name
                    UNION
                    SELECT EXTRACT(YEAR FROM $3)::TEXT AS year, EXTRACT(MONTH FROM $3)::integer AS month, TO_CHAR($3, 'FMMonth') AS month_name
                )
                SELECT COALESCE(am.year, EXTRACT(YEAR FROM $1)::TEXT) AS year,
                       COALESCE(am.month_name, TO_CHAR($1, 'FMMonth')) AS month,
                       COALESCE(mr.total_revenue, 0) AS total_revenue
                FROM all_months am LEFT JOIN monthly_revenue mr ON am.year = mr.year AND am.month = mr.month
                ORDER BY am.year DESC, am.month DESC;
                """)
                .execute(getMonthlyDateRange(year, month))
                .map(this::mapMonthlyTotalRevenue);
    }

    @Override
    public Future<List<OrderYearlyTotalRevenue>> getYearlyTotalRevenue(int year) {
        return client.preparedQuery("""
                WITH yearly_revenue AS (
                    SELECT EXTRACT(YEAR FROM o.created_at)::integer AS year, COALESCE(SUM(o.total_price), 0)::INTEGER AS total_revenue
                    FROM orders o JOIN order_items oi ON o.order_id = oi.order_id
                    WHERE o.deleted_at IS NULL AND oi.deleted_at IS NULL
                      AND (EXTRACT(YEAR FROM o.created_at) = $1::integer OR EXTRACT(YEAR FROM o.created_at) = $1::integer - 1)
                    GROUP BY EXTRACT(YEAR FROM o.created_at)
                ), all_years AS ( SELECT $1 AS year UNION SELECT $1 - 1 AS year )
                SELECT ay.year::text AS year, COALESCE(yr.total_revenue, 0) AS total_revenue
                FROM all_years ay LEFT JOIN yearly_revenue yr ON ay.year = yr.year ORDER BY ay.year DESC;
                """)
                .execute(Tuple.of(year))
                .map(this::mapYearlyTotalRevenue);
    }

    @Override
    public Future<List<OrderMonthlyTotalRevenue>> getMonthlyTotalRevenueById(Long orderId, int year, int month) {
        Tuple args = getMonthlyDateRange(year, month).addLong(orderId);
        return client.preparedQuery("""
                WITH monthly_revenue AS (
                    SELECT EXTRACT(YEAR FROM o.created_at)::TEXT AS year, EXTRACT(MONTH FROM o.created_at)::integer AS month,
                           COALESCE(SUM(o.total_price), 0)::INTEGER AS total_revenue
                    FROM orders o JOIN order_items oi ON o.order_id = oi.order_id
                    WHERE o.deleted_at IS NULL AND oi.deleted_at IS NULL
                      AND ( (o.created_at >= $1 AND o.created_at <= $2) OR (o.created_at >= $3 AND o.created_at <= $4) )
                      AND o.order_id = $5
                    GROUP BY EXTRACT(YEAR FROM o.created_at), EXTRACT(MONTH FROM o.created_at)
                ), all_months AS (
                    SELECT EXTRACT(YEAR FROM $1)::TEXT AS year, EXTRACT(MONTH FROM $1)::integer AS month, TO_CHAR($1, 'FMMonth') AS month_name
                    UNION SELECT EXTRACT(YEAR FROM $3)::TEXT AS year, EXTRACT(MONTH FROM $3)::integer AS month, TO_CHAR($3, 'FMMonth') AS month_name
                )
                SELECT COALESCE(am.year, EXTRACT(YEAR FROM $1)::TEXT) AS year,
                       COALESCE(am.month_name, TO_CHAR($1, 'FMMonth')) AS month,
                       COALESCE(mr.total_revenue, 0) AS total_revenue
                FROM all_months am LEFT JOIN monthly_revenue mr ON am.year = mr.year AND am.month = mr.month
                ORDER BY am.year DESC, am.month DESC;
                """)
                .execute(args)
                .map(this::mapMonthlyTotalRevenue);
    }

    @Override
    public Future<List<OrderYearlyTotalRevenue>> getYearlyTotalRevenueById(Long orderId, int year) {
        return client.preparedQuery("""
                WITH yearly_revenue AS (
                    SELECT EXTRACT(YEAR FROM o.created_at)::integer AS year, COALESCE(SUM(o.total_price), 0)::INTEGER AS total_revenue
                    FROM orders o JOIN order_items oi ON o.order_id = oi.order_id
                    WHERE o.deleted_at IS NULL AND oi.deleted_at IS NULL
                      AND (EXTRACT(YEAR FROM o.created_at) = $1::integer OR EXTRACT(YEAR FROM o.created_at) = $1::integer - 1)
                      AND o.order_id = $2
                    GROUP BY EXTRACT(YEAR FROM o.created_at)
                ), all_years AS ( SELECT $1 AS year UNION SELECT $1 - 1 AS year )
                SELECT ay.year::text AS year, COALESCE(yr.total_revenue, 0) AS total_revenue
                FROM all_years ay LEFT JOIN yearly_revenue yr ON ay.year = yr.year ORDER BY ay.year DESC;
                """)
                .execute(Tuple.of(year, orderId))
                .map(this::mapYearlyTotalRevenue);
    }

    @Override
    public Future<List<OrderMonthlyTotalRevenue>> getMonthlyTotalRevenueByMerchant(Integer merchantId, int year, int month) {
        Tuple args = getMonthlyDateRange(year, month).addInteger(merchantId);
        return client.preparedQuery("""
                WITH monthly_revenue AS (
                    SELECT EXTRACT(YEAR FROM o.created_at)::TEXT AS year, EXTRACT(MONTH FROM o.created_at)::integer AS month,
                           COALESCE(SUM(o.total_price), 0)::INTEGER AS total_revenue
                    FROM orders o JOIN order_items oi ON o.order_id = oi.order_id
                    WHERE o.deleted_at IS NULL AND oi.deleted_at IS NULL
                      AND ( (o.created_at >= $1 AND o.created_at <= $2) OR (o.created_at >= $3 AND o.created_at <= $4) )
                      AND o.merchant_id = $5
                    GROUP BY EXTRACT(YEAR FROM o.created_at), EXTRACT(MONTH FROM o.created_at)
                ), all_months AS (
                    SELECT EXTRACT(YEAR FROM $1)::TEXT AS year, EXTRACT(MONTH FROM $1)::integer AS month, TO_CHAR($1, 'FMMonth') AS month_name
                    UNION SELECT EXTRACT(YEAR FROM $3)::TEXT AS year, EXTRACT(MONTH FROM $3)::integer AS month, TO_CHAR($3, 'FMMonth') AS month_name
                )
                SELECT COALESCE(am.year, EXTRACT(YEAR FROM $1)::TEXT) AS year,
                       COALESCE(am.month_name, TO_CHAR($1, 'FMMonth')) AS month,
                       COALESCE(mr.total_revenue, 0) AS total_revenue
                FROM all_months am LEFT JOIN monthly_revenue mr ON am.year = mr.year AND am.month = mr.month
                ORDER BY am.year DESC, am.month DESC;
                """)
                .execute(args)
                .map(this::mapMonthlyTotalRevenue);
    }

    @Override
    public Future<List<OrderYearlyTotalRevenue>> getYearlyTotalRevenueByMerchant(Integer merchantId, int year) {
        return client.preparedQuery("""
                WITH yearly_revenue AS (
                    SELECT EXTRACT(YEAR FROM o.created_at)::integer AS year, COALESCE(SUM(o.total_price), 0)::INTEGER AS total_revenue
                    FROM orders o JOIN order_items oi ON o.order_id = oi.order_id
                    WHERE o.deleted_at IS NULL AND oi.deleted_at IS NULL
                      AND (EXTRACT(YEAR FROM o.created_at) = $1::integer OR EXTRACT(YEAR FROM o.created_at) = $1::integer - 1)
                      AND o.merchant_id = $2
                    GROUP BY EXTRACT(YEAR FROM o.created_at)
                ), all_years AS ( SELECT $1 AS year UNION SELECT $1 - 1 AS year )
                SELECT ay.year::text AS year, COALESCE(yr.total_revenue, 0) AS total_revenue
                FROM all_years ay LEFT JOIN yearly_revenue yr ON ay.year = yr.year ORDER BY ay.year DESC;
                """)
                .execute(Tuple.of(year, merchantId))
                .map(this::mapYearlyTotalRevenue);
    }

    @Override
    public Future<List<OrderMonthly>> getMonthlyOrder(int year) {
        Timestamp refTs = Timestamp.valueOf(LocalDateTime.of(year, 1, 1, 0, 0));
        return client.preparedQuery("""
                WITH date_range AS (
                    SELECT date_trunc('month', $1::timestamp) AS start_date,
                           date_trunc('month', $1::timestamp) + interval '1 year' - interval '1 day' AS end_date
                ), monthly_orders AS (
                    SELECT date_trunc('month', o.created_at) AS activity_month,
                           COUNT(o.order_id) AS order_count, SUM(o.total_price)::NUMERIC AS total_revenue, SUM(oi.quantity) AS total_items_sold
                    FROM orders o JOIN order_items oi ON o.order_id = oi.order_id
                    WHERE o.deleted_at IS NULL AND oi.deleted_at IS NULL
                      AND o.created_at BETWEEN (SELECT start_date FROM date_range) AND (SELECT end_date FROM date_range)
                    GROUP BY activity_month
                )
                SELECT TO_CHAR(mo.activity_month, 'Mon') AS month, mo.order_count, mo.total_revenue, mo.total_items_sold
                FROM monthly_orders mo ORDER BY mo.activity_month;
                """)
                .execute(Tuple.of(refTs))
                .map(this::mapMonthlyOrder);
    }

    @Override
    public Future<List<OrderYearly>> getYearlyOrder(int year) {
        Timestamp refTs = Timestamp.valueOf(LocalDateTime.of(year, 1, 1, 0, 0));
        return client.preparedQuery("""
                WITH last_five_years AS (
                    SELECT EXTRACT(YEAR FROM o.created_at)::text AS year,
                           COUNT(o.order_id) AS order_count, SUM(o.total_price)::NUMERIC AS total_revenue,
                           SUM(oi.quantity) AS total_items_sold, COUNT(DISTINCT oi.product_id) AS unique_products_sold
                    FROM orders o JOIN order_items oi ON o.order_id = oi.order_id
                    WHERE o.deleted_at IS NULL AND oi.deleted_at IS NULL
                      AND EXTRACT(YEAR FROM o.created_at) BETWEEN (EXTRACT(YEAR FROM $1::timestamp) - 4) AND EXTRACT(YEAR FROM $1::timestamp)
                    GROUP BY EXTRACT(YEAR FROM o.created_at)
                )
                SELECT year, order_count, total_revenue, total_items_sold, unique_products_sold
                FROM last_five_years ORDER BY year;
                """)
                .execute(Tuple.of(refTs))
                .map(this::mapYearlyOrder);
    }

    @Override
    public Future<List<OrderMonthly>> getMonthlyOrderByMerchant(Integer merchantId, int year) {
        Timestamp refTs = Timestamp.valueOf(LocalDateTime.of(year, 1, 1, 0, 0));
        return client.preparedQuery("""
                WITH date_range AS (
                    SELECT date_trunc('month', $1::timestamp) AS start_date,
                           date_trunc('month', $1::timestamp) + interval '1 year' - interval '1 day' AS end_date
                ), monthly_orders AS (
                    SELECT date_trunc('month', o.created_at) AS activity_month,
                           COUNT(o.order_id) AS order_count, SUM(o.total_price)::NUMERIC AS total_revenue, SUM(oi.quantity) AS total_items_sold
                    FROM orders o JOIN order_items oi ON o.order_id = oi.order_id
                    WHERE o.deleted_at IS NULL AND oi.deleted_at IS NULL
                      AND o.created_at BETWEEN (SELECT start_date FROM date_range) AND (SELECT end_date FROM date_range)
                      AND o.merchant_id = $2
                    GROUP BY activity_month
                )
                SELECT TO_CHAR(mo.activity_month, 'Mon') AS month, mo.order_count, mo.total_revenue, mo.total_items_sold
                FROM monthly_orders mo ORDER BY mo.activity_month;
                """)
                .execute(Tuple.of(refTs, merchantId))
                .map(this::mapMonthlyOrder);
    }

    @Override
    public Future<List<OrderYearly>> getYearlyOrderByMerchant(Integer merchantId, int year) {
        Timestamp refTs = Timestamp.valueOf(LocalDateTime.of(year, 1, 1, 0, 0));
        return client.preparedQuery("""
                WITH last_five_years AS (
                    SELECT EXTRACT(YEAR FROM o.created_at)::text AS year,
                           COUNT(o.order_id) AS order_count, SUM(o.total_price)::NUMERIC AS total_revenue,
                           SUM(oi.quantity) AS total_items_sold, COUNT(DISTINCT oi.product_id) AS unique_products_sold
                    FROM orders o JOIN order_items oi ON o.order_id = oi.order_id
                    WHERE o.deleted_at IS NULL AND oi.deleted_at IS NULL
                      AND EXTRACT(YEAR FROM o.created_at) BETWEEN (EXTRACT(YEAR FROM $1::timestamp) - 4) AND EXTRACT(YEAR FROM $1::timestamp)
                      AND o.merchant_id = $2
                    GROUP BY EXTRACT(YEAR FROM o.created_at)
                )
                SELECT year, order_count, total_revenue, total_items_sold, unique_products_sold
                FROM last_five_years ORDER BY year;
                """)
                .execute(Tuple.of(refTs, merchantId))
                .map(this::mapYearlyOrder);
    }

    private List<OrderMonthlyTotalRevenue> mapMonthlyTotalRevenue(RowSet<Row> rows) {
        List<OrderMonthlyTotalRevenue> list = new ArrayList<>();
        for (Row r : rows) {
            list.add(new OrderMonthlyTotalRevenue(
                    r.getString("year"),
                    r.getString("month"),
                    r.getLong("total_revenue")));
        }
        return list;
    }

    private List<OrderYearlyTotalRevenue> mapYearlyTotalRevenue(RowSet<Row> rows) {
        List<OrderYearlyTotalRevenue> list = new ArrayList<>();
        for (Row r : rows) {
            list.add(new OrderYearlyTotalRevenue(
                    r.getString("year"),
                    r.getLong("total_revenue")));
        }
        return list;
    }

    private List<OrderMonthly> mapMonthlyOrder(RowSet<Row> rows) {
        List<OrderMonthly> list = new ArrayList<>();
        for (Row r : rows) {
            list.add(new OrderMonthly(
                    r.getString("month"),
                    r.getInteger("order_count"),
                    r.get(BigDecimal.class, "total_revenue") != null
                            ? r.get(BigDecimal.class, "total_revenue").longValue()
                            : 0L,
                    r.getInteger("total_items_sold")));
        }
        return list;
    }

    private List<OrderYearly> mapYearlyOrder(RowSet<Row> rows) {
        List<OrderYearly> list = new ArrayList<>();
        for (Row r : rows) {
            list.add(new OrderYearly(
                    r.getString("year"),
                    r.getInteger("order_count"),
                    r.get(BigDecimal.class, "total_revenue") != null
                            ? r.get(BigDecimal.class, "total_revenue").longValue()
                            : 0L,
                    r.getInteger("total_items_sold"),
                    null,
                    r.getInteger("unique_products_sold")));
        }
        return list;
    }
}
