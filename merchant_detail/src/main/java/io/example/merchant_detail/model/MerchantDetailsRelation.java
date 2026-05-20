package io.example.merchant_detail.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import io.vertx.core.json.JsonArray;
import io.vertx.sqlclient.Row;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantDetailsRelation {
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
  private String merchantName;
  private Integer totalCount;
  private List<MerchantSocialMediaLink> socialMediaLinks;

  public static MerchantDetailsRelation fromRow(Row row) {
    if (row == null) {
      return null;
    }

    MerchantDetailsRelation relation = MerchantDetailsRelation.builder()
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
        .merchantName(row.getString("merchant_name"))
        .totalCount(row.getInteger("total_count"))
        .build();

    Object socialLinksObj = row.getValue("social_media_links");
    if (socialLinksObj instanceof JsonArray) {
      relation.setSocialMediaLinks(mapSocialLinks((JsonArray) socialLinksObj));
    } else if (socialLinksObj instanceof String) {
      try {
        JsonArray arr = new JsonArray((String) socialLinksObj);
        relation.setSocialMediaLinks(mapSocialLinks(arr));
      } catch (Exception e) {
        relation.setSocialMediaLinks(List.of());
      }
    } else {
      relation.setSocialMediaLinks(List.of());
    }

    return relation;
  }

  private static List<MerchantSocialMediaLink> mapSocialLinks(JsonArray arr) {
    return arr.stream()
        .filter(obj -> obj instanceof io.vertx.core.json.JsonObject)
        .map(obj -> (io.vertx.core.json.JsonObject) obj)
        .map(json -> MerchantSocialMediaLink.builder()
            .merchantSocialId(json.getLong("id"))
            .platform(json.getString("platform"))
            .url(json.getString("url"))
            .build())
        .toList();
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
