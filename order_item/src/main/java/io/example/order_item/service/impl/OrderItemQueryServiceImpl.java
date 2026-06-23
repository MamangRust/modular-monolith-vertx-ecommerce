package io.example.order_item.service.impl;

import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.domain.PagedResult;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.order_item.domain.requests.FindAllOrderItemRequest;
import io.example.order_item.model.OrderItem;
import io.example.order_item.model.OrderItemResponse;
import io.example.order_item.model.OrderItemResponseDeleteAt;
import io.example.order_item.repository.OrderItemQueryRepository;
import io.example.order_item.service.OrderItemQueryService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderItemQueryServiceImpl implements OrderItemQueryService {
        private static final ObjectMapper mapper = new ObjectMapper();
        private final OrderItemQueryRepository repo;
        private final RedisService redis;
        private final TracingMetrics metrics;

        private static final String CACHE_PREFIX = "order_item:";
        private static final Duration CACHE_TTL = Duration.ofMinutes(10);

        private PagedResult<OrderItemResponse> mapPagination(PagedResult<OrderItem> res) {
                List<OrderItemResponse> data = res.getData().stream().map(OrderItemResponse::from).toList();
                return new PagedResult<>(data, res.getTotalRecords());
        }

        private PagedResult<OrderItemResponseDeleteAt> mapPaginationDeleteAt(PagedResult<OrderItem> res) {
                List<OrderItemResponseDeleteAt> data = res.getData().stream().map(OrderItemResponseDeleteAt::from)
                                .toList();
                return new PagedResult<>(data, res.getTotalRecords());
        }

        @Override
        public Future<PagedResult<OrderItemResponse>> getAll(FindAllOrderItemRequest req) {
                var ctx = metrics.startSpan("OrderItemQueryService.getAll");
                int page = req.getPage() > 0 ? req.getPage() : 1;
                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                String cacheKey = String.format("%sall:p:%d:s:%d:k:%s", CACHE_PREFIX, page, pageSize, keyword);

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<OrderItem> typedCached = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<OrderItem>>() {
                                                                        });
                                                        return Future.succeededFuture(mapPagination(typedCached));
                                                } catch (Exception e) {
                                                        // fallback to db if deserialize fails
                                                }
                                        }
                                        return repo.getOrderItems(req)
                                                        .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL)
                                                                        .map(v -> res))
                                                        .map(this::mapPagination);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getAll", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getAll", e.getMessage()));
        }

        @Override
        public Future<PagedResult<OrderItemResponseDeleteAt>> getActive(FindAllOrderItemRequest req) {
                var ctx = metrics.startSpan("OrderItemQueryService.getActive");
                int page = req.getPage() > 0 ? req.getPage() : 1;
                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                String cacheKey = String.format("%sactive:p:%d:s:%d:k:%s", CACHE_PREFIX, page, pageSize, keyword);

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<OrderItem> typedCached = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<OrderItem>>() {
                                                                        });
                                                        return Future.succeededFuture(
                                                                        mapPaginationDeleteAt(typedCached));
                                                } catch (Exception e) {
                                                        // fallback to db if deserialize fails
                                                }
                                        }
                                        return repo.getOrderItemsActive(req)
                                                        .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL)
                                                                        .map(v -> res))
                                                        .map(this::mapPaginationDeleteAt);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getActive", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getActive", e.getMessage()));
        }

        @Override
        public Future<PagedResult<OrderItemResponseDeleteAt>> getTrashed(FindAllOrderItemRequest req) {
                var ctx = metrics.startSpan("OrderItemQueryService.getTrashed");
                int page = req.getPage() > 0 ? req.getPage() : 1;
                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                String cacheKey = String.format("%strashed:p:%d:s:%d:k:%s", CACHE_PREFIX, page, pageSize, keyword);

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<OrderItem> typedCached = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<OrderItem>>() {
                                                                        });
                                                        return Future.succeededFuture(
                                                                        mapPaginationDeleteAt(typedCached));
                                                } catch (Exception e) {
                                                        // fallback to db if deserialize fails
                                                }
                                        }
                                        return repo.getOrderItemsTrashed(req)
                                                        .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL)
                                                                        .map(v -> res))
                                                        .map(this::mapPaginationDeleteAt);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTrashed", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getTrashed", e.getMessage()));
        }

        @Override
        public Future<List<OrderItemResponse>> getByOrderId(Long orderId) {
                var ctx = metrics.startSpan("OrderItemQueryService.getByOrderId",
                                Attributes.builder().put("order.id", orderId).build());
                String cacheKey = CACHE_PREFIX + "order:" + orderId;

                return redis.getJsonList(cacheKey, OrderItem.class)
                                .compose(cachedItems -> {
                                        if (cachedItems != null && !cachedItems.isEmpty()) {
                                                List<OrderItemResponse> responses = cachedItems.stream()
                                                                .map(OrderItemResponse::from).toList();
                                                return Future.succeededFuture(responses);
                                        }
                                        return repo.getOrderItemsByOrder(orderId)
                                                        .compose(items -> {
                                                                if (items == null || items.isEmpty()) {
                                                                        return Future.succeededFuture(
                                                                                        List.<OrderItem>of());
                                                                }
                                                                return redis.setJsonList(cacheKey, items, CACHE_TTL)
                                                                                .<List<OrderItem>>map(v -> items);
                                                        })
                                                        .map(items -> items.stream().map(OrderItemResponse::from)
                                                                        .toList());
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getByOrderId", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getByOrderId", e.getMessage()));
        }
}