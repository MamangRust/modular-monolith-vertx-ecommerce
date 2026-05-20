package io.example.review.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDeleteAt {
    private Integer id;
    private Integer userId;
    private Integer productId;
    private String name;
    private String comment;
    private Integer rating;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    public static ReviewResponseDeleteAt from(Review review) {
        if (review == null) return null;
        return ReviewResponseDeleteAt.builder()
                .id(review.getReviewId() != null ? review.getReviewId().intValue() : 0)
                .userId(review.getUserId())
                .productId(review.getProductId())
                .name(review.getName())
                .comment(review.getComment())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().toString() : null)
                .updatedAt(review.getUpdatedAt() != null ? review.getUpdatedAt().toString() : null)
                .deletedAt(review.getDeletedAt() != null ? review.getDeletedAt().toString() : null)
                .build();
    }
}
