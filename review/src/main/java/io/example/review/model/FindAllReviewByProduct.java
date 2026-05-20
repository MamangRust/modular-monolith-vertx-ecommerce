package io.example.review.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindAllReviewByProduct {
    private Long productId;
    private Integer rating;
    private String search;
    private Integer page;
    private Integer pageSize;
}
