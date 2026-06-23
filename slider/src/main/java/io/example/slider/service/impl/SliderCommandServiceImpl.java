package io.example.slider.service.impl;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.slider.domain.requests.CreateSliderRequest;
import io.example.slider.domain.requests.UpdateSliderRequest;
import io.example.slider.model.SliderResponse;
import io.example.slider.model.SliderResponseDeleteAt;
import io.example.slider.repository.SliderCommandRepository;
import io.example.slider.repository.SliderQueryRepository;
import io.example.slider.service.SliderCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SliderCommandServiceImpl implements SliderCommandService {
    private final SliderCommandRepository repo;
    private final SliderQueryRepository queryRepository;
    private final RedisService redis;
    private final TracingMetrics metrics;

    private static final String CACHE_PREFIX = "slider:";

    private Future<Void> evict(Long id) {
        return redis.delete(CACHE_PREFIX + id).<Void>mapEmpty();
    }

    private Future<Void> evictAll() {
        return redis.deleteByPattern("sliders:*").<Void>mapEmpty();
    }

    @Override
    public Future<SliderResponse> createSlider(CreateSliderRequest req) {
        var ctx = metrics.startSpan("SliderCommandService.createSlider",
                Attributes.builder().put("slider.name", req.getName()).build());

        return repo.createSlider(req)
                .map(SliderResponse::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "create", "Slider created successfully"))
                .onFailure(e -> metrics.completeSpanError(ctx, "create", e.getMessage()));
    }

    @Override
    public Future<SliderResponse> updateSlider(UpdateSliderRequest req) {
        Long sliderId = req.getSliderId();
        var ctx = metrics.startSpan("SliderCommandService.updateSlider",
                Attributes.builder().put("slider.id", sliderId).build());

        return repo.updateSlider(req)
                .compose(slider -> {
                    if (slider == null) {
                        return Future.failedFuture(new NotFoundException("Slider not found"));
                    }
                    return evict(sliderId).map(v -> slider);
                })
                .map(SliderResponse::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "update", "Slider updated successfully"))
                .onFailure(e -> metrics.completeSpanError(ctx, "update", e.getMessage()));
    }

    @Override
    public Future<SliderResponseDeleteAt> trashSlider(Long sliderId) {
        var ctx = metrics.startSpan("SliderCommandService.trashSlider",
                Attributes.builder().put("slider.id", sliderId).build());

        return repo.trash(sliderId)
                .compose(slider -> {
                    if (slider == null) {
                        return Future.failedFuture(new NotFoundException("Slider not found"));
                    }
                    return evict(sliderId.longValue()).map(v -> slider);
                })
                .map(SliderResponseDeleteAt::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "trash", "Slider trashed successfully"))
                .onFailure(e -> metrics.completeSpanError(ctx, "trash", e.getMessage()));
    }

    @Override
    public Future<SliderResponseDeleteAt> restoreSlider(Long sliderId) {
        var ctx = metrics.startSpan("SliderCommandService.restoreSlider",
                Attributes.builder().put("slider.id", sliderId).build());

        return queryRepository.findByTrashedId(sliderId)
                .compose(trashed -> {
                    if (trashed == null) {
                        return Future.failedFuture(new NotFoundException("Slider not found or not in trashed state"));
                    }
                    return repo.restore(sliderId)
                            .compose(slider -> {
                                if (slider == null) {
                                    return Future.failedFuture(new NotFoundException("Slider not found"));
                                }
                                return evict(sliderId.longValue()).map(v -> slider);
                            });
                })
                .map(SliderResponseDeleteAt::from)
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore", "Slider restored successfully"))
                .onFailure(e -> metrics.completeSpanError(ctx, "restore", e.getMessage()));
    }

    @Override
    public Future<Void> deleteSliderPermanently(Long sliderId) {
        var ctx = metrics.startSpan("SliderCommandService.deletePermanent",
                Attributes.builder().put("slider.id", sliderId).build());

        return queryRepository.findByTrashedId(sliderId)
                .compose(trashed -> {
                    if (trashed == null) {
                        return Future.<Void>failedFuture(
                                new BadRequestException(
                                        "Slider not found or must be trashed before permanent deletion"));
                    }
                    return repo.deletePermanent(sliderId)
                            .compose(v -> evictAll());
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deletePermanent", "Slider deleted permanently"))
                .onFailure(e -> metrics.completeSpanError(ctx, "deletePermanent", e.getMessage()));
    }

    @Override
    public Future<Void> restoreAllSliders() {
        var ctx = metrics.startSpan("SliderCommandService.restoreAll");

        return repo.restoreAll()
                .compose(count -> {
                    if (count == 0) {
                        return Future.<Void>failedFuture(new NotFoundException("No trashed sliders found"));
                    }
                    return evictAll();
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore_all", "All sliders restored"))
                .onFailure(e -> metrics.completeSpanError(ctx, "restore_all", e.getMessage()));
    }

    @Override
    public Future<Void> deleteAllPermanentSliders() {
        var ctx = metrics.startSpan("SliderCommandService.deleteAllPermanent");

        return repo.deleteAll()
                .compose(count -> {
                    if (count == 0) {
                        return Future.<Void>failedFuture(new NotFoundException("No trashed sliders found"));
                    }
                    return evictAll();
                })
                .onSuccess(v -> metrics.completeSpanSuccess(ctx, "delete_all_permanent",
                        "All sliders deleted permanently"))
                .onFailure(e -> metrics.completeSpanError(ctx, "delete_all_permanent", e.getMessage()));
    }
}