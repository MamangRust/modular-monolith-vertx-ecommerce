package io.example.slider.service;

import io.example.slider.domain.requests.CreateSliderRequest;
import io.example.slider.domain.requests.UpdateSliderRequest;
import io.example.slider.model.SliderResponse;
import io.example.slider.model.SliderResponseDeleteAt;
import io.vertx.core.Future;

public interface SliderCommandService {
    Future<SliderResponse> createSlider(CreateSliderRequest req);

    Future<SliderResponse> updateSlider(UpdateSliderRequest req);

    Future<SliderResponseDeleteAt> trashSlider(Long sliderId);

    Future<SliderResponseDeleteAt> restoreSlider(Long sliderId);

    Future<Void> deleteSliderPermanently(Long sliderId);

    Future<Void> restoreAllSliders();

    Future<Void> deleteAllPermanentSliders();
}