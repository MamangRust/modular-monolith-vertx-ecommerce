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
public class MerchantDetail {
  private Long merchantDetailId;
  private Integer merchantId;
  private String displayName;
  private String coverImageUrl;
  private String logoUrl;
  private String shortDescription;
  private String websiteUrl;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  private Timestamp deletedAt;

  public static MerchantDetail fromRow(Row row) {
    if (row == null) {
      return null;
    }
    return MerchantDetail.builder()
        .merchantDetailId(row.getLong("merchant_detail_id"))
        .merchantId(row.getInteger("merchant_id"))
        .displayName(row.getString("display_name"))
        .coverImageUrl(row.getString("cover_image_url"))
        .logoUrl(row.getString("logo_url"))
        .shortDescription(row.getString("short_description"))
        .websiteUrl(row.getString("website_url"))
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
