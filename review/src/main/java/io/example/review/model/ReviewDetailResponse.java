package io.example.review.model;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailResponse {
    private int id;
    private int reviewId;
    private String type;
    private String url;
    private String caption;
    private String createdAt;
    private String updatedAt;

    public static ReviewDetailResponse from(ReviewDetail reviewDetail) {
        if (reviewDetail == null) return null;
        return ReviewDetailResponse.builder()
                .id(reviewDetail.getReviewDetailId() != null ? reviewDetail.getReviewDetailId().intValue() : 0)
                .reviewId(reviewDetail.getReviewId() != null ? reviewDetail.getReviewId() : 0)
                .type(reviewDetail.getType())
                .url(reviewDetail.getUrl())
                .caption(reviewDetail.getCaption())
                .createdAt(reviewDetail.getCreatedAt() != null ? reviewDetail.getCreatedAt().toString() : null)
                .updatedAt(reviewDetail.getUpdatedAt() != null ? reviewDetail.getUpdatedAt().toString() : null)
                .build();
    }

    public static List<ReviewDetailResponse> fromList(List<ReviewDetail> reviewDetails) {
        if (reviewDetails == null) return List.of();
        return reviewDetails.stream()
                .map(ReviewDetailResponse::from)
                .collect(Collectors.toList());
    }
}
