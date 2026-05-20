package io.example.slider.repository;

import io.example.slider.model.CreateSliderRequest;
import io.example.slider.model.Slider;
import io.example.slider.model.UpdateSliderRequest;
import io.vertx.core.Future;

public interface SliderCommandRepository {
    Future<Slider> createSlider(CreateSliderRequest req);
    Future<Slider> updateSlider(UpdateSliderRequest req);
    Future<Slider> trash(Long sliderId);
    Future<Slider> restore(Long sliderId);
    Future<Void> deletePermanent(Long sliderId);
    Future<Integer> restoreAll();
    Future<Integer> deleteAll();
}
