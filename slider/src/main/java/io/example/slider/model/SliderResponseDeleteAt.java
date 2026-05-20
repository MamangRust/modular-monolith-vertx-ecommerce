package io.example.slider.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SliderResponseDeleteAt {
    private Long id;
    private String name;
    private String image;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    public static SliderResponseDeleteAt from(Slider s) {
        if (s == null) return null;
        return SliderResponseDeleteAt.builder()
                .id(s.getSliderId())
                .name(s.getName())
                .image(s.getImage())
                .createdAt(s.getCreatedAt() != null ? s.getCreatedAt().toString() : null)
                .updatedAt(s.getUpdatedAt() != null ? s.getUpdatedAt().toString() : null)
                .deletedAt(s.getDeletedAt() != null ? s.getDeletedAt().toString() : null)
                .build();
    }
}
