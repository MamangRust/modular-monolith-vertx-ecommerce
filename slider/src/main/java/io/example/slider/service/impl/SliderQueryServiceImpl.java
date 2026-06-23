package io.example.slider.service.impl;

import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.domain.PagedResult;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.slider.domain.requests.FindAllSlider;
import io.example.slider.model.Slider;
import io.example.slider.model.SliderResponse;
import io.example.slider.model.SliderResponseDeleteAt;
import io.example.slider.repository.SliderQueryRepository;
import io.example.slider.service.SliderQueryService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SliderQueryServiceImpl implements SliderQueryService {
    private final SliderQueryRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private PagedResult<SliderResponse> mapPagination(PagedResult<Slider> res) {
        List<SliderResponse> data = res.getData().stream().map(SliderResponse::from).toList();
        return new PagedResult<>(data, res.getTotalRecords());
    }

    private PagedResult<SliderResponseDeleteAt> mapPaginationDeleteAt(PagedResult<Slider> res) {
        List<SliderResponseDeleteAt> data = res.getData().stream().map(SliderResponseDeleteAt::from).toList();
        return new PagedResult<>(data, res.getTotalRecords());
    }

    @Override
    public Future<PagedResult<SliderResponse>> getAllSliders(FindAllSlider req) {
        var ctx = metrics.startSpan("SliderQueryService.getAll");
        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        String cacheKey = String.format("sliders:page:%d:search:%s", page, keyword);

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Slider> result = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Slider>>() {
                                    });
                            return Future.succeededFuture(mapPagination(result));
                        } catch (Exception e) {
                        }
                    }
                    return repo.getSliders(req)
                            .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL).map(v -> res))
                            .map(this::mapPagination);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getAll", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getAll", e.getMessage()));
    }

    @Override
    public Future<PagedResult<SliderResponseDeleteAt>> getActiveSliders(FindAllSlider req) {
        var ctx = metrics.startSpan("SliderQueryService.getActive");
        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        String cacheKey = String.format("sliders:active:page:%d:search:%s", page, keyword);

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Slider> result = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Slider>>() {
                                    });
                            return Future.succeededFuture(mapPaginationDeleteAt(result));
                        } catch (Exception e) {
                        }
                    }
                    return repo.getSlidersActive(req)
                            .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL).map(v -> res))
                            .map(this::mapPaginationDeleteAt);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getActive", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getActive", e.getMessage()));
    }

    @Override
    public Future<PagedResult<SliderResponseDeleteAt>> getTrashedSliders(FindAllSlider req) {
        var ctx = metrics.startSpan("SliderQueryService.getTrashed");
        int page = req.getPage() > 0 ? req.getPage() - 1 : 0;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        req.setPage(page);
        req.setPageSize(pageSize);
        req.setSearch(keyword);

        String cacheKey = String.format("sliders:trashed:page:%d:search:%s", page, keyword);

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Slider> result = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Slider>>() {
                                    });
                            return Future.succeededFuture(mapPaginationDeleteAt(result));
                        } catch (Exception e) {
                        }
                    }
                    return repo.getSlidersTrashed(req)
                            .compose(res -> redis.set(cacheKey, Json.encode(res), CACHE_TTL).map(v -> res))
                            .map(this::mapPaginationDeleteAt);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTrashed", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getTrashed", e.getMessage()));
    }

    @Override
    public Future<SliderResponse> getSliderById(Long sliderId) {
        var ctx = metrics.startSpan("SliderQueryService.getById",
                Attributes.builder().put("slider.id", sliderId).build());
        String cacheKey = "slider:" + sliderId;

        return redis.getJson(cacheKey, Slider.class)
                .compose(cached -> {
                    if (cached != null) {
                        return Future.succeededFuture(SliderResponse.from(cached));
                    }
                    return repo.getSliderById(sliderId)
                            .compose(db -> {
                                if (db == null) {
                                    return Future.<Slider>failedFuture(new NotFoundException("Slider not found"));
                                }
                                return redis.setJson(cacheKey, db, Duration.ofMinutes(60)).<Slider>map(v -> db);
                            })
                            .map(SliderResponse::from);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getById", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getById", e.getMessage()));
    }
}