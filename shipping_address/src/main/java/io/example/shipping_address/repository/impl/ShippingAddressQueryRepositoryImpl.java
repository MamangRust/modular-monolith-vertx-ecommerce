package io.example.shipping_address.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.shipping_address.domain.requests.FindAllShippingAddress;
import io.example.shipping_address.model.ShippingAddress;
import io.example.shipping_address.repository.ShippingAddressQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShippingAddressQueryRepositoryImpl implements ShippingAddressQueryRepository {
    private final Pool client;

    @Override
    public Future<PagedResult<ShippingAddress>> getShippingAddresses(FindAllShippingAddress req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

        return client
                .preparedQuery("""
                        SELECT
                            shipping_address_id,
                            order_id,
                            alamat,
                            provinsi,
                            negara,
                            kota,
                            courier,
                            shipping_method,
                            shipping_cost,
                            created_at,
                            updated_at,
                            COUNT(*) OVER () AS total_count
                        FROM shipping_addresses
                        WHERE
                            deleted_at IS NULL
                            AND (
                                $1::TEXT IS NULL
                                OR shipping_address_id::TEXT ILIKE '%' || $1 || '%'
                                OR alamat ILIKE '%' || $1 || '%'
                            )
                        ORDER BY created_at DESC
                        LIMIT $2
                        OFFSET $3
                        """)
                .execute(Tuple.of(
                        normalizeSearch(req.getSearch()),
                        req.getPageSize(),
                        offset))
                .map(this::mapPagedShippingAddresses);
    }

    @Override
    public Future<PagedResult<ShippingAddress>> getShippingAddressActive(FindAllShippingAddress req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

        return client
                .preparedQuery("""
                        SELECT
                            shipping_address_id,
                            order_id,
                            alamat,
                            provinsi,
                            negara,
                            kota,
                            courier,
                            shipping_method,
                            shipping_cost,
                            created_at,
                            updated_at,
                            deleted_at,
                            COUNT(*) OVER () AS total_count
                        FROM shipping_addresses
                        WHERE
                            deleted_at IS NULL
                            AND (
                                $1::TEXT IS NULL
                                OR shipping_address_id::TEXT ILIKE '%' || $1 || '%'
                                OR alamat ILIKE '%' || $1 || '%'
                            )
                        ORDER BY created_at DESC
                        LIMIT $2
                        OFFSET $3
                        """)
                .execute(Tuple.of(
                        normalizeSearch(req.getSearch()),
                        req.getPageSize(),
                        offset))
                .map(this::mapPagedShippingAddresses);
    }

    @Override
    public Future<PagedResult<ShippingAddress>> getShippingAddressTrashed(FindAllShippingAddress req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

        return client
                .preparedQuery("""
                        SELECT
                            shipping_address_id,
                            order_id,
                            alamat,
                            provinsi,
                            negara,
                            kota,
                            courier,
                            shipping_method,
                            shipping_cost,
                            created_at,
                            updated_at,
                            deleted_at,
                            COUNT(*) OVER () AS total_count
                        FROM shipping_addresses
                        WHERE
                            deleted_at IS NOT NULL
                            AND (
                                $1::TEXT IS NULL
                                OR shipping_address_id::TEXT ILIKE '%' || $1 || '%'
                                OR alamat ILIKE '%' || $1 || '%'
                            )
                        ORDER BY created_at DESC
                        LIMIT $2
                        OFFSET $3
                        """)
                .execute(Tuple.of(
                        normalizeSearch(req.getSearch()),
                        req.getPageSize(),
                        offset))
                .map(this::mapPagedShippingAddresses);
    }

    @Override
    public Future<ShippingAddress> getShippingByID(Long shippingAddressId) {
        return client
                .preparedQuery("""
                        SELECT
                            shipping_address_id,
                            order_id,
                            alamat,
                            provinsi,
                            negara,
                            kota,
                            courier,
                            shipping_method,
                            shipping_cost,
                            created_at,
                            updated_at
                        FROM shipping_addresses
                        WHERE
                            shipping_address_id = $1
                            AND deleted_at IS NULL
                        """)
                .execute(Tuple.of(shippingAddressId))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<ShippingAddress> getShippingAddressByOrderID(Long orderId) {
        return client
                .preparedQuery("""
                        SELECT
                            shipping_address_id,
                            order_id,
                            alamat,
                            provinsi,
                            negara,
                            kota,
                            courier,
                            shipping_method,
                            shipping_cost,
                            created_at,
                            updated_at
                        FROM shipping_addresses
                        WHERE
                            order_id = $1
                            AND deleted_at IS NULL
                        """)
                .execute(Tuple.of(orderId))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<ShippingAddress> findByTrashedId(Long shippingAddressId) {
        return client
                .preparedQuery("""
                        SELECT
                            shipping_address_id,
                            order_id,
                            alamat,
                            provinsi,
                            negara,
                            kota,
                            courier,
                            shipping_method,
                            shipping_cost,
                            created_at,
                            updated_at,
                            deleted_at
                        FROM shipping_addresses
                        WHERE
                            shipping_address_id = $1
                            AND deleted_at IS NOT NULL
                        """)
                .execute(Tuple.of(shippingAddressId))
                .map(this::mapSingleOrNull);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search;
    }

    private ShippingAddress mapSingleOrNull(RowSet<Row> rows) {
        return rows.iterator().hasNext() ? ShippingAddress.fromRow(rows.iterator().next()) : null;
    }

    private PagedResult<ShippingAddress> mapPagedShippingAddresses(RowSet<Row> rows) {
        List<ShippingAddress> items = new ArrayList<>();
        int total = 0;

        for (Row row : rows) {
            items.add(ShippingAddress.fromRow(row));
            if (total == 0) {
                total = row.getInteger("total_count");
            }
        }

        return new PagedResult<>(items, total);
    }
}
