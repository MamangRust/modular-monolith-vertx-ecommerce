package io.example.cart.service.impl;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.domain.PagedResult;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.cart.model.Cart;
import io.example.cart.model.CartResponse;
import io.example.cart.repository.CartQueryRepository;
import io.example.cart.service.CartQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import io.example.cart.domain.requests.FindAllCartsRequest;

@RequiredArgsConstructor
public class CartQueryServiceImpl implements CartQueryService {
    private static final Logger log = LoggerFactory.getLogger(CartQueryServiceImpl.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final CartQueryRepository repository;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "cart:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private PagedResult<CartResponse> mapPagination(PagedResult<Cart> res) {
        List<CartResponse> data = res.getData().stream().map(CartResponse::from).toList();
        return new PagedResult<>(data, res.getTotalRecords());
    }

    @Override
    public Future<PagedResult<CartResponse>> findAll(FindAllCartsRequest req) {
        var ctx = metrics.startSpan("CartQueryService.findAll");
        String cacheKey = CACHE_PREFIX + "list:all:" + req.getUserId() + ":" +
                (req.getSearch() != null ? req.getSearch() : "") + ":" + req.getPage() + ":" + req.getPageSize();

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Cart> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Cart>>() {
                                    });
                            return Future.succeededFuture(mapPagination(typedCached));
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached carts: {}", e.getMessage());
                        }
                    }
                    return repository.getCarts(FindAllCartsRequest.builder()
                            .userId(req.getUserId())
                            .search(req.getSearch())
                            .page(req.getPage())
                            .pageSize(req.getPageSize())
                            .build())
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
                            .map(this::mapPagination);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "findAll", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "findAll", e.getMessage()));
    }
}