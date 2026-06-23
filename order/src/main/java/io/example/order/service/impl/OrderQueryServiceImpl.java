package io.example.order.service.impl;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.domain.PagedResult;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.order.domain.requests.*;
import io.example.order.model.Order;
import io.example.order.model.OrderResponse;
import io.example.order.model.OrderResponseDeleteAt;
import io.example.order.repository.OrderQueryRepository;
import io.example.order.service.OrderQueryService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {
    private static final Logger log = LoggerFactory.getLogger(OrderQueryServiceImpl.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final OrderQueryRepository repository;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "order:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private PagedResult<OrderResponse> mapPagination(PagedResult<Order> res) {
        List<OrderResponse> data = res.getData().stream().map(OrderResponse::from).toList();
        return new PagedResult<>(data, res.getTotalRecords());
    }

    private PagedResult<OrderResponseDeleteAt> mapPaginationDeleteAt(PagedResult<Order> res) {
        List<OrderResponseDeleteAt> data = res.getData().stream().map(OrderResponseDeleteAt::from).toList();
        return new PagedResult<>(data, res.getTotalRecords());
    }

    @Override
    public Future<PagedResult<OrderResponse>> getAll(FindAllOrderRequest req) {
        var ctx = metrics.startSpan("OrderQueryService.getAll");
        String cacheKey = CACHE_PREFIX + "list:all:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
                + req.getPage() + ":" + req.getPageSize();

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Order> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Order>>() {
                                    });
                            return Future.succeededFuture(mapPagination(typedCached));
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached orders: {}", e.getMessage());
                        }
                    }
                    return repository.getOrders(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
                            .map(this::mapPagination);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getAll", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getAll", e.getMessage()));
    }

    @Override
    public Future<PagedResult<OrderResponse>> getActive(FindAllOrderRequest req) {
        var ctx = metrics.startSpan("OrderQueryService.getActive");
        String cacheKey = CACHE_PREFIX + "list:active:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
                + req.getPage() + ":" + req.getPageSize();

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Order> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Order>>() {
                                    });
                            return Future.succeededFuture(mapPagination(typedCached));
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached active orders: {}", e.getMessage());
                        }
                    }
                    return repository.getOrdersActive(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
                            .map(this::mapPagination);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getActive", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getActive", e.getMessage()));
    }

    @Override
    public Future<PagedResult<OrderResponseDeleteAt>> getTrashed(FindAllOrderRequest req) {
        var ctx = metrics.startSpan("OrderQueryService.getTrashed");
        String cacheKey = CACHE_PREFIX + "list:trashed:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
                + req.getPage() + ":" + req.getPageSize();

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Order> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Order>>() {
                                    });
                            return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached trashed orders: {}", e.getMessage());
                        }
                    }
                    return repository.getOrdersTrashed(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
                            .map(this::mapPaginationDeleteAt);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTrashed", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getTrashed", e.getMessage()));
    }

    @Override
    public Future<PagedResult<OrderResponse>> getByMerchant(FindAllOrderByMerchantRequest req) {
        var ctx = metrics.startSpan("OrderQueryService.getByMerchant");
        String cacheKey = CACHE_PREFIX + "list:merchant:" + req.getMerchantId() + ":"
                + (req.getSearch() != null ? req.getSearch() : "") + ":"
                + req.getPage() + ":" + req.getPageSize();

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Order> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Order>>() {
                                    });
                            return Future.succeededFuture(mapPagination(typedCached));
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached merchant orders: {}", e.getMessage());
                        }
                    }
                    return repository.getOrdersByMerchant(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
                            .map(this::mapPagination);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getByMerchant", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getByMerchant", e.getMessage()));
    }

    @Override
    public Future<OrderResponse> getById(Long id) {
        var ctx = metrics.startSpan("OrderQueryService.getById",
                Attributes.builder().put("order.id", id).build());
        String key = CACHE_PREFIX + id;

        return redis.getJson(key, Order.class)
                .compose(cached -> {
                    if (cached != null) {
                        return Future.succeededFuture(OrderResponse.from(cached));
                    }
                    return repository.getOrderById(id)
                            .compose(db -> {
                                if (db == null) {
                                    return Future.<Order>failedFuture(new NotFoundException("Order not found"));
                                }
                                return redis.setJson(key, db, CACHE_TTL).<Order>map(v -> db);
                            })
                            .map(OrderResponse::from);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getById", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getById", e.getMessage()));
    }
}