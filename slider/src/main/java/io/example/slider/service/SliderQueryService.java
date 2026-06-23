package io.example.slider.service;

import io.example.common.domain.PagedResult;
import io.example.slider.domain.requests.FindAllSlider;
import io.example.slider.model.SliderResponse;
import io.example.slider.model.SliderResponseDeleteAt;
import io.vertx.core.Future;

public interface SliderQueryService {
    Future<PagedResult<SliderResponse>> getAllSliders(FindAllSlider req);

    Future<PagedResult<SliderResponseDeleteAt>> getActiveSliders(FindAllSlider req);

    Future<PagedResult<SliderResponseDeleteAt>> getTrashedSliders(FindAllSlider req);

    Future<SliderResponse> getSliderById(Long sliderId);
}