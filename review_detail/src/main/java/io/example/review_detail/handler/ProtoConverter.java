package io.example.review_detail.handler;
 
import com.google.protobuf.StringValue;
import io.example.review_detail.model.ReviewDetailResponse;
import io.example.review_detail.model.ReviewDetailResponseDeleteAt;
 
public class ProtoConverter {
 
    public static pb.review_detail.ReviewDetailCommon.ReviewDetailsResponse toProtoResponse(ReviewDetailResponse rd) {
        if (rd == null) {
            return pb.review_detail.ReviewDetailCommon.ReviewDetailsResponse.getDefaultInstance();
        }
        return pb.review_detail.ReviewDetailCommon.ReviewDetailsResponse.newBuilder()
                .setId(rd.getId())
                .setReviewId(rd.getReviewId())
                .setType(rd.getType() != null ? rd.getType() : "")
                .setUrl(rd.getUrl() != null ? rd.getUrl() : "")
                .setCaption(rd.getCaption() != null ? rd.getCaption() : "")
                .setCreatedAt(rd.getCreatedAt() != null ? rd.getCreatedAt() : "")
                .setUpdatedAt(rd.getUpdatedAt() != null ? rd.getUpdatedAt() : "")
                .build();
    }
 
    public static pb.review_detail.ReviewDetailCommon.ReviewDetailsResponseDeleteAt toProtoResponseDeleteAt(ReviewDetailResponseDeleteAt rd) {
        if (rd == null) {
            return pb.review_detail.ReviewDetailCommon.ReviewDetailsResponseDeleteAt.getDefaultInstance();
        }
        pb.review_detail.ReviewDetailCommon.ReviewDetailsResponseDeleteAt.Builder b = pb.review_detail.ReviewDetailCommon.ReviewDetailsResponseDeleteAt.newBuilder()
                .setId(rd.getId())
                .setReviewId(rd.getReviewId())
                .setType(rd.getType() != null ? rd.getType() : "")
                .setUrl(rd.getUrl() != null ? rd.getUrl() : "")
                .setCaption(rd.getCaption() != null ? rd.getCaption() : "")
                .setCreatedAt(rd.getCreatedAt() != null ? rd.getCreatedAt() : "")
                .setUpdatedAt(rd.getUpdatedAt() != null ? rd.getUpdatedAt() : "");
 
        if (rd.getDeletedAt() != null && !rd.getDeletedAt().isEmpty()) {
            b.setDeletedAt(StringValue.of(rd.getDeletedAt()));
        }
        return b.build();
    }
}
