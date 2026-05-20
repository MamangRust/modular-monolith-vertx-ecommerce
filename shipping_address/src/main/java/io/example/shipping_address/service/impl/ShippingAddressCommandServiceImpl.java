package io.example.shipping_address.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.shipping_address.model.CreateShippingAddressRequest;
import io.example.shipping_address.model.ShippingAddress;
import io.example.shipping_address.model.ShippingAddressResponse;
import io.example.shipping_address.model.ShippingAddressResponseDeleteAt;
import io.example.shipping_address.model.UpdateShippingAddressRequest;
import io.example.shipping_address.repository.ShippingAddressCommandRepository;
import io.example.shipping_address.service.ShippingAddressCommandService;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;

public class ShippingAddressCommandServiceImpl implements ShippingAddressCommandService {
    private static final Logger logger = LoggerFactory.getLogger(ShippingAddressCommandServiceImpl.class);
    private final ShippingAddressCommandRepository repo;
    private final RedisService redisService;
    private final TracingMetrics tracingMetrics;

    public ShippingAddressCommandServiceImpl(
            ShippingAddressCommandRepository repo,
            RedisService redisService,
            TracingMetrics tracingMetrics) {
        this.repo = repo;
        this.redisService = redisService;
        this.tracingMetrics = tracingMetrics;
    }

    @Override
    public Future<ApiResponse<ShippingAddressResponse>> createShippingAddress(CreateShippingAddressRequest req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ShippingAddressService.createShippingAddress",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("shipping_address.order_id", req.getOrderId())
                        .build());
        Span span = Span.fromContext(tracingContext.getContext());

        logger.info("Creating shipping address for order: {}", req.getOrderId());

        return repo.createShippingAddress(req)
                .map(created -> {
                    span.setAttribute("shipping_address.id", created.getShippingAddressId());
                    tracingMetrics.completeSpanSuccess(tracingContext, "create", "Shipping address created successfully");
                    return ApiResponse.success("Shipping address created successfully", ShippingAddressResponse.from(created));
                })
                .recover(err -> {
                    logger.error("Failed to create shipping address", err);
                    tracingMetrics.completeSpanError(tracingContext, "create", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to create shipping address: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<ShippingAddressResponse>> updateShippingAddress(UpdateShippingAddressRequest req) {
        Integer shippingId = req.getShippingId();
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ShippingAddressService.updateShippingAddress",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("shipping_address.id", shippingId)
                        .build());

        logger.info("Updating shipping address: {}", shippingId);

        return repo.updateShippingAddress(req)
                .compose((ShippingAddress address) -> {
                    if (address == null) {
                        return Future.failedFuture(new RuntimeException("Shipping address not found or already deleted"));
                    }
                    String cacheId = "shipping_address:" + shippingId;
                    String cacheOrder = "shipping_address:order:" + address.getOrderId();
                    return redisService.delete(cacheId)
                            .compose(v -> redisService.delete(cacheOrder))
                            .onSuccess(deleted -> logger.debug("Shipping address {} cache invalidated", shippingId))
                            .onFailure(err -> logger.warn("Failed to invalidate cache for shipping address {}: {}", shippingId, err.getMessage()))
                            .map(address);
                })
                .map((ShippingAddress address) -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "update", "Shipping address updated successfully");
                    return ApiResponse.success("Shipping address updated successfully", ShippingAddressResponse.from(address));
                })
                .recover(err -> {
                    logger.error("Failed to update shipping address: {}", shippingId, err);
                    tracingMetrics.completeSpanError(tracingContext, "update", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to update shipping address: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<ShippingAddressResponseDeleteAt>> trashShippingAddress(Integer shippingAddressId) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ShippingAddressService.trashShippingAddress",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("shipping_address.id", shippingAddressId)
                        .build());

        logger.info("Trashing shipping address: {}", shippingAddressId);

        return repo.trashShippingAddress(shippingAddressId)
                .compose(address -> {
                    if (address == null) {
                        return Future.failedFuture(new RuntimeException("Shipping address not found or already trashed"));
                    }
                    String cacheId = "shipping_address:" + shippingAddressId;
                    String cacheOrder = "shipping_address:order:" + address.getOrderId();
                    return redisService.delete(cacheId)
                            .compose(v -> redisService.delete(cacheOrder))
                            .onSuccess(deleted -> logger.debug("Shipping address {} cache invalidated on trash", shippingAddressId))
                            .onFailure(err -> logger.warn("Failed to invalidate cache for trashed shipping address {}: {}", shippingAddressId, err.getMessage()))
                            .map(address);
                })
                .map(address -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "trashed", "Shipping address trashed successfully");
                    return ApiResponse.success("Shipping address trashed successfully", ShippingAddressResponseDeleteAt.from(address));
                })
                .recover(err -> {
                    logger.error("Failed to trash shipping address: {}", shippingAddressId, err);
                    tracingMetrics.completeSpanError(tracingContext, "trashed", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to trash shipping address: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<ShippingAddressResponseDeleteAt>> restoreShippingAddress(Integer shippingAddressId) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ShippingAddressService.restoreShippingAddress",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("shipping_address.id", shippingAddressId)
                        .build());

        logger.info("Restoring shipping address: {}", shippingAddressId);

        return repo.restoreShippingAddress(shippingAddressId)
                .compose(address -> {
                    if (address == null) {
                        return Future.failedFuture(new RuntimeException("Shipping address not found or not trashed"));
                    }
                    String cacheId = "shipping_address:" + shippingAddressId;
                    String cacheOrder = "shipping_address:order:" + address.getOrderId();
                    return redisService.delete(cacheId)
                            .compose(v -> redisService.delete(cacheOrder))
                            .onSuccess(deleted -> logger.debug("Shipping address {} cache invalidated on restore", shippingAddressId))
                            .onFailure(err -> logger.warn("Failed to invalidate cache for restored shipping address {}: {}", shippingAddressId, err.getMessage()))
                            .map(address);
                })
                .map(address -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "restore", "Shipping address restored successfully");
                    return ApiResponse.success("Shipping address restored successfully", ShippingAddressResponseDeleteAt.from(address));
                })
                .recover(err -> {
                    logger.error("Failed to restore shipping address: {}", shippingAddressId, err);
                    tracingMetrics.completeSpanError(tracingContext, "restore", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to restore shipping address: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deleteShippingAddressPermanently(Integer shippingAddressId) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ShippingAddressService.deleteShippingAddressPermanently",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("shipping_address.id", shippingAddressId)
                        .build());

        logger.info("Permanently deleting shipping address: {}", shippingAddressId);

        return repo.deleteShippingAddressPermanently(shippingAddressId)
                .compose(v -> {
                    String cacheId = "shipping_address:" + shippingAddressId;
                    return redisService.delete(cacheId)
                            .onSuccess(deleted -> logger.debug("Shipping address {} cache invalidated on permanent delete", shippingAddressId))
                            .onFailure(err -> logger.warn("Failed to invalidate cache for deleted shipping address {}: {}", shippingAddressId, err.getMessage()))
                            .map(v);
                })
                .map(v -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "deletePermanent", "Shipping address deleted permanently");
                    return ApiResponse.<Void>success("success", null);
                })
                .recover(throwable -> {
                    logger.error("Failed to deletePermanent shipping address: {}", shippingAddressId, throwable);
                    tracingMetrics.completeSpanError(tracingContext, "deletePermanent", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to delete shipping address: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deleteShippingAddressByOrderPermanent(Integer orderId) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ShippingAddressService.deleteShippingAddressByOrderPermanent",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("shipping_address.order_id", orderId)
                        .build());

        logger.info("Permanently deleting shipping address by order: {}", orderId);

        return repo.deleteByOrderIDPermanent(orderId)
                .compose(v -> {
                    String cacheOrder = "shipping_address:order:" + orderId;
                    return redisService.delete(cacheOrder)
                            .onSuccess(deleted -> logger.debug("Shipping address for order {} cache invalidated on permanent delete", orderId))
                            .onFailure(err -> logger.warn("Failed to invalidate cache for order shipping address {}: {}", orderId, err.getMessage()))
                            .map(v);
                })
                .map(v -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "deleteByOrderPermanent", "Shipping address for order deleted permanently");
                    return ApiResponse.<Void>success("success", null);
                })
                .recover(throwable -> {
                    logger.error("Failed to deleteByOrderPermanent shipping address for order: {}", orderId, throwable);
                    tracingMetrics.completeSpanError(tracingContext, "deleteByOrderPermanent", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to delete shipping address by order: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> restoreAllShippingAddresses() {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ShippingAddressService.restoreAllShippingAddresses");

        logger.info("Restoring all trashed shipping addresses");

        return repo.restoreAllShippingAddress()
                .map(v -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "restore_all", "All shipping addresses restored successfully");
                    return ApiResponse.<Void>success("All shipping addresses restored successfully", null);
                })
                .recover(throwable -> {
                    logger.error("Failed to restore all shipping addresses", throwable);
                    tracingMetrics.completeSpanError(tracingContext, "restore_all", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to restore all shipping addresses: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deleteAllPermanentShippingAddresses() {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ShippingAddressService.deleteAllPermanentShippingAddresses");

        logger.info("Permanently deleting all trashed shipping addresses");

        return repo.deleteAllPermanentShippingAddress()
                .map(v -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "delete_all_permanent", "All trashed shipping addresses deleted permanently");
                    return ApiResponse.<Void>success("All trashed shipping addresses deleted permanently", null);
                })
                .recover(throwable -> {
                    logger.error("Failed to delete all permanent shipping addresses", throwable);
                    tracingMetrics.completeSpanError(tracingContext, "delete_all_permanent", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to delete all shipping addresses: " + throwable.getMessage()));
                });
    }
}
