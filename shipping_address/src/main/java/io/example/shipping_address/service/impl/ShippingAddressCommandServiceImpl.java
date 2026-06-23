package io.example.shipping_address.service.impl;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.shipping_address.domain.requests.CreateShippingAddressRequest;
import io.example.shipping_address.domain.requests.UpdateShippingAddressRequest;
import io.example.shipping_address.model.ShippingAddressResponse;
import io.example.shipping_address.model.ShippingAddressResponseDeleteAt;
import io.example.shipping_address.repository.ShippingAddressCommandRepository;
import io.example.shipping_address.repository.ShippingAddressQueryRepository;
import io.example.shipping_address.service.ShippingAddressCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShippingAddressCommandServiceImpl implements ShippingAddressCommandService {
        private final ShippingAddressCommandRepository repo;
        private final ShippingAddressQueryRepository queryRepo;
        private final RedisService redis;
        private final TracingMetrics metrics;

        private static final String CACHE_PREFIX = "shipping_address:";

        private Future<Void> evict(Long id, Integer orderId) {
                return redis.delete(CACHE_PREFIX + id)
                                .compose(v -> redis.delete(CACHE_PREFIX + "order:" + orderId))
                                .<Void>mapEmpty();
        }

        private Future<Void> evictAll() {
                return redis.deleteByPattern(CACHE_PREFIX + "*").<Void>mapEmpty();
        }

        @Override
        public Future<ShippingAddressResponse> createShippingAddress(CreateShippingAddressRequest req) {
                var ctx = metrics.startSpan("ShippingAddressCommandService.createShippingAddress",
                                Attributes.builder().put("shipping_address.order_id", req.getOrderId()).build());

                return repo.createShippingAddress(req)
                                .map(ShippingAddressResponse::from)
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "create",
                                                "Shipping address created successfully"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "create", e.getMessage()));
        }

        @Override
        public Future<ShippingAddressResponse> updateShippingAddress(UpdateShippingAddressRequest req) {
                Integer shippingId = req.getShippingId();
                var ctx = metrics.startSpan("ShippingAddressCommandService.updateShippingAddress",
                                Attributes.builder().put("shipping_address.id", shippingId).build());

                return repo.updateShippingAddress(req)
                                .compose(address -> {
                                        if (address == null) {
                                                return Future.failedFuture(
                                                                new NotFoundException("Shipping address not found"));
                                        }
                                        return evict((long) shippingId, address.getOrderId()).map(v -> address);
                                })
                                .map(ShippingAddressResponse::from)
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "update",
                                                "Shipping address updated successfully"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "update", e.getMessage()));
        }

        @Override
        public Future<ShippingAddressResponseDeleteAt> trashShippingAddress(Long shippingAddressId) {
                var ctx = metrics.startSpan("ShippingAddressCommandService.trashShippingAddress",
                                Attributes.builder().put("shipping_address.id", shippingAddressId).build());

                return repo.trashShippingAddress(shippingAddressId)
                                .compose(address -> {
                                        if (address == null) {
                                                return Future.failedFuture(
                                                                new NotFoundException("Shipping address not found"));
                                        }
                                        return evict(shippingAddressId, address.getOrderId()).map(v -> address);
                                })
                                .map(ShippingAddressResponseDeleteAt::from)
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "trash",
                                                "Shipping address trashed successfully"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "trash", e.getMessage()));
        }

        @Override
        public Future<ShippingAddressResponseDeleteAt> restoreShippingAddress(Long shippingAddressId) {
                var ctx = metrics.startSpan("ShippingAddressCommandService.restoreShippingAddress",
                                Attributes.builder().put("shipping_address.id", shippingAddressId).build());

                return queryRepo.findByTrashedId(shippingAddressId)
                                .compose(trashed -> {
                                        if (trashed == null) {
                                                return Future.failedFuture(new NotFoundException(
                                                                "Shipping address not found or not in trashed state"));
                                        }
                                        return repo.restoreShippingAddress(shippingAddressId)
                                                        .compose(address -> {
                                                                if (address == null) {
                                                                        return Future.failedFuture(
                                                                                        new NotFoundException(
                                                                                                        "Shipping address not found"));
                                                                }
                                                                return evict(shippingAddressId, address.getOrderId())
                                                                                .map(v -> address);
                                                        });
                                })
                                .map(ShippingAddressResponseDeleteAt::from)
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore",
                                                "Shipping address restored successfully"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "restore", e.getMessage()));
        }

        @Override
        public Future<Void> deleteShippingAddressPermanently(Long shippingAddressId) {
                var ctx = metrics.startSpan("ShippingAddressCommandService.deletePermanent",
                                Attributes.builder().put("shipping_address.id", shippingAddressId).build());

                return queryRepo.findByTrashedId(shippingAddressId)
                                .compose(trashed -> {
                                        if (trashed == null) {
                                                return Future.<Void>failedFuture(
                                                                new BadRequestException(
                                                                                "Shipping address not found or must be trashed before permanent deletion"));
                                        }
                                        return repo.deleteShippingAddressPermanently(shippingAddressId)
                                                        .compose(v -> evictAll());
                                })
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deletePermanent",
                                                "Shipping address deleted permanently"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "deletePermanent", e.getMessage()));
        }

        @Override
        public Future<Void> deleteShippingAddressByOrderPermanent(Long orderId) {
                var ctx = metrics.startSpan("ShippingAddressCommandService.deleteByOrderPermanent",
                                Attributes.builder().put("shipping_address.order_id", orderId).build());

                return repo.deleteByOrderIDPermanent(orderId)
                                .compose(v -> redis.delete(CACHE_PREFIX + "order:" + orderId).<Void>mapEmpty())
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deleteByOrderPermanent",
                                                "Shipping address for order deleted permanently"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "deleteByOrderPermanent",
                                                e.getMessage()));
        }

        @Override
        public Future<Void> restoreAllShippingAddresses() {
                var ctx = metrics.startSpan("ShippingAddressCommandService.restoreAll");

                return repo.restoreAllShippingAddress()
                                .compose(count -> {
                                        if (count == 0) {
                                                return Future.<Void>failedFuture(new NotFoundException(
                                                                "No trashed shipping addresses found"));
                                        }
                                        return evictAll();
                                })
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore_all",
                                                "All shipping addresses restored"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "restore_all", e.getMessage()));
        }

        @Override
        public Future<Void> deleteAllPermanentShippingAddresses() {
                var ctx = metrics.startSpan("ShippingAddressCommandService.deleteAllPermanent");

                return repo.deleteAllPermanentShippingAddress()
                                .compose(count -> {
                                        if (count == 0) {
                                                return Future.<Void>failedFuture(new NotFoundException(
                                                                "No trashed shipping addresses found"));
                                        }
                                        return evictAll();
                                })
                                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "delete_all_permanent",
                                                "All shipping addresses deleted permanently"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "delete_all_permanent", e.getMessage()));
        }
}