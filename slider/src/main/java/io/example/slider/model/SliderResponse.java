package io.example.slider.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SliderResponse {
    private Long id;
    private String name;
    private String image;
    private String createdAt;
    private String updatedAt;

    public static SliderResponse from(Slider s) {
        if (s == null) return null;
        return SliderResponse.builder()
                .id(s.getSliderId())
                .name(s.getName())
                .image(s.getImage())
                .createdAt(s.getCreatedAt() != null ? s.getCreatedAt().toString() : null)
                .updatedAt(s.getUpdatedAt() != null ? s.getUpdatedAt().toString() : null)
                .build();
    }
}
