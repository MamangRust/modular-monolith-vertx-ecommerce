package io.example.order.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.domain.PagedResult;
import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.common.model.PaginationMeta;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.common.exception.NotFoundException;
import io.example.order.model.Order;
import io.example.order.model.OrderResponse;
import io.example.order.model.OrderResponseDeleteAt;
import io.example.order.repository.OrderQueryRepository;
import io.example.order.service.OrderQueryService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;

public class OrderQueryServiceImpl implements OrderQueryService {
    private static final Logger logger = LoggerFactory.getLogger(OrderQueryServiceImpl.class);

    private final OrderQueryRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "order:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    public OrderQueryServiceImpl(OrderQueryRepository repo, RedisService redis, TracingMetrics metrics) {
        this.repo = repo;
        this.redis = redis;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponsePagination<List<OrderResponse>>> getAll(String search, int page, int pageSize) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderQueryService.getAll");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int p = page > 0 ? page : 1;
        int ps = pageSize > 0 ? pageSize : 10;
        String keyword = search != null ? search.trim() : "";

        String cacheKey = String.format("%sall:p:%d:ps:%d:s:%s", CACHE_PREFIX, p, ps, keyword);

        return redis.getJson(cacheKey, ApiResponsePagination.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("order.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_all", "Orders fetched from cache");
                        @SuppressWarnings("unchecked")
                        ApiResponsePagination<List<OrderResponse>> typed = (ApiResponsePagination<List<OrderResponse>>) cached;
                        return Future.succeededFuture(typed);
                    }
                    span.setAttribute("order.cache_hit", false);
                    return repo.getOrders(keyword, p, ps)
                            .map(result -> mapOrderPagination(result, p, ps))
                            .compose(response -> redis.setJson(cacheKey, response, CACHE_TTL).map(response));
                })
                .onSuccess(response -> {
                    @SuppressWarnings("unchecked")
                    ApiResponsePagination<List<OrderResponse>> typed = (ApiResponsePagination<List<OrderResponse>>) response;
                    span.setAttribute("orders.count", (long) typed.data().size());
                    span.setAttribute("orders.total_records", (long) typed.pagination().totalRecords());
                    metrics.completeSpanSuccess(tracingContext, "get_all", "Orders fetched successfully");
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch orders", throwable);
                    metrics.completeSpanError(tracingContext, "get_all", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch orders: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<OrderResponse>>> getActive(String search, int page, int pageSize) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderQueryService.getActive");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int p = page > 0 ? page : 1;
        int ps = pageSize > 0 ? pageSize : 10;
        String keyword = search != null ? search.trim() : "";

        String cacheKey = String.format("%sactive:p:%d:ps:%d:s:%s", CACHE_PREFIX, p, ps, keyword);

        return redis.getJson(cacheKey, ApiResponsePagination.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("order.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_active", "Active orders fetched from cache");
                        @SuppressWarnings("unchecked")
                        ApiResponsePagination<List<OrderResponse>> typed = (ApiResponsePagination<List<OrderResponse>>) cached;
                        return Future.succeededFuture(typed);
                    }
                    span.setAttribute("order.cache_hit", false);
                    return repo.getOrdersActive(keyword, p, ps)
                            .map(result -> mapOrderPagination(result, p, ps))
                            .compose(response -> redis.setJson(cacheKey, response, CACHE_TTL).map(response));
                })
                .onSuccess(response -> {
                    @SuppressWarnings("unchecked")
                    ApiResponsePagination<List<OrderResponse>> typed = (ApiResponsePagination<List<OrderResponse>>) response;
                    span.setAttribute("orders.count", (long) typed.data().size());
                    span.setAttribute("orders.total_records", (long) typed.pagination().totalRecords());
                    metrics.completeSpanSuccess(tracingContext, "get_active", "Active orders fetched successfully");
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch active orders", throwable);
                    metrics.completeSpanError(tracingContext, "get_active", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch active orders: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<OrderResponseDeleteAt>>> getTrashed(String search, int page, int pageSize) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderQueryService.getTrashed");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int p = page > 0 ? page : 1;
        int ps = pageSize > 0 ? pageSize : 10;
        String keyword = search != null ? search.trim() : "";

        String cacheKey = String.format("%strashed:p:%d:ps:%d:s:%s", CACHE_PREFIX, p, ps, keyword);

        return redis.getJson(cacheKey, ApiResponsePagination.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("order.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_trashed", "Trashed orders fetched from cache");
                        @SuppressWarnings("unchecked")
                        ApiResponsePagination<List<OrderResponseDeleteAt>> typed = (ApiResponsePagination<List<OrderResponseDeleteAt>>) cached;
                        return Future.succeededFuture(typed);
                    }
                    span.setAttribute("order.cache_hit", false);
                    return repo.getOrdersTrashed(keyword, p, ps)
                            .map(result -> mapTrashedOrderPagination(result, p, ps))
                            .compose(response -> redis.setJson(cacheKey, response, CACHE_TTL).map(response));
                })
                .onSuccess(response -> {
                    @SuppressWarnings("unchecked")
                    ApiResponsePagination<List<OrderResponseDeleteAt>> typed = (ApiResponsePagination<List<OrderResponseDeleteAt>>) response;
                    span.setAttribute("orders.count", (long) typed.data().size());
                    span.setAttribute("orders.total_records", (long) typed.pagination().totalRecords());
                    metrics.completeSpanSuccess(tracingContext, "get_trashed", "Trashed orders fetched successfully");
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch trashed orders", throwable);
                    metrics.completeSpanError(tracingContext, "get_trashed", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch trashed orders: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<OrderResponse>>> getByMerchant(Integer merchantId, String search, int page, int pageSize) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderQueryService.getByMerchant");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));
        span.setAttribute("merchant.id", (long) merchantId);

        int p = page > 0 ? page : 1;
        int ps = pageSize > 0 ? pageSize : 10;
        String keyword = search != null ? search.trim() : "";

        String cacheKey = String.format("%smerchant:%d:p:%d:ps:%d:s:%s", CACHE_PREFIX, merchantId, p, ps, keyword);

        return redis.getJson(cacheKey, ApiResponsePagination.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("order.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_by_merchant", "Merchant orders fetched from cache");
                        @SuppressWarnings("unchecked")
                        ApiResponsePagination<List<OrderResponse>> typed = (ApiResponsePagination<List<OrderResponse>>) cached;
                        return Future.succeededFuture(typed);
                    }
                    span.setAttribute("order.cache_hit", false);
                    return repo.getOrdersByMerchant(merchantId, keyword, p, ps)
                            .map(result -> mapOrderPagination(result, p, ps))
                            .compose(response -> redis.setJson(cacheKey, response, CACHE_TTL).map(response));
                })
                .onSuccess(response -> {
                    @SuppressWarnings("unchecked")
                    ApiResponsePagination<List<OrderResponse>> typed = (ApiResponsePagination<List<OrderResponse>>) response;
                    span.setAttribute("orders.count", (long) typed.data().size());
                    span.setAttribute("orders.total_records", (long) typed.pagination().totalRecords());
                    metrics.completeSpanSuccess(tracingContext, "get_by_merchant", "Merchant orders fetched successfully");
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch orders by merchant ID: {}", merchantId, throwable);
                    metrics.completeSpanError(tracingContext, "get_by_merchant", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch merchant orders: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<OrderResponse>> getById(Long id) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderQueryService.getById");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));
        span.setAttribute("order.id", id);

        String cacheKey = CACHE_PREFIX + "id:" + id;

        return redis.getJson(cacheKey, Order.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("order.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_by_id", "Order fetched from cache");
                        return Future.succeededFuture(ApiResponse.success("Order fetched from cache", OrderResponse.from(cached)));
                    }
                    span.setAttribute("order.cache_hit", false);
                    return repo.getOrderById(id)
                            .compose(order -> {
                                if (order == null) {
                                    return Future.failedFuture(new NotFoundException("Order not found with ID: " + id));
                                }
                                return redis.setJson(cacheKey, order, CACHE_TTL)
                                        .map(v -> ApiResponse.success("Order fetched successfully", OrderResponse.from(order)));
                            });
                })
                .onSuccess(response -> metrics.completeSpanSuccess(tracingContext, "get_by_id", "Order fetched successfully"))
                .recover(throwable -> {
                    logger.error("Failed to fetch order by ID: {}", id, throwable);
                    metrics.completeSpanError(tracingContext, "get_by_id", throwable.getMessage());
                    return Future.succeededFuture(ApiResponse.error("Failed to fetch order: " + throwable.getMessage()));
                });
    }

    private ApiResponsePagination<List<OrderResponse>> mapOrderPagination(PagedResult<Order> result, int page, int pageSize) {
        int totalRecords = result.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<OrderResponse> data = result.getData().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());

        return new ApiResponsePagination<>("success", "Orders found", data,
                new PaginationMeta(page, pageSize, totalPages, totalRecords));
    }

    private ApiResponsePagination<List<OrderResponseDeleteAt>> mapTrashedOrderPagination(PagedResult<Order> result, int page, int pageSize) {
        int totalRecords = result.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<OrderResponseDeleteAt> data = result.getData().stream()
                .map(OrderResponseDeleteAt::from)
                .collect(Collectors.toList());

        return new ApiResponsePagination<>("success", "Trashed orders found", data,
                new PaginationMeta(page, pageSize, totalPages, totalRecords));
    }
}
