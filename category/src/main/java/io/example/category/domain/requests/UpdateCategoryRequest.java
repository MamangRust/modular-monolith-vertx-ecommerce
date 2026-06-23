package io.example.category.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCategoryRequest {
    private Long id;
    private String name;
    private String description;
    private String slugCategory;
    private String imageCategory;
}
