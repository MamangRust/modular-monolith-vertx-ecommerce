package io.example.shipping_address.repository.impl;

import io.example.shipping_address.domain.requests.CreateShippingAddressRequest;
import io.example.shipping_address.model.ShippingAddress;
import io.example.shipping_address.domain.requests.UpdateShippingAddressRequest;
import io.example.shipping_address.repository.ShippingAddressCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShippingAddressCommandRepositoryImpl implements ShippingAddressCommandRepository {
    private final Pool client;

    @Override
    public Future<ShippingAddress> createShippingAddress(CreateShippingAddressRequest req) {
        return client
                .preparedQuery("""
                        INSERT INTO
                            shipping_addresses (
                                order_id,
                                alamat,
                                provinsi,
                                negara,
                                kota,
                                courier,
                                shipping_method,
                                shipping_cost
                            )
                        VALUES (
                                $1,
                                $2,
                                $3,
                                $4,
                                $5,
                                $6,
                                $7,
                                $8
                            )
                        RETURNING
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
                        """)
                .execute(Tuple.of(
                        req.getOrderId(),
                        req.getAlamat(),
                        req.getProvinsi(),
                        req.getNegara(),
                        req.getKota(),
                        req.getCourier(),
                        req.getShippingMethod(),
                        req.getShippingCost()))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<ShippingAddress> updateShippingAddress(UpdateShippingAddressRequest req) {
        return client
                .preparedQuery("""
                        UPDATE shipping_addresses
                        SET
                            alamat = COALESCE(NULLIF($2, ''), alamat),
                            provinsi = COALESCE(NULLIF($3, ''), provinsi),
                            negara = COALESCE(NULLIF($4, ''), negara),
                            kota = COALESCE(NULLIF($5, ''), kota),
                            courier = COALESCE(NULLIF($6, ''), courier),
                            shipping_method = COALESCE(NULLIF($7, ''), shipping_method),
                            shipping_cost = COALESCE(NULLIF($8::INT, 0), shipping_cost),
                            updated_at = CURRENT_TIMESTAMP
                        WHERE
                            shipping_address_id = $1
                            AND deleted_at IS NULL
                        RETURNING
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
                        """)
                .execute(Tuple.of(
                        req.getShippingId(),
                        req.getAlamat(),
                        req.getProvinsi(),
                        req.getNegara(),
                        req.getKota(),
                        req.getCourier(),
                        req.getShippingMethod(),
                        req.getShippingCost()))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<ShippingAddress> trashShippingAddress(Long shippingAddressId) {
        return client
                .preparedQuery("""
                        UPDATE shipping_addresses
                        SET
                            deleted_at = CURRENT_TIMESTAMP
                        WHERE
                            shipping_address_id = $1
                            AND deleted_at IS NULL
                        RETURNING
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
                        """)
                .execute(Tuple.of(shippingAddressId))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<ShippingAddress> restoreShippingAddress(Long shippingAddressId) {
        return client
                .preparedQuery("""
                        UPDATE shipping_addresses
                        SET
                            deleted_at = NULL
                        WHERE
                            shipping_address_id = $1
                            AND deleted_at IS NOT NULL
                        RETURNING
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
                        """)
                .execute(Tuple.of(shippingAddressId))
                .map(this::mapSingleOrNull);
    }

    @Override
    public Future<Boolean> deleteShippingAddressPermanently(Long shippingAddressId) {
        return client
                .preparedQuery("""
                        DELETE FROM shipping_addresses
                        WHERE
                            shipping_address_id = $1
                            AND deleted_at IS NOT NULL
                        """)
                .execute(Tuple.of(shippingAddressId))
                .map(row -> row.rowCount() > 0);
    }

    @Override
    public Future<Boolean> deleteByOrderIDPermanent(Long orderId) {
        return client
                .preparedQuery("""
                        DELETE FROM shipping_addresses
                        WHERE
                            order_id = $1
                        """)
                .execute(Tuple.of(orderId))
                .map(row -> row.rowCount() > 0);
    }

    @Override
    public Future<Integer> restoreAllShippingAddress() {
        return client
                .preparedQuery("""
                        UPDATE shipping_addresses
                        SET
                            deleted_at = NULL
                        WHERE
                            deleted_at IS NOT NULL
                        """)
                .execute()
                .map(RowSet::rowCount);
    }

    @Override
    public Future<Integer> deleteAllPermanentShippingAddress() {
        return client
                .preparedQuery("DELETE FROM shipping_addresses WHERE deleted_at IS NOT NULL")
                .execute()
                .map(RowSet::rowCount);
    }

    private ShippingAddress mapSingleOrNull(RowSet<Row> rows) {
        return rows.iterator().hasNext() ? ShippingAddress.fromRow(rows.iterator().next()) : null;
    }
}
