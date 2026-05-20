package io.example.review.handler;

import com.google.protobuf.StringValue;
import io.example.review.model.ReviewResponse;
import io.example.review.model.ReviewResponseDeleteAt;
import io.example.review.model.ReviewDetailResponse;
import io.example.review.model.ReviewRelationsDetailResponse;

public class ProtoConverter {

    public static pb.review.ReviewCommon.ReviewResponse toReviewResponse(ReviewResponse r) {
        if (r == null)
            return pb.review.ReviewCommon.ReviewResponse.getDefaultInstance();
        return pb.review.ReviewCommon.ReviewResponse.newBuilder()
                .setId(r.getId() != null ? r.getId() : 0)
                .setUserId(r.getUserId() != null ? r.getUserId() : 0)
                .setProductId(r.getProductId() != null ? r.getProductId() : 0)
                .setName(r.getName() != null ? r.getName() : "")
                .setComment(r.getComment() != null ? r.getComment() : "")
                .setRating(r.getRating() != null ? r.getRating() : 0)
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
                .build();
    }

    public static pb.review.ReviewCommon.ReviewResponseDeleteAt toReviewDeleteAt(ReviewResponseDeleteAt r) {
        if (r == null)
            return pb.review.ReviewCommon.ReviewResponseDeleteAt.getDefaultInstance();
        pb.review.ReviewCommon.ReviewResponseDeleteAt.Builder b = pb.review.ReviewCommon.ReviewResponseDeleteAt.newBuilder()
                .setId(r.getId() != null ? r.getId() : 0)
                .setUserId(r.getUserId() != null ? r.getUserId() : 0)
                .setProductId(r.getProductId() != null ? r.getProductId() : 0)
                .setName(r.getName() != null ? r.getName() : "")
                .setComment(r.getComment() != null ? r.getComment() : "")
                .setRating(r.getRating() != null ? r.getRating() : 0)
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "");

        if (r.getDeletedAt() != null && !r.getDeletedAt().isEmpty()) {
            b.setDeletedAt(StringValue.of(r.getDeletedAt()));
        }
        return b.build();
    }

    public static pb.review.ReviewCommon.ReviewDetailResponse toReviewDetailResponse(ReviewDetailResponse rd) {
        if (rd == null)
            return pb.review.ReviewCommon.ReviewDetailResponse.getDefaultInstance();
        return pb.review.ReviewCommon.ReviewDetailResponse.newBuilder()
                .setId(rd.getId())
                .setType(rd.getType() != null ? rd.getType() : "")
                .setUrl(rd.getUrl() != null ? rd.getUrl() : "")
                .setCaption(rd.getCaption() != null ? rd.getCaption() : "")
                .setCreatedAt(rd.getCreatedAt() != null ? rd.getCreatedAt() : "")
                .build();
    }

    public static pb.review.ReviewCommon.ReviewsDetailResponse toReviewsDetailResponse(ReviewRelationsDetailResponse r) {
        if (r == null)
            return pb.review.ReviewCommon.ReviewsDetailResponse.getDefaultInstance();
        pb.review.ReviewCommon.ReviewsDetailResponse.Builder b = pb.review.ReviewCommon.ReviewsDetailResponse.newBuilder()
                .setId(r.getId() != null ? r.getId() : 0)
                .setUserId(r.getUserId() != null ? r.getUserId() : 0)
                .setProductId(r.getProductId() != null ? r.getProductId() : 0)
                .setName(r.getName() != null ? r.getName() : "")
                .setComment(r.getComment() != null ? r.getComment() : "")
                .setRating(r.getRating() != null ? r.getRating() : 0)
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
                .setDeletedAt(r.getDeletedAt() != null ? r.getDeletedAt() : "");

        if (r.getReviewDetail() != null && !r.getReviewDetail().isEmpty()) {
            b.setReviewDetail(toReviewDetailResponse(r.getReviewDetail().get(0)));
        }
        return b.build();
    }
}
