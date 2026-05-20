package io.example.category.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDeleteAt {
    private Long id;
    private String name;
    private String description;
    private String slugCategory;
    private String imageCategory;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    public static CategoryResponseDeleteAt from(Category category) {
        if (category == null) return null;
        return CategoryResponseDeleteAt.builder()
                .id(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .slugCategory(category.getSlugCategory())
                .imageCategory(category.getImageCategory())
                .createdAt(category.getCreatedAt() != null ? category.getCreatedAt().toString() : null)
                .updatedAt(category.getUpdatedAt() != null ? category.getUpdatedAt().toString() : null)
                .deletedAt(category.getDeletedAt() != null ? category.getDeletedAt().toString() : null)
                .build();
    }
}
