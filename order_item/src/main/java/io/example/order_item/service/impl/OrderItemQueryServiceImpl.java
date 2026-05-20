package io.example.order_item.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.domain.PagedResult;
import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.common.model.PaginationMeta;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.order_item.model.OrderItem;
import io.example.order_item.model.OrderItemResponse;
import io.example.order_item.model.OrderItemResponseDeleteAt;
import io.example.order_item.repository.OrderItemQueryRepository;
import io.example.order_item.service.OrderItemQueryService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import pb.order_item.OrderItemQuery.FindAllOrderItemRequest;

public class OrderItemQueryServiceImpl implements OrderItemQueryService {
    private static final Logger logger = LoggerFactory.getLogger(OrderItemQueryServiceImpl.class);

    private final OrderItemQueryRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "order_item:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    public OrderItemQueryServiceImpl(
            OrderItemQueryRepository repo,
            RedisService redis,
            TracingMetrics metrics) {
        this.repo = repo;
        this.redis = redis;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponsePagination<List<OrderItemResponse>>> getAll(FindAllOrderItemRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderItemQueryService.getAll");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        String cacheKey = String.format("%sall:p:%d:s:%d:k:%s", CACHE_PREFIX, page, pageSize, keyword);

        return redis.getJson(cacheKey, ApiResponsePagination.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("order_item.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_all", "Order items fetched from cache");
                        @SuppressWarnings("unchecked")
                        ApiResponsePagination<List<OrderItemResponse>> typedCached = (ApiResponsePagination<List<OrderItemResponse>>) cached;
                        return Future.succeededFuture(typedCached);
                    }
                    span.setAttribute("order_item.cache_hit", false);
                    return repo.getOrderItems(keyword, page, pageSize)
                            .map(result -> mapOrderItemPagination(result, page, pageSize))
                            .compose(response -> redis.setJson(cacheKey, response, CACHE_TTL).map(response));
                })
                .onSuccess(response -> {
                    @SuppressWarnings("unchecked")
                    ApiResponsePagination<List<OrderItemResponse>> typedResponse = (ApiResponsePagination<List<OrderItemResponse>>) response;
                    span.setAttribute("order_items.count", (long) typedResponse.data().size());
                    span.setAttribute("order_items.total_records", (long) typedResponse.pagination().totalRecords());
                    metrics.completeSpanSuccess(tracingContext, "get_all", "Order items fetched successfully");
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch order items", throwable);
                    metrics.completeSpanError(tracingContext, "get_all", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.<List<OrderItemResponse>>error("Failed to fetch order items: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<OrderItemResponseDeleteAt>>> getActive(FindAllOrderItemRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderItemQueryService.getActive");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        String cacheKey = String.format("%sactive:p:%d:s:%d:k:%s", CACHE_PREFIX, page, pageSize, keyword);

        return redis.getJson(cacheKey, ApiResponsePagination.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("order_item.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_active", "Active order items fetched from cache");
                        @SuppressWarnings("unchecked")
                        ApiResponsePagination<List<OrderItemResponseDeleteAt>> typedCached = (ApiResponsePagination<List<OrderItemResponseDeleteAt>>) cached;
                        return Future.succeededFuture(typedCached);
                    }
                    span.setAttribute("order_item.cache_hit", false);
                    return repo.getOrderItemsActive(keyword, page, pageSize)
                            .map(result -> mapOrderItemPaginationDeleteAt(result, page, pageSize))
                            .compose(response -> redis.setJson(cacheKey, response, CACHE_TTL).map(response));
                })
                .onSuccess(response -> {
                    @SuppressWarnings("unchecked")
                    ApiResponsePagination<List<OrderItemResponseDeleteAt>> typedResponse = (ApiResponsePagination<List<OrderItemResponseDeleteAt>>) response;
                    span.setAttribute("order_items.count", (long) typedResponse.data().size());
                    span.setAttribute("order_items.total_records", (long) typedResponse.pagination().totalRecords());
                    metrics.completeSpanSuccess(tracingContext, "get_active", "Active order items fetched successfully");
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch active order items", throwable);
                    metrics.completeSpanError(tracingContext, "get_active", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.<List<OrderItemResponseDeleteAt>>error("Failed to fetch active order items: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<OrderItemResponseDeleteAt>>> getTrashed(FindAllOrderItemRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("OrderItemQueryService.getTrashed");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        String cacheKey = String.format("%strashed:p:%d:s:%d:k:%s", CACHE_PREFIX, page, pageSize, keyword);

        return redis.getJson(cacheKey, ApiResponsePagination.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("order_item.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_trashed", "Trashed order items fetched from cache");
                        @SuppressWarnings("unchecked")
                        ApiResponsePagination<List<OrderItemResponseDeleteAt>> typedCached = (ApiResponsePagination<List<OrderItemResponseDeleteAt>>) cached;
                        return Future.succeededFuture(typedCached);
                    }
                    span.setAttribute("order_item.cache_hit", false);
                    return repo.getOrderItemsTrashed(keyword, page, pageSize)
                            .map(result -> mapOrderItemPaginationDeleteAt(result, page, pageSize))
                            .compose(response -> redis.setJson(cacheKey, response, CACHE_TTL).map(response));
                })
                .onSuccess(response -> {
                    @SuppressWarnings("unchecked")
                    ApiResponsePagination<List<OrderItemResponseDeleteAt>> typedResponse = (ApiResponsePagination<List<OrderItemResponseDeleteAt>>) response;
                    span.setAttribute("order_items.count", (long) typedResponse.data().size());
                    span.setAttribute("order_items.total_records", (long) typedResponse.pagination().totalRecords());
                    metrics.completeSpanSuccess(tracingContext, "get_trashed", "Trashed order items fetched successfully");
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch trashed order items", throwable);
                    metrics.completeSpanError(tracingContext, "get_trashed", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.<List<OrderItemResponseDeleteAt>>error("Failed to fetch trashed order items: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<List<OrderItemResponse>>> getByOrderId(Integer orderId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "OrderItemQueryService.getByOrderId",
                Attributes.builder()
                        .put("order.id", (long) orderId)
                        .build());
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        logger.info("Fetching order items by order ID: {}", orderId);
        String cacheKey = CACHE_PREFIX + "order:" + orderId;

        return redis.getJsonList(cacheKey, OrderItem.class)
                .compose(cachedItems -> {
                    if (cachedItems != null && !cachedItems.isEmpty()) {
                        logger.info("Order items for order {} found in cache", orderId);
                        span.setAttribute("order_item.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_by_order_id", "Order items fetched from cache");
                        List<OrderItemResponse> responses = cachedItems.stream()
                                .map(OrderItemResponse::from)
                                .toList();
                        return Future.succeededFuture(ApiResponse.success(
                                "Order items fetched successfully (from cache)", responses));
                    } else {
                        span.setAttribute("order_item.cache_hit", false);
                        return repo.getOrderItemsByOrder(orderId)
                                .compose(items -> {
                                    if (items == null || items.isEmpty()) {
                                        return Future.succeededFuture(List.<OrderItem>of());
                                    }
                                    return redis.setJsonList(cacheKey, items, CACHE_TTL).map(items);
                                })
                                .map(items -> {
                                    metrics.completeSpanSuccess(tracingContext, "get_by_order_id", "Order items fetched from database");
                                    List<OrderItemResponse> responses = items.stream()
                                            .map(OrderItemResponse::from)
                                            .toList();
                                    return ApiResponse.success("Order items fetched successfully", responses);
                                });
                    }
                })
                .recover(err -> {
                    logger.error("Failed to fetch order items by order ID: {}", orderId, err);
                    metrics.completeSpanError(tracingContext, "get_by_order_id", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<List<OrderItemResponse>>error("Failed to fetch order items: " + err.getMessage()));
                });
    }

    private ApiResponsePagination<List<OrderItemResponse>> mapOrderItemPagination(
            PagedResult<OrderItem> result,
            int page,
            int pageSize) {

        int totalRecords = result.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<OrderItemResponse> data = result.getData()
                .stream()
                .map(OrderItemResponse::from)
                .toList();

        return new ApiResponsePagination<>(
                "success",
                "Order items found",
                data,
                new PaginationMeta(
                        page,
                        pageSize,
                        totalPages,
                        totalRecords));
    }

    private ApiResponsePagination<List<OrderItemResponseDeleteAt>> mapOrderItemPaginationDeleteAt(
            PagedResult<OrderItem> result,
            int page,
            int pageSize) {

        int totalRecords = result.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<OrderItemResponseDeleteAt> data = result.getData()
                .stream()
                .map(OrderItemResponseDeleteAt::from)
                .toList();

        return new ApiResponsePagination<>(
                "success",
                "Order items found",
                data,
                new PaginationMeta(
                        page,
                        pageSize,
                        totalPages,
                        totalRecords));
    }
}
