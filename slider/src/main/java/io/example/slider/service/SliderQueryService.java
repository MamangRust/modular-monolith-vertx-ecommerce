package io.example.slider.service;

import java.util.List;

import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.slider.model.FindAllSlider;
import io.example.slider.model.SliderResponse;
import io.example.slider.model.SliderResponseDeleteAt;
import io.vertx.core.Future;

public interface SliderQueryService {
    Future<ApiResponsePagination<List<SliderResponse>>> getAllSliders(FindAllSlider req);
    Future<ApiResponsePagination<List<SliderResponseDeleteAt>>> getActiveSliders(FindAllSlider req);
    Future<ApiResponsePagination<List<SliderResponseDeleteAt>>> getTrashedSliders(FindAllSlider req);
    Future<ApiResponse<SliderResponse>> getSliderById(Long sliderId);
}
