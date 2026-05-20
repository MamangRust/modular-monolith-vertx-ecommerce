package io.example.merchant_award.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantAward {
  private Long merchantCertificationId;
  private Integer merchantId;
  private String title;
  private String description;
  private String issuedBy;
  private Date issueDate;
  private Date expiryDate;
  private String certificateUrl;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  private Timestamp deletedAt;

  private String merchantName;
  private Integer totalCount;

  public JsonObject toJson() {
    JsonObject json = new JsonObject()
        .put("merchantCertificationId", merchantCertificationId)
        .put("merchantId", merchantId)
        .put("title", title)
        .put("description", description)
        .put("issuedBy", issuedBy)
        .put("certificateUrl", certificateUrl)
        .put("merchantName", merchantName)
        .put("totalCount", totalCount);

    if (issueDate != null) {
      json.put("issueDate", issueDate.toString());
    }
    if (expiryDate != null) {
      json.put("expiryDate", expiryDate.toString());
    }
    if (createdAt != null) {
      json.put("createdAt", createdAt.toString());
    }
    if (updatedAt != null) {
      json.put("updatedAt", updatedAt.toString());
    }
    if (deletedAt != null) {
      json.put("deletedAt", deletedAt.toString());
    }
    return json;
  }

  public static MerchantAward fromJson(JsonObject json) {
    if (json == null) {
      return null;
    }
    return MerchantAward.builder()
        .merchantCertificationId(json.getLong("merchantCertificationId"))
        .merchantId(json.getInteger("merchantId"))
        .title(json.getString("title"))
        .description(json.getString("description"))
        .issuedBy(json.getString("issuedBy"))
        .issueDate(parseDate(json.getValue("issueDate")))
        .expiryDate(parseDate(json.getValue("expiryDate")))
        .certificateUrl(json.getString("certificateUrl"))
        .createdAt(parseTimestamp(json, "createdAt"))
        .updatedAt(parseTimestamp(json, "updatedAt"))
        .deletedAt(parseTimestamp(json, "deletedAt"))
        .merchantName(json.getString("merchantName"))
        .totalCount(json.getInteger("totalCount"))
        .build();
  }

  public static MerchantAward fromRow(Row row) {
    if (row == null) {
      return null;
    }

    Long mcaId = row.getLong("merchant_certification_id");
    if (mcaId == null) {
      mcaId = row.getLong("merchantCertificationId");
    }

    Integer mId = row.getInteger("merchant_id");
    if (mId == null) {
      mId = row.getInteger("merchantId");
    }

    String title = row.getString("title");
    String desc = row.getString("description");
    String issuedBy = row.getString("issued_by");
    if (issuedBy == null) {
      issuedBy = row.getString("issuedBy");
    }

    LocalDate issueDateLocal = row.get(LocalDate.class, "issue_date");
    if (issueDateLocal == null) {
      issueDateLocal = row.get(LocalDate.class, "issueDate");
    }
    Date issueDate = issueDateLocal != null ? Date.valueOf(issueDateLocal) : null;

    LocalDate expiryDateLocal = row.get(LocalDate.class, "expiry_date");
    if (expiryDateLocal == null) {
      expiryDateLocal = row.get(LocalDate.class, "expiryDate");
    }
    Date expiryDate = expiryDateLocal != null ? Date.valueOf(expiryDateLocal) : null;

    String certificateUrl = row.getString("certificate_url");
    if (certificateUrl == null) {
      certificateUrl = row.getString("certificateUrl");
    }

    Timestamp createdAt = getTimestampFromRow(row, "created_at");
    if (createdAt == null) {
      createdAt = getTimestampFromRow(row, "createdAt");
    }

    Timestamp updatedAt = getTimestampFromRow(row, "updated_at");
    if (updatedAt == null) {
      updatedAt = getTimestampFromRow(row, "updatedAt");
    }

    Timestamp deletedAt = getTimestampFromRow(row, "deleted_at");
    if (deletedAt == null) {
      deletedAt = getTimestampFromRow(row, "deletedAt");
    }

    String merchantName = null;
    try {
      merchantName = row.getString("merchant_name");
      if (merchantName == null) {
        merchantName = row.getString("merchantName");
      }
    } catch (Exception ignored) {}

    Integer totalCount = null;
    try {
      totalCount = row.getInteger("total_count");
      if (totalCount == null) {
        totalCount = row.getInteger("totalCount");
      }
    } catch (Exception ignored) {}

    return MerchantAward.builder()
        .merchantCertificationId(mcaId)
        .merchantId(mId)
        .title(title)
        .description(desc)
        .issuedBy(issuedBy)
        .issueDate(issueDate)
        .expiryDate(expiryDate)
        .certificateUrl(certificateUrl)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .deletedAt(deletedAt)
        .merchantName(merchantName)
        .totalCount(totalCount)
        .build();
  }

  private static Date parseDate(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Date) {
      return (Date) value;
    }
    if (value instanceof String str && !str.isBlank()) {
      try {
        return Date.valueOf(str);
      } catch (IllegalArgumentException e) {
        return null;
      }
    }
    return null;
  }

  private static Timestamp parseTimestamp(JsonObject json, String field) {
    Object value = json.getValue(field);
    if (value == null) {
      return null;
    }
    if (value instanceof Timestamp ts) {
      return ts;
    }
    if (value instanceof String str && !str.isBlank()) {
      try {
        return Timestamp.from(Instant.parse(str));
      } catch (DateTimeParseException e) {
        try {
          return Timestamp.valueOf(str);
        } catch (IllegalArgumentException ex) {
          return null;
        }
      }
    }
    if (value instanceof Number num) {
      return new Timestamp(num.longValue());
    }
    return null;
  }

  private static Timestamp getTimestampFromRow(Row row, String column) {
    try {
      LocalDateTime localDateTime = row.get(LocalDateTime.class, column);
      return localDateTime != null ? Timestamp.valueOf(localDateTime) : null;
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public String toString() {
    return toJson().encode();
  }
}
