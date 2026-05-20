package io.example.product.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.example.common.domain.ApiResponse;
import io.example.common.domain.ApiResponsePagination;
import io.example.common.domain.PagedResult;
import io.example.common.domain.PaginationMeta;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.product.model.FindAllProductRequest;
import io.example.product.model.Product;
import io.example.product.model.ProductResponse;
import io.example.product.model.ProductResponseDeleteAt;
import io.example.product.repository.ProductQueryRepository;
import io.example.product.service.ProductQueryService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import io.vertx.core.json.Json;

public class ProductQueryServiceImpl implements ProductQueryService {
    private static final Logger logger = LoggerFactory.getLogger(ProductQueryServiceImpl.class);

    private final ProductQueryRepository repository;
    private final RedisService redisService;
    private final TracingMetrics tracingMetrics;
    private final ObjectMapper mapper = new ObjectMapper();

    public ProductQueryServiceImpl(
            ProductQueryRepository repository,
            RedisService redisService,
            TracingMetrics tracingMetrics) {
        this.repository = repository;
        this.redisService = redisService;
        this.tracingMetrics = tracingMetrics;
    }

    @Override
    public Future<ApiResponsePagination<List<ProductResponse>>> getAll(FindAllProductRequest req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ProductQueryService.getAll");
        Span span = Span.fromContext(tracingContext.getContext());

        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = req.getSearch() != null ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        String cacheKey = String.format("products:page:%d:search:%s", page, keyword);

        return redisService.get(cacheKey)
                .<ApiResponsePagination<List<ProductResponse>>>compose(cachedResult -> {
                    if (cachedResult != null && !cachedResult.isEmpty()) {
                        logger.info("Product cache hit for key: {}", cacheKey);
                        span.setAttribute("cache.hit", true);
                        try {
                            PagedResult<Product> result = mapper.readValue(
                                    cachedResult,
                                    new TypeReference<PagedResult<Product>>() {
                                    });

                            return Future.succeededFuture(mapToPagedResponse(result, req));
                        } catch (Exception e) {
                            logger.warn("Failed to parse product cache: {}", e.getMessage());
                        }
                    }

                    span.setAttribute("cache.hit", false);
                    return repository.findAll(req)
                            .map(result -> {
                                redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                                        .onFailure(err -> logger.warn("Failed to set product cache: {}",
                                                err.getMessage()));

                                return mapToPagedResponse(result, req);
                            });
                })
                .map(response -> {
                    span.setAttribute("records.count", response.data().size());
                    span.setAttribute("records.total", response.pagination().totalRecords());
                    tracingMetrics.completeSpanSuccess(tracingContext, "get_all", "Success");
                    return response;
                })
                .recover(err -> {
                    logger.error("Failed to fetch products", err);
                    tracingMetrics.completeSpanError(tracingContext, "get_all", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch data: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<ProductResponse>>> getActive(FindAllProductRequest req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ProductQueryService.getActive");
        Span span = Span.fromContext(tracingContext.getContext());

        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = req.getSearch() != null ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        String cacheKey = String.format("products:active:page:%d:search:%s", page, keyword);

        return redisService.get(cacheKey)
                .<ApiResponsePagination<List<ProductResponse>>>compose(cachedResult -> {
                    if (cachedResult != null && !cachedResult.isEmpty()) {
                        logger.info("Active product cache hit for key: {}", cacheKey);
                        span.setAttribute("cache.hit", true);
                        try {
                            PagedResult<Product> result = mapper.readValue(
                                    cachedResult,
                                    new TypeReference<PagedResult<Product>>() {
                                    });

                            return Future.succeededFuture(mapToPagedResponse(result, req));
                        } catch (Exception e) {
                            logger.warn("Failed to parse active product cache: {}", e.getMessage());
                        }
                    }

                    span.setAttribute("cache.hit", false);
                    return repository.findActive(req)
                            .map(result -> {
                                redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                                        .onFailure(err -> logger.warn("Failed to set active product cache: {}",
                                                err.getMessage()));

                                return mapToPagedResponse(result, req);
                            });
                })
                .map(response -> {
                    span.setAttribute("records.count", response.data().size());
                    span.setAttribute("records.total", response.pagination().totalRecords());
                    tracingMetrics.completeSpanSuccess(tracingContext, "get_active", "Success");
                    return response;
                })
                .recover(err -> {
                    logger.error("Failed to fetch active products", err);
                    tracingMetrics.completeSpanError(tracingContext, "get_active", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch data: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<ProductResponseDeleteAt>>> getTrashed(FindAllProductRequest req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ProductQueryService.getTrashed");
        Span span = Span.fromContext(tracingContext.getContext());

        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = req.getSearch() != null ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        String cacheKey = String.format("products:trashed:page:%d:search:%s", page, keyword);

        return redisService.get(cacheKey)
                .<ApiResponsePagination<List<ProductResponseDeleteAt>>>compose(cachedResult -> {
                    if (cachedResult != null && !cachedResult.isEmpty()) {
                        logger.info("Trashed product cache hit for key: {}", cacheKey);
                        span.setAttribute("cache.hit", true);
                        try {
                            PagedResult<Product> result = mapper.readValue(
                                    cachedResult,
                                    new TypeReference<PagedResult<Product>>() {
                                    });

                            return Future.succeededFuture(mapToPagedResponseDeleteAt(result, req));
                        } catch (Exception e) {
                            logger.warn("Failed to parse trashed product cache: {}", e.getMessage());
                        }
                    }

                    span.setAttribute("cache.hit", false);
                    return repository.findTrashed(req)
                            .map(result -> {
                                redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                                        .onFailure(err -> logger.warn("Failed to set trashed product cache: {}",
                                                err.getMessage()));

                                return mapToPagedResponseDeleteAt(result, req);
                            });
                })
                .map(response -> {
                    span.setAttribute("records.count", response.data().size());
                    span.setAttribute("records.total", response.pagination().totalRecords());
                    tracingMetrics.completeSpanSuccess(tracingContext, "get_trashed", "Success");
                    return response;
                })
                .recover(err -> {
                    logger.error("Failed to fetch trashed products", err);
                    tracingMetrics.completeSpanError(tracingContext, "get_trashed", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch data: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<ProductResponse>>> getByMerchant(
            Long merchantId, String search, Long categoryId, Integer minPrice, Integer maxPrice, int page,
            int pageSize) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("ProductQueryService.getByMerchant");
        Span span = Span.fromContext(tracingContext.getContext());
        span.setAttribute("merchant.id", merchantId);

        String cacheKey = String.format("products:merchant:%d:search:%s:cat:%s:min:%s:max:%s:page:%d:size:%d",
                merchantId, search, categoryId, minPrice, maxPrice, page, pageSize);

        return redisService.get(cacheKey)
                .<PagedResult<Product>>compose(cachedResult -> {
                    if (cachedResult != null && !cachedResult.isEmpty()) {
                        logger.info("Product by merchant cache hit for key: {}", cacheKey);
                        span.setAttribute("cache.hit", true);
                        try {
                            PagedResult<Product> result = mapper.readValue(
                                    cachedResult,
                                    new TypeReference<PagedResult<Product>>() {
                                    });
                            return Future.succeededFuture(result);
                        } catch (Exception e) {
                            logger.warn("Failed to parse product by merchant cache: {}", e.getMessage());
                        }
                    }

                    span.setAttribute("cache.hit", false);
                    return repository.findByMerchant(merchantId, search, categoryId, minPrice, maxPrice, page, pageSize)
                            .compose(result -> {
                                return redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                                        .map(result)
                                        .recover(err -> {
                                            logger.warn("Failed to set product by merchant cache: {}",
                                                    err.getMessage());
                                            return Future.succeededFuture(result);
                                        });
                            });
                })
                .map(result -> {
                    ApiResponsePagination<List<ProductResponse>> response = mapToPagedResponse(result, page, pageSize);
                    span.setAttribute("records.count", response.data().size());
                    span.setAttribute("records.total", response.pagination().totalRecords());
                    tracingMetrics.completeSpanSuccess(tracingContext, "get_by_merchant", "Success");
                    return response;
                })
                .recover(err -> {
                    logger.error("Failed to fetch products by merchant", err);
                    tracingMetrics.completeSpanError(tracingContext, "get_by_merchant", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch data: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<ProductResponse>>> getByCategoryName(
            String categoryName, String search, Integer minPrice, Integer maxPrice, int page, int pageSize) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics
                .startSpan("ProductQueryService.getByCategoryName");
        Span span = Span.fromContext(tracingContext.getContext());

        String cacheKey = String.format("products:category:%s:search:%s:min:%s:max:%s:page:%d:size:%d",
                categoryName, search, minPrice, maxPrice, page, pageSize);

        return redisService.get(cacheKey)
                .<PagedResult<Product>>compose(cachedResult -> {
                    if (cachedResult != null && !cachedResult.isEmpty()) {
                        logger.info("Product by category cache hit for key: {}", cacheKey);
                        span.setAttribute("cache.hit", true);
                        try {
                            PagedResult<Product> result = mapper.readValue(
                                    cachedResult,
                                    new TypeReference<PagedResult<Product>>() {
                                    });
                            return Future.succeededFuture(result);
                        } catch (Exception e) {
                            logger.warn("Failed to parse product by category cache: {}", e.getMessage());
                        }
                    }

                    span.setAttribute("cache.hit", false);
                    return repository.findByCategory(categoryName, search, minPrice, maxPrice, page, pageSize)
                            .compose(result -> {
                                return redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                                        .map(result)
                                        .recover(err -> {
                                            logger.warn("Failed to set product by category cache: {}",
                                                    err.getMessage());
                                            return Future.succeededFuture(result);
                                        });
                            });
                })
                .map(result -> {
                    ApiResponsePagination<List<ProductResponse>> response = mapToPagedResponse(result, page, pageSize);
                    span.setAttribute("records.count", response.data().size());
                    span.setAttribute("records.total", response.pagination().totalRecords());
                    tracingMetrics.completeSpanSuccess(tracingContext, "get_by_category", "Success");
                    return response;
                })
                .recover(err -> {
                    logger.error("Failed to fetch products by category", err);
                    tracingMetrics.completeSpanError(tracingContext, "get_by_category", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch data: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<ProductResponse>> getById(Long id) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "ProductQueryService.getById",
                Attributes.builder().put("id", id).build());
        Span span = Span.fromContext(tracingContext.getContext());

        logger.info("Fetching product by id: {}", id);
        String cacheKey = "product:" + id;

        return redisService.get(cacheKey)
                .<ApiResponse<ProductResponse>>compose(cached -> {
                    if (cached != null && !cached.isEmpty()) {
                        span.setAttribute("cache.hit", true);
                        try {
                            Product data = Json.decodeValue(cached, Product.class);
                            return Future.succeededFuture(
                                    ApiResponse.success("Data from cache", ProductResponse.from(data)));
                        } catch (Exception e) {
                            logger.warn("Cache parse error: {}", e.getMessage());
                        }
                    }

                    span.setAttribute("cache.hit", false);
                    return repository.findById(id)
                            .<ApiResponse<ProductResponse>>compose(data -> {
                                if (data == null) {
                                    return Future.failedFuture(new RuntimeException("Product not found"));
                                }
                                return redisService.set(cacheKey, Json.encode(data), Duration.ofMinutes(60))
                                        .map(ApiResponse.success("Data fetched successfully",
                                                ProductResponse.from(data)))
                                        .recover(err -> {
                                            logger.warn("Cache set failed: {}", err.getMessage());
                                            return Future.succeededFuture(ApiResponse
                                                    .success("Data fetched successfully", ProductResponse.from(data)));
                                        });
                            });
                })
                .map(response -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "get_by_id", "Success");
                    return response;
                })
                .recover(err -> {
                    logger.error("Failed to fetch by id", err);
                    tracingMetrics.completeSpanError(tracingContext, "get_by_id", err.getMessage());
                    return Future.succeededFuture(ApiResponse.error(err.getMessage()));
                });
    }

    private ApiResponsePagination<List<ProductResponse>> mapToPagedResponse(PagedResult<Product> result,
            FindAllProductRequest req) {
        return mapToPagedResponse(result, req.getPage(), req.getPageSize());
    }

    private ApiResponsePagination<List<ProductResponse>> mapToPagedResponse(PagedResult<Product> result, int page,
            int pageSize) {
        List<ProductResponse> data = result.getData().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
        return new ApiResponsePagination<>(
                "success", "Data fetched", data,
                new PaginationMeta(page, pageSize,
                        (int) Math.ceil((double) result.getTotalRecords() / pageSize),
                        result.getTotalRecords()));
    }

    private ApiResponsePagination<List<ProductResponseDeleteAt>> mapToPagedResponseDeleteAt(PagedResult<Product> result,
            FindAllProductRequest req) {
        List<ProductResponseDeleteAt> data = result.getData().stream()
                .map(ProductResponseDeleteAt::from)
                .collect(Collectors.toList());
        return new ApiResponsePagination<>(
                "success", "Data fetched", data,
                new PaginationMeta(req.getPage(), req.getPageSize(),
                        (int) Math.ceil((double) result.getTotalRecords() / req.getPageSize()),
                        result.getTotalRecords()));
    }
}
