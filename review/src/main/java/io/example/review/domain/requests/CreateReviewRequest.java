package io.example.review.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewRequest {
    private int userId;
    private Long productId;
    private String name;
    private int rating;
    private String comment;
}
