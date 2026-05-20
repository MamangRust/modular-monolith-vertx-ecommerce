package io.example.merchant_detail.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import io.vertx.sqlclient.Row;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantSocialMediaLink {
  private Long merchantSocialId;
  private Integer merchantDetailId;
  private String platform;
  private String url;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  private Timestamp deletedAt;

  public static MerchantSocialMediaLink fromRow(Row row) {
    if (row == null) {
      return null;
    }
    return MerchantSocialMediaLink.builder()
        .merchantSocialId(row.getLong("merchant_social_id"))
        .merchantDetailId(row.getInteger("merchant_detail_id"))
        .platform(row.getString("platform"))
        .url(row.getString("url"))
        .createdAt(getTimestampFromRow(row, "created_at"))
        .updatedAt(getTimestampFromRow(row, "updated_at"))
        .deletedAt(getTimestampFromRow(row, "deleted_at"))
        .build();
  }

  private static Timestamp getTimestampFromRow(Row row, String column) {
    try {
      LocalDateTime localDateTime = row.get(LocalDateTime.class, column);
      return localDateTime != null ? Timestamp.valueOf(localDateTime) : null;
    } catch (Exception e) {
      return null;
    }
  }
}
