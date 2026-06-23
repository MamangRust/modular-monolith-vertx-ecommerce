package io.example.product.service.impl;

import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.domain.PagedResult;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.product.domain.requests.FindAllProductCategoryRequest;
import io.example.product.domain.requests.FindAllProductMerchantRequest;
import io.example.product.domain.requests.FindAllProductRequest;
import io.example.product.model.Product;
import io.example.product.model.ProductResponse;
import io.example.product.model.ProductResponseDeleteAt;
import io.example.product.repository.ProductQueryRepository;
import io.example.product.service.ProductQueryService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {
    private final ProductQueryRepository repository;
    private final RedisService redis;
    private final TracingMetrics metrics;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private PagedResult<ProductResponse> mapPagination(PagedResult<Product> res, int page, int pageSize) {
        List<ProductResponse> data = res.getData().stream().map(ProductResponse::from).toList();
        return new PagedResult<>(data, res.getTotalRecords());
    }

    private PagedResult<ProductResponseDeleteAt> mapPaginationDeleteAt(PagedResult<Product> res) {
        List<ProductResponseDeleteAt> data = res.getData().stream().map(ProductResponseDeleteAt::from).toList();
        return new PagedResult<>(data, res.getTotalRecords());
    }

    @Override
    public Future<PagedResult<ProductResponse>> getAll(FindAllProductRequest req) {
        var ctx = metrics.startSpan("ProductQueryService.getAll");
        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = req.getSearch() != null ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        String cacheKey = String.format("products:page:%d:search:%s", page, keyword);

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Product> result = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Product>>() {
                                    });
                            return Future.succeededFuture(mapPagination(result, page, pageSize));
                        } catch (Exception e) {
                        }
                    }
                    return repository.findAll(req)
                            .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL).map(v -> res))
                            .map(res -> mapPagination(res, page, pageSize));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getAll", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getAll", e.getMessage()));
    }

    @Override
    public Future<PagedResult<ProductResponseDeleteAt>> getActive(FindAllProductRequest req) {
        var ctx = metrics.startSpan("ProductQueryService.getActive");
        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = req.getSearch() != null ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        String cacheKey = String.format("products:active:page:%d:search:%s", page, keyword);

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Product> result = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Product>>() {
                                    });
                            return Future.succeededFuture(mapPaginationDeleteAt(result));
                        } catch (Exception e) {
                            // fallback to db
                        }
                    }
                    return repository.findActive(req)
                            .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL).map(v -> res))
                            .map(this::mapPaginationDeleteAt);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getActive", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getActive", e.getMessage()));
    }

    @Override
    public Future<PagedResult<ProductResponseDeleteAt>> getTrashed(FindAllProductRequest req) {
        var ctx = metrics.startSpan("ProductQueryService.getTrashed");
        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = req.getSearch() != null ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        String cacheKey = String.format("products:trashed:page:%d:search:%s", page, keyword);

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Product> result = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Product>>() {
                                    });
                            return Future.succeededFuture(mapPaginationDeleteAt(result));
                        } catch (Exception e) {
                            // fallback to db
                        }
                    }
                    return repository.findTrashed(req)
                            .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL).map(v -> res))
                            .map(this::mapPaginationDeleteAt);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTrashed", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getTrashed", e.getMessage()));
    }

    @Override
    public Future<PagedResult<ProductResponse>> getByMerchant(FindAllProductMerchantRequest req) {
        var ctx = metrics.startSpan("ProductQueryService.getByMerchant",
                Attributes.builder().put("merchant.id", req.getMerchantId()).build());
        String cacheKey = String.format("products:merchant:%d:search:%s:cat:%s:min:%s:max:%s:page:%d:size:%d",
                req.getMerchantId(), req.getSearch(), req.getCategoryId(), req.getMinPrice(), req.getMaxPrice(),
                req.getPage(), req.getPageSize());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Product> result = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Product>>() {
                                    });
                            return Future.succeededFuture(mapPagination(result, req.getPage(), req.getPageSize()));
                        } catch (Exception e) {
                        }
                    }
                    return repository.findByMerchant(req)
                            .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL).map(v -> res))
                            .map(res -> mapPagination(res, req.getPage(), req.getPageSize()));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getByMerchant", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getByMerchant", e.getMessage()));
    }

    @Override
    public Future<PagedResult<ProductResponse>> getByCategoryName(FindAllProductCategoryRequest req) {
        var ctx = metrics.startSpan("ProductQueryService.getByCategoryName");
        String cacheKey = String.format("products:category:%s:search:%s:min:%s:max:%s:page:%d:size:%d",
                req.getCategoryName(),
                req.getSearch(), req.getMinPrice(), req.getMaxPrice(), req.getPage(), req.getPageSize());

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Product> result = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Product>>() {
                                    });
                            return Future.succeededFuture(mapPagination(result, req.getPage(), req.getPageSize()));
                        } catch (Exception e) {
                        }
                    }
                    return repository.findByCategory(req)
                            .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL).map(v -> res))
                            .map(res -> mapPagination(res, req.getPage(), req.getPageSize()));
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getByCategoryName", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getByCategoryName", e.getMessage()));
    }

    @Override
    public Future<ProductResponse> getById(Long id) {
        var ctx = metrics.startSpan("ProductQueryService.getById", Attributes.builder().put("product.id", id).build());
        String cacheKey = "product:" + id;

        return redis.getJson(cacheKey, Product.class)
                .compose(cached -> {
                    if (cached != null) {
                        return Future.succeededFuture(ProductResponse.from(cached));
                    }
                    return repository.findById(id)
                            .compose(db -> {
                                if (db == null) {
                                    return Future.<Product>failedFuture(new NotFoundException("Product not found"));
                                }
                                return redis.setJson(cacheKey, db, Duration.ofMinutes(60)).<Product>map(v -> db);
                            })
                            .map(ProductResponse::from);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getById", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getById", e.getMessage()));
    }
}