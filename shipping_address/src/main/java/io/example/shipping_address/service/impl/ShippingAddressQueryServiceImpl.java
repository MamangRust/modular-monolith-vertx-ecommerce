package io.example.shipping_address.service.impl;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.example.common.domain.PagedResult;
import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.common.model.PaginationMeta;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.shipping_address.model.FindAllShippingAddress;
import io.example.shipping_address.model.ShippingAddress;
import io.example.shipping_address.model.ShippingAddressResponse;
import io.example.shipping_address.model.ShippingAddressResponseDeleteAt;
import io.example.shipping_address.repository.ShippingAddressQueryRepository;
import io.example.shipping_address.service.ShippingAddressQueryService;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class ShippingAddressQueryServiceImpl implements ShippingAddressQueryService {
    private static final Logger logger = LoggerFactory.getLogger(ShippingAddressQueryServiceImpl.class);
    private final ShippingAddressQueryRepository repo;
    private final RedisService redisService;
    private final TracingMetrics tracingMetrics;
    private final ObjectMapper mapper = new ObjectMapper();

    public ShippingAddressQueryServiceImpl(
            ShippingAddressQueryRepository repo,
            RedisService redisService,
            TracingMetrics tracingMetrics) {
        this.repo = repo;
        this.redisService = redisService;
        this.tracingMetrics = tracingMetrics;
    }

    @Override
    public Future<ApiResponsePagination<List<ShippingAddressResponse>>> getAllShippingAddresses(FindAllShippingAddress req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ShippingAddressService.getAllShippingAddresses");
        Span span = Span.fromContext(tracingContext.getContext());

        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        logger.info("Fetching shipping addresses | search={}, page={}, pageSize={}", keyword, page, pageSize);

        String cacheKey = String.format("shipping_addresses:page:%d:search:%s", page, keyword);

        return redisService.get(cacheKey)
                .<ApiResponsePagination<List<ShippingAddressResponse>>>compose(cachedResult -> {
                    if (cachedResult != null && !cachedResult.isEmpty()) {
                        logger.info("Shipping addresses cache hit for key: {}", cacheKey);
                        span.setAttribute("cache.hit", true);
                        try {
                            PagedResult<ShippingAddress> result = mapper.readValue(
                                    cachedResult,
                                    new TypeReference<PagedResult<ShippingAddress>>() {
                                    });

                            ApiResponsePagination<List<ShippingAddressResponse>> response = mapShippingAddressPagination(result, req, "Shipping addresses fetched successfully (from cache)");
                            return Future.succeededFuture(response);
                        } catch (Exception e) {
                            logger.warn("Failed to parse shipping addresses cache: {}", e.getMessage());
                        }
                    }

                    span.setAttribute("cache.hit", false);
                    return repo.getShippingAddresses(req)
                            .map(result -> {
                                redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                                        .onFailure(err -> logger.warn("Failed to set shipping addresses cache: {}", err.getMessage()));

                                return mapShippingAddressPagination(result, req, "Shipping addresses fetched successfully");
                            });
                })
                .map(response -> {
                    span.setAttribute("shipping_addresses.count", response.data().size());
                    span.setAttribute("shipping_addresses.total_records", response.pagination().totalRecords());
                    tracingMetrics.completeSpanSuccess(tracingContext, "get_all", "Shipping addresses fetched successfully");
                    return response;
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch shipping addresses", throwable);
                    tracingMetrics.completeSpanError(tracingContext, "get_all", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch shipping addresses: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<ShippingAddressResponseDeleteAt>>> getActiveShippingAddresses(FindAllShippingAddress req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ShippingAddressService.getActiveShippingAddresses");
        Span span = Span.fromContext(tracingContext.getContext());

        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        logger.info("Fetching active shipping addresses | search={}, page={}, pageSize={}", keyword, page, pageSize);

        String cacheKey = String.format("shipping_addresses:active:page:%d:search:%s", page, keyword);

        return redisService.get(cacheKey)
                .<ApiResponsePagination<List<ShippingAddressResponseDeleteAt>>>compose(cachedResult -> {
                    if (cachedResult != null && !cachedResult.isEmpty()) {
                        logger.info("Active shipping addresses cache hit for key: {}", cacheKey);
                        span.setAttribute("cache.hit", true);
                        try {
                            PagedResult<ShippingAddress> result = mapper.readValue(
                                    cachedResult,
                                    new TypeReference<PagedResult<ShippingAddress>>() {
                                    });

                            ApiResponsePagination<List<ShippingAddressResponseDeleteAt>> response = mapShippingAddressPaginationDeleteAt(result, req, "Active shipping addresses fetched successfully (from cache)");
                            return Future.succeededFuture(response);
                        } catch (Exception e) {
                            logger.warn("Failed to parse active shipping addresses cache: {}", e.getMessage());
                        }
                    }

                    span.setAttribute("cache.hit", false);
                    return repo.getShippingAddressActive(req)
                            .map(result -> {
                                redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                                        .onFailure(err -> logger.warn("Failed to set active shipping addresses cache: {}", err.getMessage()));

                                return mapShippingAddressPaginationDeleteAt(result, req, "Active shipping addresses fetched successfully");
                            });
                })
                .map(response -> {
                    span.setAttribute("shipping_address.count", response.data().size());
                    span.setAttribute("shipping_address.total_records", response.pagination().totalRecords());
                    tracingMetrics.completeSpanSuccess(tracingContext, "get_active", "Active shipping addresses fetched successfully");
                    return response;
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch active shipping addresses", throwable);
                    tracingMetrics.completeSpanError(tracingContext, "get_active", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch active shipping addresses: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<ShippingAddressResponseDeleteAt>>> getTrashedShippingAddresses(FindAllShippingAddress req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ShippingAddressService.getTrashedShippingAddresses");
        Span span = Span.fromContext(tracingContext.getContext());

        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        logger.info("Fetching trashed shipping addresses | search={}, page={}, pageSize={}", keyword, page, pageSize);

        String cacheKey = String.format("shipping_addresses:trashed:page:%d:search:%s", page, keyword);

        return redisService.get(cacheKey)
                .<ApiResponsePagination<List<ShippingAddressResponseDeleteAt>>>compose(cachedResult -> {
                    if (cachedResult != null && !cachedResult.isEmpty()) {
                        logger.info("Trashed shipping addresses cache hit for key: {}", cacheKey);
                        span.setAttribute("cache.hit", true);
                        try {
                            PagedResult<ShippingAddress> result = mapper.readValue(
                                    cachedResult,
                                    new TypeReference<PagedResult<ShippingAddress>>() {
                                    });

                            ApiResponsePagination<List<ShippingAddressResponseDeleteAt>> response = mapShippingAddressPaginationDeleteAt(result, req, "Trashed shipping addresses fetched successfully (from cache)");
                            return Future.succeededFuture(response);
                        } catch (Exception e) {
                            logger.warn("Failed to parse trashed shipping addresses cache: {}", e.getMessage());
                        }
                    }

                    span.setAttribute("cache.hit", false);
                    return repo.getShippingAddressTrashed(req)
                            .map(result -> {
                                redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                                        .onFailure(err -> logger.warn("Failed to set trashed shipping addresses cache: {}", err.getMessage()));

                                return mapShippingAddressPaginationDeleteAt(result, req, "Trashed shipping addresses fetched successfully");
                            });
                })
                .map(response -> {
                    span.setAttribute("shipping_address.count", response.data().size());
                    span.setAttribute("shipping_address.total_records", response.pagination().totalRecords());
                    tracingMetrics.completeSpanSuccess(tracingContext, "get_trashed", "Trashed shipping addresses fetched successfully");
                    return response;
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch trashed shipping addresses", throwable);
                    tracingMetrics.completeSpanError(tracingContext, "get_trashed", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch trashed shipping addresses: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<ShippingAddressResponse>> getShippingAddressById(Integer shippingAddressId) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ShippingAddressService.getShippingAddressById",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("shipping_address.id", shippingAddressId)
                        .build());
        Span span = Span.fromContext(tracingContext.getContext());

        logger.info("Fetching shipping address by id: {}", shippingAddressId);
        String cacheKey = "shipping_address:" + shippingAddressId;

        return redisService.get(cacheKey)
                .compose(cachedAddress -> {
                    if (cachedAddress != null && !cachedAddress.isEmpty()) {
                        logger.info("Shipping address {} found in cache", shippingAddressId);
                        span.setAttribute("shipping_address.cache_hit", true);
                        try {
                            ShippingAddress address = ShippingAddress.fromJson(new JsonObject(cachedAddress));
                            tracingMetrics.completeSpanSuccess(tracingContext, "get_by_id", "Shipping address fetched from cache");
                            return Future.succeededFuture(ApiResponse.success(
                                    "Shipping address fetched successfully (from cache)",
                                    ShippingAddressResponse.from(address)));
                        } catch (Exception e) {
                            logger.warn("Failed to parse cached shipping address data for {}: {}", shippingAddressId, e.getMessage());
                            return fetchShippingAddressFromDatabase(shippingAddressId, tracingContext);
                        }
                    } else {
                        span.setAttribute("shipping_address.cache_hit", false);
                        return fetchShippingAddressFromDatabase(shippingAddressId, tracingContext);
                    }
                })
                .recover(err -> {
                    logger.error("Failed to fetch shipping address by id: {}", shippingAddressId, err);
                    tracingMetrics.completeSpanError(tracingContext, "get_by_id", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to fetch shipping address: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<ShippingAddressResponse>> getShippingAddressByOrderId(Integer orderId) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ShippingAddressService.getShippingAddressByOrderId",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("shipping_address.order_id", orderId)
                        .build());
        Span span = Span.fromContext(tracingContext.getContext());

        logger.info("Fetching shipping address by order id: {}", orderId);
        String cacheKey = "shipping_address:order:" + orderId;

        return redisService.get(cacheKey)
                .compose(cachedAddress -> {
                    if (cachedAddress != null && !cachedAddress.isEmpty()) {
                        logger.info("Shipping address for order {} found in cache", orderId);
                        span.setAttribute("shipping_address.cache_hit", true);
                        try {
                            ShippingAddress address = ShippingAddress.fromJson(new JsonObject(cachedAddress));
                            tracingMetrics.completeSpanSuccess(tracingContext, "get_by_order_id", "Shipping address fetched from cache");
                            return Future.succeededFuture(ApiResponse.success(
                                    "Shipping address fetched successfully (from cache)",
                                    ShippingAddressResponse.from(address)));
                        } catch (Exception e) {
                            logger.warn("Failed to parse cached shipping address data for order {}: {}", orderId, e.getMessage());
                            return fetchShippingAddressByOrderFromDatabase(orderId, tracingContext);
                        }
                    } else {
                        span.setAttribute("shipping_address.cache_hit", false);
                        return fetchShippingAddressByOrderFromDatabase(orderId, tracingContext);
                    }
                })
                .recover(err -> {
                    logger.error("Failed to fetch shipping address by order id: {}", orderId, err);
                    tracingMetrics.completeSpanError(tracingContext, "get_by_order_id", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to fetch shipping address: " + err.getMessage()));
                });
    }

    private Future<ApiResponse<ShippingAddressResponse>> fetchShippingAddressFromDatabase(Integer shippingAddressId, TracingMetrics.TracingContext tracingContext) {
        Span span = Span.fromContext(tracingContext.getContext());

        return repo.getShippingByID(shippingAddressId)
                .compose((ShippingAddress address) -> {
                    if (address == null) {
                        return Future.failedFuture(new RuntimeException("Shipping address not found with id: " + shippingAddressId));
                    }

                    span.setAttribute("shipping_address.alamat", address.getAlamat());

                    String cacheKey = "shipping_address:" + shippingAddressId;
                    redisService.setJson(cacheKey, address.toJson(), Duration.ofMinutes(60))
                            .onSuccess(v -> logger.debug("Shipping address {} cached successfully", shippingAddressId))
                            .onFailure(err -> logger.warn("Failed to cache shipping address {}: {}", shippingAddressId, err.getMessage()));

                    return Future.succeededFuture(ApiResponse.success(
                            "Shipping address fetched successfully",
                            ShippingAddressResponse.from(address)));
                });
    }

    private Future<ApiResponse<ShippingAddressResponse>> fetchShippingAddressByOrderFromDatabase(Integer orderId, TracingMetrics.TracingContext tracingContext) {
        Span span = Span.fromContext(tracingContext.getContext());

        return repo.getShippingAddressByOrderID(orderId)
                .compose((ShippingAddress address) -> {
                    if (address == null) {
                        return Future.failedFuture(new RuntimeException("Shipping address not found for order id: " + orderId));
                    }

                    span.setAttribute("shipping_address.alamat", address.getAlamat());

                    String cacheKey = "shipping_address:order:" + orderId;
                    redisService.setJson(cacheKey, address.toJson(), Duration.ofMinutes(60))
                            .onSuccess(v -> logger.debug("Shipping address for order {} cached successfully", orderId))
                            .onFailure(err -> logger.warn("Failed to cache shipping address for order {}: {}", orderId, err.getMessage()));

                    return Future.succeededFuture(ApiResponse.success(
                            "Shipping address fetched successfully",
                            ShippingAddressResponse.from(address)));
                });
    }

    private ApiResponsePagination<List<ShippingAddressResponse>> mapShippingAddressPagination(PagedResult<ShippingAddress> result, FindAllShippingAddress req, String message) {
        int pageSize = req.getPageSize();
        int totalRecords = result.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<ShippingAddressResponse> data = result.getData().stream().map(ShippingAddressResponse::from).toList();

        return new ApiResponsePagination<>(
                "success",
                message,
                data,
                new PaginationMeta(req.getPage() + 1, pageSize, totalPages, totalRecords));
    }

    private ApiResponsePagination<List<ShippingAddressResponseDeleteAt>> mapShippingAddressPaginationDeleteAt(PagedResult<ShippingAddress> result, FindAllShippingAddress req, String message) {
        int pageSize = req.getPageSize();
        int totalRecords = result.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<ShippingAddressResponseDeleteAt> data = result.getData().stream().map(ShippingAddressResponseDeleteAt::from).toList();

        return new ApiResponsePagination<>(
                "success",
                message,
                data,
                new PaginationMeta(req.getPage() + 1, pageSize, totalPages, totalRecords));
    }
}
