package io.example.shipping_address.service.impl;

import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.domain.PagedResult;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.shipping_address.domain.requests.FindAllShippingAddress;
import io.example.shipping_address.model.ShippingAddress;
import io.example.shipping_address.model.ShippingAddressResponse;
import io.example.shipping_address.model.ShippingAddressResponseDeleteAt;
import io.example.shipping_address.repository.ShippingAddressQueryRepository;
import io.example.shipping_address.service.ShippingAddressQueryService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShippingAddressQueryServiceImpl implements ShippingAddressQueryService {
        private final ShippingAddressQueryRepository repo;
        private final RedisService redis;
        private final TracingMetrics metrics;
        private static final ObjectMapper mapper = new ObjectMapper();
        private static final Duration CACHE_TTL = Duration.ofMinutes(10);

        private PagedResult<ShippingAddressResponse> mapPagination(PagedResult<ShippingAddress> res) {
                List<ShippingAddressResponse> data = res.getData().stream().map(ShippingAddressResponse::from).toList();
                return new PagedResult<>(data, res.getTotalRecords());
        }

        private PagedResult<ShippingAddressResponseDeleteAt> mapPaginationDeleteAt(PagedResult<ShippingAddress> res) {
                List<ShippingAddressResponseDeleteAt> data = res.getData().stream()
                                .map(ShippingAddressResponseDeleteAt::from).toList();
                return new PagedResult<>(data, res.getTotalRecords());
        }

        @Override
        public Future<PagedResult<ShippingAddressResponse>> getAllShippingAddresses(FindAllShippingAddress req) {
                var ctx = metrics.startSpan("ShippingAddressQueryService.getAll");
                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                req.setPage(page);
                req.setPageSize(pageSize);
                req.setSearch(keyword);

                String cacheKey = String.format("shipping_addresses:page:%d:search:%s", page, keyword);

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<ShippingAddress> result = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<ShippingAddress>>() {
                                                                        });
                                                        return Future.succeededFuture(mapPagination(result));
                                                } catch (Exception e) {
                                                }
                                        }
                                        return repo.getShippingAddresses(req)
                                                        .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL)
                                                                        .map(v -> res))
                                                        .map(this::mapPagination);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getAll", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getAll", e.getMessage()));
        }

        @Override
        public Future<PagedResult<ShippingAddressResponseDeleteAt>> getActiveShippingAddresses(
                        FindAllShippingAddress req) {
                var ctx = metrics.startSpan("ShippingAddressQueryService.getActive");
                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                req.setPage(page);
                req.setPageSize(pageSize);
                req.setSearch(keyword);

                String cacheKey = String.format("shipping_addresses:active:page:%d:search:%s", page, keyword);

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<ShippingAddress> result = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<ShippingAddress>>() {
                                                                        });
                                                        return Future.succeededFuture(mapPaginationDeleteAt(result));
                                                } catch (Exception e) {
                                                }
                                        }
                                        return repo.getShippingAddressActive(req)
                                                        .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL)
                                                                        .map(v -> res))
                                                        .map(this::mapPaginationDeleteAt);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getActive", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getActive", e.getMessage()));
        }

        @Override
        public Future<PagedResult<ShippingAddressResponseDeleteAt>> getTrashedShippingAddresses(
                        FindAllShippingAddress req) {
                var ctx = metrics.startSpan("ShippingAddressQueryService.getTrashed");
                int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
                int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

                req.setPage(page);
                req.setPageSize(pageSize);
                req.setSearch(keyword);

                String cacheKey = String.format("shipping_addresses:trashed:page:%d:search:%s", page, keyword);

                return redis.get(cacheKey)
                                .compose(jsonStr -> {
                                        if (jsonStr != null && !jsonStr.isEmpty()) {
                                                try {
                                                        PagedResult<ShippingAddress> result = mapper.readValue(jsonStr,
                                                                        new TypeReference<PagedResult<ShippingAddress>>() {
                                                                        });
                                                        return Future.succeededFuture(mapPaginationDeleteAt(result));
                                                } catch (Exception e) {
                                                }
                                        }
                                        return repo.getShippingAddressTrashed(req)
                                                        .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL)
                                                                        .map(v -> res))
                                                        .map(this::mapPaginationDeleteAt);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTrashed", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getTrashed", e.getMessage()));
        }

        @Override
        public Future<ShippingAddressResponse> getShippingAddressById(Long shippingAddressId) {
                var ctx = metrics.startSpan("ShippingAddressQueryService.getById",
                                Attributes.builder().put("shipping_address.id", shippingAddressId).build());
                String cacheKey = "shipping_address:" + shippingAddressId;

                return redis.getJson(cacheKey, ShippingAddress.class)
                                .compose(cached -> {
                                        if (cached != null) {
                                                return Future.succeededFuture(ShippingAddressResponse.from(cached));
                                        }
                                        return repo.getShippingByID(shippingAddressId)
                                                        .compose(db -> {
                                                                if (db == null) {
                                                                        return Future.<ShippingAddress>failedFuture(
                                                                                        new NotFoundException(
                                                                                                        "Shipping address not found"));
                                                                }
                                                                return redis.setJson(cacheKey, db,
                                                                                 Duration.ofMinutes(60))
                                                                                 .<ShippingAddress>map(v -> db);
                                                        })
                                                        .map(ShippingAddressResponse::from);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getById", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getById", e.getMessage()));
        }

        @Override
        public Future<ShippingAddressResponse> getShippingAddressByOrderId(Long orderId) {
                var ctx = metrics.startSpan("ShippingAddressQueryService.getByOrderId",
                                Attributes.builder().put("shipping_address.order_id", orderId).build());
                String cacheKey = "shipping_address:order:" + orderId;

                return redis.getJson(cacheKey, ShippingAddress.class)
                                .compose(cached -> {
                                        if (cached != null) {
                                                return Future.succeededFuture(ShippingAddressResponse.from(cached));
                                        }
                                        return repo.getShippingAddressByOrderID(orderId)
                                                        .compose(db -> {
                                                                if (db == null) {
                                                                        return Future.<ShippingAddress>failedFuture(
                                                                                        new NotFoundException(
                                                                                                        "Shipping address not found for order"));
                                                                }
                                                                return redis.setJson(cacheKey, db,
                                                                                 Duration.ofMinutes(60))
                                                                                 .<ShippingAddress>map(v -> db);
                                                        })
                                                        .map(ShippingAddressResponse::from);
                                })
                                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getByOrderId", "Success"))
                                .onFailure(e -> metrics.completeSpanError(ctx, "getByOrderId", e.getMessage()));
        }
}