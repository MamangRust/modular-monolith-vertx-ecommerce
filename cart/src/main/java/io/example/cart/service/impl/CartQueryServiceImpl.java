package io.example.cart.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.domain.PagedResult;
import io.example.common.model.ApiResponsePagination;
import io.example.common.model.PaginationMeta;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.cart.model.Cart;
import io.example.cart.model.CartResponse;
import io.example.cart.repository.CartQueryRepository;
import io.example.cart.service.CartQueryService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import pb.cart.CartQuery.FindAllCartRequest;

public class CartQueryServiceImpl implements CartQueryService {
    private static final Logger logger = LoggerFactory.getLogger(CartQueryServiceImpl.class);

    private final CartQueryRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "cart:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    public CartQueryServiceImpl(
            CartQueryRepository repo,
            RedisService redis,
            TracingMetrics metrics) {
        this.repo = repo;
        this.redis = redis;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponsePagination<List<CartResponse>>> findAll(FindAllCartRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("CartQueryService.findAll");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        Integer userId = req.getUserId();
        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        String cacheKey = String.format("%sall:u:%d:p:%d:s:%d:k:%s", CACHE_PREFIX, userId, page, pageSize, keyword);

        return redis.getJson(cacheKey, ApiResponsePagination.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("cart.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "find_all", "Carts fetched from cache");
                        @SuppressWarnings("unchecked")
                        ApiResponsePagination<List<CartResponse>> typedCached = (ApiResponsePagination<List<CartResponse>>) cached;
                        return Future.succeededFuture(typedCached);
                    }
                    span.setAttribute("cart.cache_hit", false);
                    return repo.getCarts(userId, keyword, page, pageSize)
                            .map(result -> mapCartPagination(result, page, pageSize))
                            .compose(response -> redis.setJson(cacheKey, response, CACHE_TTL).map(response));
                })
                .onSuccess(response -> {
                    @SuppressWarnings("unchecked")
                    ApiResponsePagination<List<CartResponse>> typedResponse = (ApiResponsePagination<List<CartResponse>>) response;
                    span.setAttribute("carts.count", (long) typedResponse.data().size());
                    span.setAttribute("carts.total_records", (long) typedResponse.pagination().totalRecords());
                    metrics.completeSpanSuccess(tracingContext, "find_all", "Carts fetched successfully");
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch carts for user ID: {}", userId, throwable);
                    metrics.completeSpanError(tracingContext, "find_all", throwable.getMessage());

                    return Future.succeededFuture(
                            ApiResponsePagination.<List<CartResponse>>error("Failed to fetch carts: " + throwable.getMessage()));
                });
    }

    private ApiResponsePagination<List<CartResponse>> mapCartPagination(
            PagedResult<Cart> result,
            int page,
            int pageSize) {

        int totalRecords = result.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<CartResponse> data = result.getData()
                .stream()
                .map(CartResponse::from)
                .toList();

        return new ApiResponsePagination<>(
                "success",
                "Carts found",
                data,
                new PaginationMeta(
                        page,
                        pageSize,
                        totalPages,
                        totalRecords));
    }
}
