package io.example.slider.repository;

import io.example.common.domain.PagedResult;
import io.example.slider.model.FindAllSlider;
import io.example.slider.model.Slider;
import io.vertx.core.Future;

public interface SliderQueryRepository {
    Future<PagedResult<Slider>> getSliders(FindAllSlider req);
    Future<PagedResult<Slider>> getSlidersActive(FindAllSlider req);
    Future<PagedResult<Slider>> getSlidersTrashed(FindAllSlider req);
    Future<Slider> getSliderById(Long sliderId);
}
