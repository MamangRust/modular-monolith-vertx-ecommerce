package io.example.category.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String slugCategory;
    private String imageCategory;
    private String createdAt;
    private String updatedAt;

    public static CategoryResponse from(Category category) {
        if (category == null) return null;
        return CategoryResponse.builder()
                .id(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .slugCategory(category.getSlugCategory())
                .imageCategory(category.getImageCategory())
                .createdAt(category.getCreatedAt() != null ? category.getCreatedAt().toString() : null)
                .updatedAt(category.getUpdatedAt() != null ? category.getUpdatedAt().toString() : null)
                .build();
    }
}
