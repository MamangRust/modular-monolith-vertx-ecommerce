package io.example.review.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRelationsDetailResponse {
    private Integer id;
    private Integer userId;
    private Integer productId;
    private String name;
    private String comment;
    private Integer rating;
    private List<ReviewDetailResponse> reviewDetail;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    public static ReviewRelationsDetailResponse from(ReviewRelationsDetail response) {
        if (response == null) return null;
        return ReviewRelationsDetailResponse.builder()
                .id(response.getReviewId() != null ? response.getReviewId().intValue() : 0)
                .userId(response.getUserId())
                .productId(response.getProductId() != null ? response.getProductId().intValue() : 0)
                .name(response.getName())
                .comment(response.getComment())
                .rating(response.getRating())
                .reviewDetail(
                        response.getReviewDetails() != null
                                ? ReviewDetailResponse.fromList(response.getReviewDetails())
                                : List.of())
                .createdAt(response.getCreatedAt() != null ? response.getCreatedAt().toString() : null)
                .updatedAt(response.getUpdatedAt() != null ? response.getUpdatedAt().toString() : null)
                .deletedAt(response.getDeletedAt() != null ? response.getDeletedAt().toString() : null)
                .build();
    }
}
