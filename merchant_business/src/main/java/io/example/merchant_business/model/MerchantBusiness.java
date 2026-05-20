package io.example.merchant_business.model;

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
public class MerchantBusiness {
  private Long merchantBusinessInfoId;
  private Integer merchantId;
  private String businessType;
  private String taxId;
  private Integer establishedYear;
  private Integer numberOfEmployees;
  private String websiteUrl;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  private Timestamp deletedAt;
  private String merchantName;

  public static MerchantBusiness fromRow(Row row) {
    if (row == null) {
      return null;
    }

    String merchantName = null;
    try {
      merchantName = row.getString("merchant_name");
    } catch (Exception ignored) {
    }

    return MerchantBusiness.builder()
        .merchantBusinessInfoId(row.getLong("merchant_business_info_id"))
        .merchantId(row.getInteger("merchant_id"))
        .businessType(row.getString("business_type"))
        .taxId(row.getString("tax_id"))
        .establishedYear(row.getInteger("established_year"))
        .numberOfEmployees(row.getInteger("number_of_employees"))
        .websiteUrl(row.getString("website_url"))
        .createdAt(getTimestampFromRow(row, "created_at"))
        .updatedAt(getTimestampFromRow(row, "updated_at"))
        .deletedAt(getTimestampFromRow(row, "deleted_at"))
        .merchantName(merchantName)
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
