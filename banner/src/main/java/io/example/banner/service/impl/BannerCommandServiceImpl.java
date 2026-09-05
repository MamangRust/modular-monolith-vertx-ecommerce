package io.example.banner.service.impl;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.banner.domain.requests.CreateBannerRequest;
import io.example.banner.domain.requests.UpdateBannerRequest;
import io.example.banner.model.BannerResponse;
import io.example.banner.model.BannerResponseDeleteAt;
import io.example.banner.repository.BannerCommandRepository;
import io.example.banner.service.BannerCommandService;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BannerCommandServiceImpl implements BannerCommandService {
    private static final Logger log = LoggerFactory.getLogger(BannerCommandServiceImpl.class);
    private final BannerCommandRepository repo;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "banner:";

    @Override
    public Future<BannerResponse> createBanner(CreateBannerRequest req) {
        var ctx = metrics.startSpan(
                "BannerCommandService.createBanner",
                Attributes.builder()
                        .put("banner.name", Objects.requireNonNull(req.getName()))
                        .build());

        log.info("Creating banner: {}", req.getName());

        return repo.createBanner(req)
                .map(BannerResponse::from)
                .onSuccess(res -> metrics.completeSpanSuccess(ctx, "create", "Banner created successfully"))
                .recover(err -> {
                    log.error("Failed to create banner: {}", req.getName(), err);
                    metrics.completeSpanError(ctx, "create", err.getMessage());
                    return Future.failedFuture(err);
                });
    }

    @Override
    public Future<BannerResponse> updateBanner(UpdateBannerRequest req) {
        Long bannerId = (long) req.getBannerId();
        var ctx = metrics.startSpan(
                "BannerCommandService.updateBanner",
                Attributes.builder()
                        .put("banner.id", bannerId)
                        .put("banner.name", Objects.requireNonNull(req.getName()))
                        .build());

        log.info("Updating banner: {}, name: {}", bannerId, req.getName());

        return repo.updateBanner(req)
                .compose(updatedBanner -> {
                    if (updatedBanner == null) {
                        return Future.failedFuture(new NotFoundException("Banner not found"));
                    }
                    String cacheKey = CACHE_PREFIX + bannerId;
                    return redis.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) {
                                    log.debug("Banner {} cache invalidated", bannerId);
                                }
                            })
                            .onFailure(err -> log.warn("Failed to invalidate cache for banner {}: {}",
                                    bannerId, err.getMessage()))
                            .map(BannerResponse.from(updatedBanner));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(ctx, "update", "Banner updated successfully"))
                .recover(err -> {
                    log.error("Failed to update banner: {}", bannerId, err);
                    metrics.completeSpanError(ctx, "update", err.getMessage());
                    return Future.failedFuture(err);
                });
    }

    @Override
    public Future<BannerResponseDeleteAt> trashBanner(Long bannerId) {
        var ctx = metrics.startSpan(
                "BannerCommandService.trashed",
                Attributes.builder()
                        .put("banner.id", bannerId)
                        .build());

        log.info("Trashing banner: {}", bannerId);

        return repo.trashed(bannerId)
                .compose(banner -> {
                    if (banner == null) {
                        return Future.failedFuture(new NotFoundException("Banner not found with id: " + bannerId));
                    }
                    String cacheKey = CACHE_PREFIX + bannerId;
                    return redis.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) {
                                    log.debug("Banner {} cache invalidated on trash", bannerId);
                                }
                            })
                            .onFailure(err -> log.warn("Failed to invalidate cache for trashed banner {}: {}",
                                    bannerId, err.getMessage()))
                            .map(BannerResponseDeleteAt.from(banner));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(ctx, "trashed", "Banner trashed successfully"))
                .recover(err -> {
                    log.error("Failed to trash banner: {}", bannerId, err);
                    metrics.completeSpanError(ctx, "trashed", err.getMessage());
                    return Future.failedFuture(err);
                });
    }

    @Override
    public Future<BannerResponseDeleteAt> restoreBanner(Long bannerId) {
        var ctx = metrics.startSpan(
                "BannerCommandService.restore",
                Attributes.builder()
                        .put("banner.id", bannerId)
                        .build());

        log.info("Restoring banner: {}", bannerId);

        return repo.restore(bannerId)
                .compose(banner -> {
                    if (banner == null) {
                        return Future.failedFuture(new NotFoundException("Banner not found with id: " + bannerId));
                    }
                    String cacheKey = CACHE_PREFIX + bannerId;
                    return redis.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) {
                                    log.debug("Banner {} cache invalidated on restore", bannerId);
                                }
                            })
                            .onFailure(err -> log.warn("Failed to invalidate cache for restored banner {}: {}",
                                    bannerId, err.getMessage()))
                            .map(BannerResponseDeleteAt.from(banner));
                })
                .onSuccess(res -> metrics.completeSpanSuccess(ctx, "restore", "Banner restored successfully"))
                .recover(err -> {
                    log.error("Failed to restore banner: {}", bannerId, err);
                    metrics.completeSpanError(ctx, "restore", err.getMessage());
                    return Future.failedFuture(err);
                });
    }

    @Override
    public Future<Void> deletePermanent(Long bannerId) {
        var ctx = metrics.startSpan(
                "BannerCommandService.deletePermanent",
                Attributes.builder()
                        .put("banner.id", bannerId)
                        .build());

        log.info("Permanently deleting banner: {}", bannerId);

        return repo.deletePermanent(bannerId)
                .compose(v -> {
                    String cacheKey = CACHE_PREFIX + bannerId;
                    return redis.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0) {
                                    log.debug("Banner {} cache invalidated on permanent delete", bannerId);
                                }
                            })
                            .onFailure(err -> log.warn("Failed to invalidate cache for deleted banner {}: {}",
                                    bannerId, err.getMessage()))
                            .map(v);
                })
                .onSuccess(v -> {
                    log.info("Banner deleted successfully: {}", bannerId);
                    metrics.completeSpanSuccess(ctx, "deletePermanent", "Banner deleted permanently");
                })
                .recover(throwable -> {
                    log.error("Failed to deletePermanent banner: {}", bannerId, throwable);
                    metrics.completeSpanError(ctx, "deletePermanent", throwable.getMessage());
                    return Future.failedFuture(throwable);
                });
    }

    @Override
    public Future<Void> restoreAllBanners() {
        var ctx = metrics.startSpan("BannerCommandService.restoreAll");

        log.info("Attempting to restore all trashed banners");

        return repo.restoreAll()
                .onSuccess(v -> {
                    log.info("All banners restored successfully");
                    metrics.completeSpanSuccess(ctx, "restore_all", "All banners restored");
                })
                .recover(throwable -> {
                    log.error("Failed to restore all banners", throwable);
                    metrics.completeSpanError(ctx, "restore_all", throwable.getMessage());
                    return Future.failedFuture(throwable);
                });
    }

    @Override
    public Future<Void> deleteAllPermanentBanners() {
        var ctx = metrics.startSpan("BannerCommandService.deleteAllPermanent");

        log.info("Attempting to permanently delete all trashed banners");

        return repo.deleteAll()
                .onSuccess(v -> {
                    log.info("All trashed banners permanently deleted");
                    metrics.completeSpanSuccess(ctx, "deleteAllPermanent", "All banners permanently deleted");
                })
                .recover(throwable -> {
                    log.error("Failed to permanently delete all banners", throwable);
                    metrics.completeSpanError(ctx, "deleteAllPermanent", throwable.getMessage());
                    return Future.failedFuture(throwable);
                });
    }
}