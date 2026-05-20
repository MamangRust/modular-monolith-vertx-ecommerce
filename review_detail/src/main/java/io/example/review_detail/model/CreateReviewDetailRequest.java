package io.example.review_detail.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewDetailRequest {
    private Long reviewId;
    private String type;
    private String file; 
    private String caption;
}
