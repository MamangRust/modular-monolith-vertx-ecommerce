package io.example.review.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRelationsDetail {
    private Long reviewId;
    private Integer userId;
    private Long productId;
    private String name;
    private String comment;
    private Integer rating;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;

    private Integer totalCount;

    private List<ReviewDetail> reviewDetails;

    public static ReviewRelationsDetail fromRow(Row row) {
        if (row == null)
            return null;

        ReviewRelationsDetail relation = ReviewRelationsDetail.builder()
                .reviewId(row.getLong("review_id"))
                .userId(row.getInteger("user_id"))
                .productId(row.getLong("product_id"))
                .name(row.getString("name"))
                .comment(row.getString("comment"))
                .rating(row.getInteger("rating"))
                .createdAt(getTimestampFromRow(row, "created_at"))
                .updatedAt(getTimestampFromRow(row, "updated_at"))
                .deletedAt(getTimestampFromRow(row, "deleted_at"))
                .totalCount(row.getInteger("total_count"))
                .build();

        Object detailsObj = row.getValue("review_details");
        if (detailsObj instanceof JsonArray) {
            relation.setReviewDetails(mapDetails((JsonArray) detailsObj));
        } else if (detailsObj instanceof String) {
            try {
                relation.setReviewDetails(mapDetails(new JsonArray((String) detailsObj)));
            } catch (Exception e) {
                relation.setReviewDetails(new ArrayList<>());
            }
        } else {
            relation.setReviewDetails(new ArrayList<>());
        }

        return relation;
    }

    private static Timestamp getTimestampFromRow(Row row, String column) {
        LocalDateTime localDateTime = row.get(LocalDateTime.class, column);
        return localDateTime != null ? Timestamp.valueOf(localDateTime) : null;
    }

    private static List<ReviewDetail> mapDetails(JsonArray arr) {
        List<ReviewDetail> details = new ArrayList<>();
        if (arr == null)
            return details;

        for (Object obj : arr) {
            if (obj instanceof JsonObject json) {
                ReviewDetail detail = new ReviewDetail();
                detail.setReviewDetailId(json.getLong("detail_id"));
                detail.setType(json.getString("type"));
                detail.setUrl(json.getString("url"));
                detail.setCaption(json.getString("caption"));

                Object createdAtObj = json.getValue("created_at");
                if (createdAtObj instanceof String) {
                    try {
                        detail.setCreatedAt(Timestamp.valueOf((String) createdAtObj));
                    } catch (Exception ignored) {
                    }
                }
                details.add(detail);
            }
        }
        return details;
    }
}
