package io.example.slider.service.impl;

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
import io.example.slider.model.FindAllSlider;
import io.example.slider.model.Slider;
import io.example.slider.model.SliderResponse;
import io.example.slider.model.SliderResponseDeleteAt;
import io.example.slider.repository.SliderQueryRepository;
import io.example.slider.service.SliderQueryService;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class SliderQueryServiceImpl implements SliderQueryService {
    private static final Logger logger = LoggerFactory.getLogger(SliderQueryServiceImpl.class);
    private final SliderQueryRepository repo;
    private final RedisService redisService;
    private final TracingMetrics tracingMetrics;
    private final ObjectMapper mapper = new ObjectMapper();

    public SliderQueryServiceImpl(
            SliderQueryRepository repo,
            RedisService redisService,
            TracingMetrics tracingMetrics) {
        this.repo = repo;
        this.redisService = redisService;
        this.tracingMetrics = tracingMetrics;
    }

    @Override
    public Future<ApiResponsePagination<List<SliderResponse>>> getAllSliders(FindAllSlider req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("SliderService.getAllSliders");
        Span span = Span.fromContext(tracingContext.getContext());

        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        logger.info("Fetching sliders | search={}, page={}, pageSize={}", keyword, page, pageSize);

        String cacheKey = String.format("sliders:page:%d:search:%s", page, keyword);

        return redisService.get(cacheKey)
                .<ApiResponsePagination<List<SliderResponse>>>compose(cachedResult -> {
                    if (cachedResult != null && !cachedResult.isEmpty()) {
                        logger.info("Sliders cache hit for key: {}", cacheKey);
                        span.setAttribute("cache.hit", true);
                        try {
                            PagedResult<Slider> result = mapper.readValue(
                                    cachedResult,
                                    new TypeReference<PagedResult<Slider>>() {
                                    });

                            ApiResponsePagination<List<SliderResponse>> response = mapSliderPagination(result, req,
                                    "Sliders fetched successfully (from cache)");
                            return Future.succeededFuture(response);
                        } catch (Exception e) {
                            logger.warn("Failed to parse sliders cache: {}", e.getMessage());
                        }
                    }

                    span.setAttribute("cache.hit", false);
                    return repo.getSliders(req)
                            .map(result -> {
                                redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                                        .onFailure(err -> logger.warn("Failed to set sliders cache: {}",
                                                err.getMessage()));

                                return mapSliderPagination(result, req, "Sliders fetched successfully");
                            });
                })
                .map(response -> {
                    span.setAttribute("sliders.count", response.data().size());
                    span.setAttribute("sliders.total_records", response.pagination().totalRecords());
                    tracingMetrics.completeSpanSuccess(tracingContext, "get_all", "Sliders fetched successfully");
                    return response;
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch sliders", throwable);
                    tracingMetrics.completeSpanError(tracingContext, "get_all", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch sliders: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<SliderResponseDeleteAt>>> getActiveSliders(FindAllSlider req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("SliderService.getActiveSliders");
        Span span = Span.fromContext(tracingContext.getContext());

        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        logger.info("Fetching active sliders | search={}, page={}, pageSize={}", keyword, page, pageSize);

        String cacheKey = String.format("sliders:active:page:%d:search:%s", page, keyword);

        return redisService.get(cacheKey)
                .<ApiResponsePagination<List<SliderResponseDeleteAt>>>compose(cachedResult -> {
                    if (cachedResult != null && !cachedResult.isEmpty()) {
                        logger.info("Active sliders cache hit for key: {}", cacheKey);
                        span.setAttribute("cache.hit", true);
                        try {
                            PagedResult<Slider> result = mapper.readValue(
                                    cachedResult,
                                    new TypeReference<PagedResult<Slider>>() {
                                    });

                            ApiResponsePagination<List<SliderResponseDeleteAt>> response = mapSliderPaginationDeleteAt(
                                    result, req, "Active sliders fetched successfully (from cache)");
                            return Future.succeededFuture(response);
                        } catch (Exception e) {
                            logger.warn("Failed to parse active sliders cache: {}", e.getMessage());
                        }
                    }

                    span.setAttribute("cache.hit", false);
                    return repo.getSlidersActive(req)
                            .map(result -> {
                                redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                                        .onFailure(err -> logger.warn("Failed to set active sliders cache: {}",
                                                err.getMessage()));

                                return mapSliderPaginationDeleteAt(result, req, "Active sliders fetched successfully");
                            });
                })
                .map(response -> {
                    span.setAttribute("sliders.count", response.data().size());
                    span.setAttribute("sliders.total_records", response.pagination().totalRecords());
                    tracingMetrics.completeSpanSuccess(tracingContext, "get_active",
                            "Active sliders fetched successfully");
                    return response;
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch active sliders", throwable);
                    tracingMetrics.completeSpanError(tracingContext, "get_active", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch active sliders: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<SliderResponseDeleteAt>>> getTrashedSliders(FindAllSlider req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("SliderService.getTrashedSliders");
        Span span = Span.fromContext(tracingContext.getContext());

        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        logger.info("Fetching trashed sliders | search={}, page={}, pageSize={}", keyword, page, pageSize);

        String cacheKey = String.format("sliders:trashed:page:%d:search:%s", page, keyword);

        return redisService.get(cacheKey)
                .<ApiResponsePagination<List<SliderResponseDeleteAt>>>compose(cachedResult -> {
                    if (cachedResult != null && !cachedResult.isEmpty()) {
                        logger.info("Trashed sliders cache hit for key: {}", cacheKey);
                        span.setAttribute("cache.hit", true);
                        try {
                            PagedResult<Slider> result = mapper.readValue(
                                    cachedResult,
                                    new TypeReference<PagedResult<Slider>>() {
                                    });

                            ApiResponsePagination<List<SliderResponseDeleteAt>> response = mapSliderPaginationDeleteAt(
                                    result, req, "Trashed sliders fetched successfully (from cache)");
                            return Future.succeededFuture(response);
                        } catch (Exception e) {
                            logger.warn("Failed to parse trashed sliders cache: {}", e.getMessage());
                        }
                    }

                    span.setAttribute("cache.hit", false);
                    return repo.getSlidersTrashed(req)
                            .map(result -> {
                                redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                                        .onFailure(err -> logger.warn("Failed to set trashed sliders cache: {}",
                                                err.getMessage()));

                                return mapSliderPaginationDeleteAt(result, req, "Trashed sliders fetched successfully");
                            });
                })
                .map(response -> {
                    span.setAttribute("sliders.count", response.data().size());
                    span.setAttribute("sliders.total_records", response.pagination().totalRecords());
                    tracingMetrics.completeSpanSuccess(tracingContext, "get_trashed",
                            "Trashed sliders fetched successfully");
                    return response;
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch trashed sliders", throwable);
                    tracingMetrics.completeSpanError(tracingContext, "get_trashed", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.error("Failed to fetch trashed sliders: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<SliderResponse>> getSliderById(Long sliderId) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "SliderService.getSliderById",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("slider.id", sliderId)
                        .build());
        Span span = Span.fromContext(tracingContext.getContext());

        logger.info("Fetching slider by id: {}", sliderId);
        String cacheKey = "slider:" + sliderId;

        return redisService.get(cacheKey)
                .compose(cachedSlider -> {
                    if (cachedSlider != null && !cachedSlider.isEmpty()) {
                        logger.info("Slider {} found in cache", sliderId);
                        span.setAttribute("slider.cache_hit", true);
                        try {
                            Slider slider = Slider.fromJson(new JsonObject(cachedSlider));
                            tracingMetrics.completeSpanSuccess(tracingContext, "get_by_id",
                                    "Slider fetched from cache");
                            return Future.succeededFuture(ApiResponse.success(
                                    "Slider fetched successfully (from cache)",
                                    SliderResponse.from(slider)));
                        } catch (Exception e) {
                            logger.warn("Failed to parse cached slider data for {}: {}", sliderId, e.getMessage());
                            return fetchSliderFromDatabase(sliderId, tracingContext);
                        }
                    } else {
                        span.setAttribute("slider.cache_hit", false);
                        return fetchSliderFromDatabase(sliderId, tracingContext);
                    }
                })
                .recover(err -> {
                    logger.error("Failed to fetch slider by id: {}", sliderId, err);
                    tracingMetrics.completeSpanError(tracingContext, "get_by_id", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to fetch slider: " + err.getMessage()));
                });
    }

    private Future<ApiResponse<SliderResponse>> fetchSliderFromDatabase(Long sliderId,
            TracingMetrics.TracingContext tracingContext) {
        Span span = Span.fromContext(tracingContext.getContext());

        return repo.getSliderById(sliderId)
                .compose((Slider slider) -> {
                    if (slider == null) {
                        return Future.failedFuture(new RuntimeException("Slider not found with id: " + sliderId));
                    }

                    span.setAttribute("slider.name", slider.getName());

                    String cacheKey = "slider:" + sliderId;
                    redisService.setJson(cacheKey, slider.toJson(), Duration.ofMinutes(60))
                            .onSuccess(v -> logger.debug("Slider {} cached successfully", sliderId))
                            .onFailure(err -> logger.warn("Failed to cache slider {}: {}", sliderId, err.getMessage()));

                    return Future.succeededFuture(ApiResponse.success(
                            "Slider fetched successfully",
                            SliderResponse.from(slider)));
                });
    }

    private ApiResponsePagination<List<SliderResponse>> mapSliderPagination(PagedResult<Slider> result,
            FindAllSlider req, String message) {
        int pageSize = req.getPageSize();
        int totalRecords = result.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<SliderResponse> data = result.getData().stream().map(SliderResponse::from).toList();

        return new ApiResponsePagination<>(
                "success",
                message,
                data,
                new PaginationMeta(req.getPage() + 1, pageSize, totalPages, totalRecords));
    }

    private ApiResponsePagination<List<SliderResponseDeleteAt>> mapSliderPaginationDeleteAt(PagedResult<Slider> result,
            FindAllSlider req, String message) {
        int pageSize = req.getPageSize();
        int totalRecords = result.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<SliderResponseDeleteAt> data = result.getData().stream().map(SliderResponseDeleteAt::from).toList();

        return new ApiResponsePagination<>(
                "success",
                message,
                data,
                new PaginationMeta(req.getPage() + 1, pageSize, totalPages, totalRecords));
    }
}
