package io.example.shipping_address.repository.impl;

import io.example.shipping_address.model.CreateShippingAddressRequest;
import io.example.shipping_address.model.ShippingAddress;
import io.example.shipping_address.model.UpdateShippingAddressRequest;
import io.example.shipping_address.repository.ShippingAddressCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class ShippingAddressCommandRepositoryImpl implements ShippingAddressCommandRepository {
    private final Pool client;

    public ShippingAddressCommandRepositoryImpl(Pool client) {
        this.client = client;
    }

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
                            alamat = $2,
                            provinsi = $3,
                            negara = $4,
                            kota = $5,
                            courier = $6,
                            shipping_method = $7,
                            shipping_cost = $8,
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
    public Future<ShippingAddress> trashShippingAddress(Integer shippingAddressId) {
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
    public Future<ShippingAddress> restoreShippingAddress(Integer shippingAddressId) {
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
    public Future<Void> deleteShippingAddressPermanently(Integer shippingAddressId) {
        return client
                .preparedQuery("""
                        DELETE FROM shipping_addresses
                        WHERE
                            shipping_address_id = $1
                            AND deleted_at IS NOT NULL
                        """)
                .execute(Tuple.of(shippingAddressId))
                .mapEmpty();
    }

    @Override
    public Future<Void> deleteByOrderIDPermanent(Integer orderId) {
        return client
                .preparedQuery("""
                        DELETE FROM shipping_addresses
                        WHERE
                            order_id = $1
                        """)
                .execute(Tuple.of(orderId))
                .mapEmpty();
    }

    @Override
    public Future<Void> restoreAllShippingAddress() {
        return client
                .preparedQuery("""
                        UPDATE shipping_addresses
                        SET
                            deleted_at = NULL
                        WHERE
                            deleted_at IS NOT NULL
                        """)
                .execute()
                .mapEmpty();
    }

    @Override
    public Future<Void> deleteAllPermanentShippingAddress() {
        return client
                .preparedQuery("DELETE FROM shipping_addresses WHERE deleted_at IS NOT NULL")
                .execute()
                .mapEmpty();
    }

    private ShippingAddress mapSingleOrNull(RowSet<Row> rows) {
        return rows.iterator().hasNext() ? ShippingAddress.fromRow(rows.iterator().next()) : null;
    }
}
