package io.example.banner.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.domain.PagedResult;
import io.example.common.exception.NotFoundException;
import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.common.model.PaginationMeta;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.banner.model.Banner;
import io.example.banner.model.BannerResponse;
import io.example.banner.model.BannerResponseDeleteAt;
import io.example.banner.repository.BannerQueryRepository;
import io.example.banner.service.BannerQueryService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import pb.banner.BannerQuery.FindAllBannerRequest;

public class BannerQueryServiceImpl implements BannerQueryService {
    private static final Logger logger = LoggerFactory.getLogger(BannerQueryServiceImpl.class);

    private final BannerQueryRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "banner:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    public BannerQueryServiceImpl(
            BannerQueryRepository repo,
            RedisService redis,
            TracingMetrics metrics) {
        this.repo = repo;
        this.redis = redis;
        this.metrics = metrics;
    }

    @Override
    public Future<ApiResponsePagination<List<BannerResponse>>> getAllBanners(FindAllBannerRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("BannerQueryService.getAllBanners");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        String cacheKey = String.format("%sall:p:%d:s:%d:k:%s", CACHE_PREFIX, page, pageSize, keyword);

        return redis.getJson(cacheKey, ApiResponsePagination.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("banner.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_all", "Banners fetched from cache");
                        @SuppressWarnings("unchecked")
                        ApiResponsePagination<List<BannerResponse>> typedCached = (ApiResponsePagination<List<BannerResponse>>) cached;
                        return Future.succeededFuture(typedCached);
                    }
                    span.setAttribute("banner.cache_hit", false);
                    return repo.getBanners(keyword, page, pageSize)
                            .map(result -> mapBannerPagination(result, page, pageSize))
                            .compose(response -> redis.setJson(cacheKey, response, CACHE_TTL).map(response));
                })
                .onSuccess(response -> {
                    @SuppressWarnings("unchecked")
                    ApiResponsePagination<List<BannerResponse>> typedResponse = (ApiResponsePagination<List<BannerResponse>>) response;
                    span.setAttribute("banners.count", (long) typedResponse.data().size());
                    span.setAttribute("banners.total_records", (long) typedResponse.pagination().totalRecords());
                    metrics.completeSpanSuccess(tracingContext, "get_all", "Banners fetched successfully");
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch banners", throwable);
                    metrics.completeSpanError(tracingContext, "get_all", throwable.getMessage());

                    return Future.succeededFuture(
                            ApiResponsePagination.<List<BannerResponse>>error("Failed to fetch banners: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<BannerResponseDeleteAt>>> getActiveBanners(FindAllBannerRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("BannerQueryService.getActiveBanners");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        String cacheKey = String.format("%sactive:p:%d:s:%d:k:%s", CACHE_PREFIX, page, pageSize, keyword);

        return redis.getJson(cacheKey, ApiResponsePagination.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("banner.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_active", "Active banners fetched from cache");
                        @SuppressWarnings("unchecked")
                        ApiResponsePagination<List<BannerResponseDeleteAt>> typedCached = (ApiResponsePagination<List<BannerResponseDeleteAt>>) cached;
                        return Future.succeededFuture(typedCached);
                    }
                    span.setAttribute("banner.cache_hit", false);
                    return repo.getActiveBanners(keyword, page, pageSize)
                            .map(result -> mapBannerPaginationDeleteAt(result, page, pageSize))
                            .compose(response -> redis.setJson(cacheKey, response, CACHE_TTL).map(response));
                })
                .onSuccess(response -> {
                    @SuppressWarnings("unchecked")
                    ApiResponsePagination<List<BannerResponseDeleteAt>> typedResponse = (ApiResponsePagination<List<BannerResponseDeleteAt>>) response;
                    span.setAttribute("banners.count", (long) typedResponse.data().size());
                    span.setAttribute("banners.total_records", (long) typedResponse.pagination().totalRecords());
                    metrics.completeSpanSuccess(tracingContext, "get_active", "Active banners fetched successfully");
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch active banners", throwable);
                    metrics.completeSpanError(tracingContext, "get_active", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.<List<BannerResponseDeleteAt>>error(
                                    "Failed to fetch active banners: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponsePagination<List<BannerResponseDeleteAt>>> getTrashedBanners(FindAllBannerRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("BannerQueryService.getTrashedBanners");
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
        String keyword = (req.getSearch() != null && !req.getSearch().isEmpty()) ? req.getSearch() : "";

        String cacheKey = String.format("%strashed:p:%d:s:%d:k:%s", CACHE_PREFIX, page, pageSize, keyword);

        return redis.getJson(cacheKey, ApiResponsePagination.class)
                .compose(cached -> {
                    if (cached != null) {
                        span.setAttribute("banner.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_trashed", "Trashed banners fetched from cache");
                        @SuppressWarnings("unchecked")
                        ApiResponsePagination<List<BannerResponseDeleteAt>> typedCached = (ApiResponsePagination<List<BannerResponseDeleteAt>>) cached;
                        return Future.succeededFuture(typedCached);
                    }
                    span.setAttribute("banner.cache_hit", false);
                    return repo.getTrashedBanners(keyword, page, pageSize)
                            .map(result -> mapBannerPaginationDeleteAt(result, page, pageSize))
                            .compose(response -> redis.setJson(cacheKey, response, CACHE_TTL).map(response));
                })
                .onSuccess(response -> {
                    @SuppressWarnings("unchecked")
                    ApiResponsePagination<List<BannerResponseDeleteAt>> typedResponse = (ApiResponsePagination<List<BannerResponseDeleteAt>>) response;
                    span.setAttribute("banners.count", (long) typedResponse.data().size());
                    span.setAttribute("banners.total_records", (long) typedResponse.pagination().totalRecords());
                    metrics.completeSpanSuccess(tracingContext, "get_trashed", "Trashed banners fetched successfully");
                })
                .recover(throwable -> {
                    logger.error("Failed to fetch trashed banners", throwable);
                    metrics.completeSpanError(tracingContext, "get_trashed", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponsePagination.<List<BannerResponseDeleteAt>>error(
                                    "Failed to fetch trashed banners: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<BannerResponse>> getBannerById(Long bannerId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "BannerQueryService.getBannerById",
                Attributes.builder()
                        .put("banner.id", bannerId)
                        .build());
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        logger.info("Fetching banner by id: {}", bannerId);
        String cacheKey = CACHE_PREFIX + "id:" + bannerId;

        return redis.getJson(cacheKey, Banner.class)
                .compose(cachedBanner -> {
                    if (cachedBanner != null) {
                        logger.info("Banner {} found in cache", bannerId);
                        span.setAttribute("banner.cache_hit", true);
                        metrics.completeSpanSuccess(tracingContext, "get_by_id", "Banner fetched from cache");
                        return Future.succeededFuture(ApiResponse.success(
                                "Banner fetched successfully (from cache)",
                                BannerResponse.from(cachedBanner)));
                    } else {
                        span.setAttribute("banner.cache_hit", false);
                        return repo.getBannerById(bannerId)
                                .compose(banner -> {
                                    if (banner == null) {
                                        return Future.failedFuture(new NotFoundException("Banner not found"));
                                    }
                                    return redis.setJson(cacheKey, banner, CACHE_TTL).map(banner);
                                })
                                .map(banner -> {
                                    metrics.completeSpanSuccess(tracingContext, "get_by_id", "Banner fetched from database");
                                    return ApiResponse.success("Banner fetched successfully", BannerResponse.from(banner));
                                });
                    }
                })
                .recover(err -> {
                    logger.error("Failed to fetch banner by id: {}", bannerId, err);
                    metrics.completeSpanError(tracingContext, "get_by_id", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<BannerResponse>error(
                                    "Failed to fetch banner: " + err.getMessage()));
                });
    }

    private ApiResponsePagination<List<BannerResponse>> mapBannerPagination(
            PagedResult<Banner> result,
            int page,
            int pageSize) {

        int totalRecords = result.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<BannerResponse> data = result.getData()
                .stream()
                .map(BannerResponse::from)
                .toList();

        return new ApiResponsePagination<>(
                "success",
                "Banners found",
                data,
                new PaginationMeta(
                        page,
                        pageSize,
                        totalPages,
                        totalRecords));
    }

    private ApiResponsePagination<List<BannerResponseDeleteAt>> mapBannerPaginationDeleteAt(
            PagedResult<Banner> result,
            int page,
            int pageSize) {

        int totalRecords = result.getTotalRecords();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        List<BannerResponseDeleteAt> data = result.getData()
                .stream()
                .map(BannerResponseDeleteAt::from)
                .toList();

        return new ApiResponsePagination<>(
                "success",
                "Banners found",
                data,
                new PaginationMeta(
                        page,
                        pageSize,
                        totalPages,
                        totalRecords));
    }
}
