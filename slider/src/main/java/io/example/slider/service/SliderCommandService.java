package io.example.slider.service;

import io.example.common.model.ApiResponse;
import io.example.slider.model.CreateSliderRequest;
import io.example.slider.model.SliderResponse;
import io.example.slider.model.SliderResponseDeleteAt;
import io.example.slider.model.UpdateSliderRequest;
import io.vertx.core.Future;

public interface SliderCommandService {
    Future<ApiResponse<SliderResponse>> createSlider(CreateSliderRequest req);
    Future<ApiResponse<SliderResponse>> updateSlider(UpdateSliderRequest req);
    Future<ApiResponse<SliderResponseDeleteAt>> trashSlider(Long sliderId);
    Future<ApiResponse<SliderResponseDeleteAt>> restoreSlider(Long sliderId);
    Future<ApiResponse<Void>> deleteSliderPermanently(Long sliderId);
    Future<ApiResponse<Void>> restoreAllSliders();
    Future<ApiResponse<Void>> deleteAllPermanentSliders();
}
