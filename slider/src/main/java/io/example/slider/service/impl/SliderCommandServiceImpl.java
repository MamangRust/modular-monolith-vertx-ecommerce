package io.example.slider.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.slider.model.CreateSliderRequest;
import io.example.slider.model.Slider;
import io.example.slider.model.SliderResponse;
import io.example.slider.model.SliderResponseDeleteAt;
import io.example.slider.model.UpdateSliderRequest;
import io.example.slider.repository.SliderCommandRepository;
import io.example.slider.service.SliderCommandService;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;

public class SliderCommandServiceImpl implements SliderCommandService {
    private static final Logger logger = LoggerFactory.getLogger(SliderCommandServiceImpl.class);
    private final SliderCommandRepository repo;
    private final RedisService redisService;
    private final TracingMetrics tracingMetrics;

    public SliderCommandServiceImpl(
            SliderCommandRepository repo,
            RedisService redisService,
            TracingMetrics tracingMetrics) {
        this.repo = repo;
        this.redisService = redisService;
        this.tracingMetrics = tracingMetrics;
    }

    @Override
    public Future<ApiResponse<SliderResponse>> createSlider(CreateSliderRequest req) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "SliderService.createSlider",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("slider.name", req.getName())
                        .build());
        Span span = Span.fromContext(tracingContext.getContext());

        logger.info("Creating slider: {}", req.getName());

        return repo.createSlider(req)
                .map(created -> {
                    span.setAttribute("slider.id", created.getSliderId());
                    tracingMetrics.completeSpanSuccess(tracingContext, "create", "Slider created successfully");
                    return ApiResponse.success("Slider created successfully", SliderResponse.from(created));
                })
                .recover(err -> {
                    logger.error("Failed to create slider", err);
                    tracingMetrics.completeSpanError(tracingContext, "create", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to create slider: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<SliderResponse>> updateSlider(UpdateSliderRequest req) {
        Long sliderId = req.getSliderId();
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "SliderService.updateSlider",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("slider.id", sliderId)
                        .put("slider.name", req.getName())
                        .build());

        logger.info("Updating slider: {}", sliderId);

        return repo.updateSlider(req)
                .compose((Slider slider) -> {
                    if (slider == null) {
                        return Future.failedFuture(new RuntimeException("Slider not found or already deleted"));
                    }
                    String cacheKey = "slider:" + sliderId;
                    return redisService.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0)
                                    logger.debug("Slider {} cache invalidated", sliderId);
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for slider {}: {}", sliderId,
                                    err.getMessage()))
                            .map(slider);
                })
                .map((Slider slider) -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "update", "Slider updated successfully");
                    return ApiResponse.success("Slider updated successfully", SliderResponse.from(slider));
                })
                .recover(err -> {
                    logger.error("Failed to update slider: {}", sliderId, err);
                    tracingMetrics.completeSpanError(tracingContext, "update", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to update slider: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<SliderResponseDeleteAt>> trashSlider(Long sliderId) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "SliderService.trashSlider",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("slider.id", sliderId)
                        .build());

        logger.info("Trashing slider: {}", sliderId);

        return repo.trash(sliderId)
                .compose(slider -> {
                    if (slider == null) {
                        return Future.failedFuture(new RuntimeException("Slider not found or already trashed"));
                    }
                    String cacheKey = "slider:" + sliderId;
                    return redisService.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0)
                                    logger.debug("Slider {} cache invalidated on trash", sliderId);
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for trashed slider {}: {}",
                                    sliderId, err.getMessage()))
                            .map(slider);
                })
                .map(slider -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "trashed", "Slider trashed successfully");
                    return ApiResponse.success("Slider trashed successfully", SliderResponseDeleteAt.from(slider));
                })
                .recover(err -> {
                    logger.error("Failed to trash slider: {}", sliderId, err);
                    tracingMetrics.completeSpanError(tracingContext, "trashed", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to trash slider: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<SliderResponseDeleteAt>> restoreSlider(Long sliderId) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "SliderService.restoreSlider",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("slider.id", sliderId)
                        .build());

        logger.info("Restoring slider: {}", sliderId);

        return repo.restore(sliderId)
                .compose(slider -> {
                    if (slider == null) {
                        return Future.failedFuture(new RuntimeException("Slider not found or not trashed"));
                    }
                    String cacheKey = "slider:" + sliderId;
                    return redisService.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0)
                                    logger.debug("Slider {} cache invalidated on restore", sliderId);
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for restored slider {}: {}",
                                    sliderId, err.getMessage()))
                            .map(slider);
                })
                .map(slider -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "restore", "Slider restored successfully");
                    return ApiResponse.success("Slider restored successfully", SliderResponseDeleteAt.from(slider));
                })
                .recover(err -> {
                    logger.error("Failed to restore slider: {}", sliderId, err);
                    tracingMetrics.completeSpanError(tracingContext, "restore", err.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.error("Failed to restore slider: " + err.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deleteSliderPermanently(Long sliderId) {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan(
                "SliderService.deleteSliderPermanently",
                io.opentelemetry.api.common.Attributes.builder()
                        .put("slider.id", sliderId)
                        .build());

        logger.info("Permanently deleting slider: {}", sliderId);

        return repo.deletePermanent(sliderId)
                .compose(v -> {
                    String cacheKey = "slider:" + sliderId;
                    return redisService.delete(cacheKey)
                            .onSuccess(deleted -> {
                                if (deleted > 0)
                                    logger.debug("Slider {} cache invalidated on permanent delete", sliderId);
                            })
                            .onFailure(err -> logger.warn("Failed to invalidate cache for deleted slider {}: {}",
                                    sliderId, err.getMessage()))
                            .map(v);
                })
                .map(v -> {
                    logger.info("Slider deleted successfully: {}", sliderId);
                    tracingMetrics.completeSpanSuccess(tracingContext, "deletePermanent", "Slider deleted permanently");
                    return ApiResponse.<Void>success("success", null);
                })
                .recover(throwable -> {
                    logger.error("Failed to deletePermanent slider: {}", sliderId, throwable);
                    tracingMetrics.completeSpanError(tracingContext, "deletePermanent", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to delete slider: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> restoreAllSliders() {
        TracingMetrics.TracingContext tracingContext = tracingMetrics.startSpan("SliderService.restoreAllSliders");

        logger.info("Restoring all trashed sliders");

        return repo.restoreAll()
                .map(count -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "restore_all",
                            "All sliders restored successfully");
                    return ApiResponse.<Void>success("All sliders restored successfully", null);
                })
                .recover(throwable -> {
                    logger.error("Failed to restore all sliders", throwable);
                    tracingMetrics.completeSpanError(tracingContext, "restore_all", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to restore all sliders: " + throwable.getMessage()));
                });
    }

    @Override
    public Future<ApiResponse<Void>> deleteAllPermanentSliders() {
        TracingMetrics.TracingContext tracingContext = tracingMetrics
                .startSpan("SliderService.deleteAllPermanentSliders");

        logger.info("Permanently deleting all trashed sliders");

        return repo.deleteAll()
                .map(count -> {
                    tracingMetrics.completeSpanSuccess(tracingContext, "delete_all_permanent",
                            "All trashed sliders deleted permanently");
                    return ApiResponse.<Void>success("All trashed sliders deleted permanently", null);
                })
                .recover(throwable -> {
                    logger.error("Failed to delete all permanent sliders", throwable);
                    tracingMetrics.completeSpanError(tracingContext, "delete_all_permanent", throwable.getMessage());
                    return Future.succeededFuture(
                            ApiResponse.<Void>error("Failed to delete all sliders: " + throwable.getMessage()));
                });
    }
}
