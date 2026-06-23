package io.example.banner.service.impl;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.banner.domain.requests.CreateBannerRequest;
import io.example.banner.domain.requests.UpdateBannerRequest;
import io.example.banner.model.Banner;
import io.example.banner.model.BannerResponse;
import io.example.banner.model.BannerResponseDeleteAt;
import io.example.banner.repository.BannerCommandRepository;
import io.example.banner.service.BannerCommandService;
import io.example.common.domain.ApiResponse;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BannerCommandServiceImpl implements BannerCommandService {
    private static final Logger logger = LoggerFactory.getLogger(BannerCommandServiceImpl.class);
    private final BannerCommandRepository repo;

    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "banner:";

    @Override
    public Future<ApiResponse<BannerResponse>> createBanner(CreateBannerRequest req) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "BannerCommandService.createBanner",
                Attributes.builder()
                        .put("banner.name", Objects.requireNonNull(req.getName()))
                        .build());
        Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

        logger.info("Creating banner: {}", req.getName());

        return repo.createBanner(req)
                .map(created -> {
                    span.setAttribute("banner.id", created.getBannerId());
                    metrics.completeSpanSuccess(tracingContext, "create", "Banner created successfully");
                    return ApiResponse.success(
                            "Banner created successfully",
                            BannerResponse.from(created));
                })
                .recover(err -> {
                    logger.error("Failed to create banner: {}", req.getName(), err);
                    metrics.completeSpanError(tracingContext, "create", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<BannerResponse>error("Failed to create banner: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<BannerResponse>> updateBanner(UpdateBannerRequest req) {
        Long bannerId = (long) req.getBannerId();
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "BannerCommandService.updateBanner",
                Attributes.builder()
                        .put("banner.id", bannerId)
                        .put("banner.name", Objects.requireNonNull(req.getName()))
                        .build());

        logger.info("Updating banner: {}, name: {}", bannerId, req.getName());

        return repo.updateBanner(req)
                .compose((Banner updatedBanner) -> {
                    if (updatedBanner == null) {
                        return Future.failedFuture(new NotFoundException("Banner not found"));
                    }
                    String cacheKey = CACHE_PREFIX + "id:" + bannerId;
                    return redis.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) {
                                    logger.debug("Banner {} cache invalidated", bannerId);
                                }
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for banner {}: {}", bannerId,
                                    err.getMessage()))
                            .map(updatedBanner);
                })
                .map((Banner updatedBanner) -> {
                    metrics.completeSpanSuccess(tracingContext, "update", "Banner updated successfully");
                    return ApiResponse.success(
                            "Banner updated successfully",
                            BannerResponse.from(updatedBanner));
                })
                .recover(err -> {
                    logger.error("Failed to update banner: {}", bannerId, err);
                    metrics.completeSpanError(tracingContext, "update", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<BannerResponse>error("Failed to update banner: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<BannerResponseDeleteAt>> trashBanner(Long bannerId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "BannerCommandService.trashed",
                Attributes.builder()
                        .put("banner.id", bannerId)
                        .build());

        logger.info("Trashing banner: {}", bannerId);

        return repo.trashed(bannerId)
                .compose(banner -> {
                    if (banner == null) {
                        return Future.failedFuture(new NotFoundException("Banner not found with id: " + bannerId));
                    }
                    String cacheKey = CACHE_PREFIX + "id:" + bannerId;
                    return redis.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) {
                                    logger.debug("Banner {} cache invalidated on trash", bannerId);
                                }
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for trashed banner {}: {}",
                                    bannerId, err.getMessage()))
                            .map(banner);
                })
                .map(banner -> {
                    metrics.completeSpanSuccess(tracingContext, "trashed", "Banner trashed successfully");
                    return ApiResponse.success("Banner trashed successfully", BannerResponseDeleteAt.from(banner));
                })
                .recover(err -> {
                    logger.error("Failed to trash banner: {}", bannerId, err);
                    metrics.completeSpanError(tracingContext, "trashed", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<BannerResponseDeleteAt>error("Failed to trash banner: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<BannerResponseDeleteAt>> restoreBanner(Long bannerId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "BannerCommandService.restore",
                Attributes.builder()
                        .put("banner.id", bannerId)
                        .build());

        logger.info("Restoring banner: {}", bannerId);

        return repo.restore(bannerId)
                .compose(banner -> {
                    if (banner == null) {
                        return Future.failedFuture(new NotFoundException("Banner not found with id: " + bannerId));
                    }
                    String cacheKey = CACHE_PREFIX + "id:" + bannerId;
                    return redis.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) {
                                    logger.debug("Banner {} cache invalidated on restore", bannerId);
                                }
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for restored banner {}: {}",
                                    bannerId, err.getMessage()))
                            .map(banner);
                })
                .map(banner -> {
                    metrics.completeSpanSuccess(tracingContext, "restore", "Banner restored successfully");
                    return ApiResponse.success(
                            "Banner restored successfully",
                            BannerResponseDeleteAt.from(banner));
                })
                .recover(err -> {
                    logger.error("Failed to restore banner: {}", bannerId, err);
                    metrics.completeSpanError(tracingContext, "restore", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<BannerResponseDeleteAt>error("Failed to restore banner: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deletePermanent(Long bannerId) {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan(
                "BannerCommandService.deletePermanent",
                Attributes.builder()
                        .put("banner.id", bannerId)
                        .build());

        logger.info("Permanently deleting banner: {}", bannerId);

        return repo.deletePermanent(bannerId)
                .compose(v -> {
                    String cacheKey = CACHE_PREFIX + "id:" + bannerId;
                    return redis.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) {
                                    logger.debug("Banner {} cache invalidated on permanent delete", bannerId);
                                }
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for deleted banner {}: {}",
                                    bannerId, err.getMessage()))
                            .map(v);
                })
                .map(v -> {
                    logger.info("Banner deleted successfully: {}", bannerId);
                    metrics.completeSpanSuccess(tracingContext, "deletePermanent", "Banner deleted permanently");
                    return ApiResponse.<Void>success("success", null);
                })
                .recover(throwable -> {
                    logger.error("Failed to deletePermanent banner: {}", bannerId, throwable);
                    metrics.completeSpanError(tracingContext, "deletePermanent", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to delete banner: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> restoreAllBanners() {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("BannerService.restoreAll");

        logger.info("Attempting to restore all trashed banners");

        return repo.restoreAll()
                .compose(v -> {
                    logger.info("All banners restored successfully");
                    metrics.completeSpanSuccess(tracingContext, "restore_all", "All banners restored");
                    return Future.succeededFuture(ApiResponse.<Void>success("All banners restored successfully"));
                })
                .recover(throwable -> {
                    logger.error("Failed to restore all banners", throwable);
                    metrics.completeSpanError(tracingContext, "restore_all", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to restore all banners: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deleteAllPermanentBanners() {
        TracingMetrics.TracingContext tracingContext = metrics.startSpan("BannerService.deleteAllPermanent");

        logger.info("Attempting to permanently delete all trashed banners");

        return repo.deleteAll()
                .compose(v -> {
                    logger.info("All trashed banners permanently deleted");
                    metrics.completeSpanSuccess(tracingContext, "deleteAllPermanent",
                            "All banners permanently deleted");
                    return Future.succeededFuture(ApiResponse.<Void>success("All banners permanently deleted"));
                })
                .recover(throwable -> {
                    logger.error("Failed to permanently delete all banners", throwable);
                    metrics.completeSpanError(tracingContext, "deleteAllPermanent", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error(
                                    "Failed to permanently delete all banners: " + throwable.getMessage()));
                });
    }
}
