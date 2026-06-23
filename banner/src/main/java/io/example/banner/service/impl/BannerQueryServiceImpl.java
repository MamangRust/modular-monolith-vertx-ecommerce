package io.example.banner.service.impl;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.domain.PagedResult;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.banner.domain.requests.FindAllBannerRequest;
import io.example.banner.model.Banner;
import io.example.banner.model.BannerResponse;
import io.example.banner.model.BannerResponseDeleteAt;
import io.example.banner.repository.BannerQueryRepository;
import io.example.banner.service.BannerQueryService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BannerQueryServiceImpl implements BannerQueryService {
    private static final Logger log = LoggerFactory.getLogger(BannerQueryServiceImpl.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final BannerQueryRepository repository;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "banner:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private PagedResult<BannerResponse> mapPagination(PagedResult<Banner> res) {
        List<BannerResponse> data = res.getData().stream().map(BannerResponse::from).toList();
        return new PagedResult<>(data, res.getTotalRecords());
    }

    private PagedResult<BannerResponseDeleteAt> mapPaginationDeleteAt(PagedResult<Banner> res) {
        List<BannerResponseDeleteAt> data = res.getData().stream().map(BannerResponseDeleteAt::from).toList();
        return new PagedResult<>(data, res.getTotalRecords());
    }

    @Override
    public Future<PagedResult<BannerResponse>> getBanners(FindAllBannerRequest req) {
        var ctx = metrics.startSpan("BannerQueryService.getBanners");
        String cacheKey = CACHE_PREFIX + "list:all:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
                + req.getPage() + ":" + req.getPageSize();

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Banner> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Banner>>() {
                                    });
                            return Future.succeededFuture(mapPagination(typedCached));
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached banners: {}", e.getMessage());
                        }
                    }
                    return repository.getBanners(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
                            .map(this::mapPagination);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getBanners", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getBanners", e.getMessage()));
    }

    @Override
    public Future<PagedResult<BannerResponseDeleteAt>> getActiveBanners(FindAllBannerRequest req) {
        var ctx = metrics.startSpan("BannerQueryService.getActiveBanners");
        String cacheKey = CACHE_PREFIX + "list:active:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
                + req.getPage() + ":" + req.getPageSize();

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Banner> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Banner>>() {
                                    });
                            return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached active banners: {}", e.getMessage());
                        }
                    }
                    return repository.getActiveBanners(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
                            .map(this::mapPaginationDeleteAt);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getActiveBanners", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getActiveBanners", e.getMessage()));
    }

    @Override
    public Future<PagedResult<BannerResponseDeleteAt>> getTrashedBanners(FindAllBannerRequest req) {
        var ctx = metrics.startSpan("BannerQueryService.getTrashedBanners");
        String cacheKey = CACHE_PREFIX + "list:trashed:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
                + req.getPage() + ":" + req.getPageSize();

        return redis.get(cacheKey)
                .compose(jsonStr -> {
                    if (jsonStr != null && !jsonStr.isEmpty()) {
                        try {
                            PagedResult<Banner> typedCached = mapper.readValue(jsonStr,
                                    new TypeReference<PagedResult<Banner>>() {
                                    });
                            return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
                        } catch (Exception e) {
                            log.warn("Failed to deserialize cached trashed banners: {}", e.getMessage());
                        }
                    }
                    return repository.getTrashedBanners(req)
                            .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
                            .map(this::mapPaginationDeleteAt);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTrashedBanners", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getTrashedBanners", e.getMessage()));
    }

    @Override
    public Future<BannerResponse> getBannerById(Long id) {
        var ctx = metrics.startSpan("BannerQueryService.getBannerById",
                Attributes.builder().put("banner.id", id).build());
        String key = CACHE_PREFIX + id;

        return redis.getJson(key, Banner.class)
                .compose(cached -> {
                    if (cached != null) {
                        return Future.succeededFuture(BannerResponse.from(cached));
                    }
                    return repository.getBannerById(id)
                            .compose(db -> {
                                if (db == null) {
                                    return Future.<Banner>failedFuture(new NotFoundException("Banner not found"));
                                }
                                return redis.setJson(key, db, CACHE_TTL).<Banner>map(v -> db);
                            })
                            .map(BannerResponse::from);
                })
                .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getBannerById", "Success"))
                .onFailure(e -> metrics.completeSpanError(ctx, "getBannerById", e.getMessage()));
    }
}