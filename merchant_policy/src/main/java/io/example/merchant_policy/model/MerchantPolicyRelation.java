package io.example.merchant_policy.model;

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
public class MerchantPolicyRelation {
  private Long merchantPolicyId;
  private Long merchantId;
  private String policyType;
  private String title;
  private String description;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  private Timestamp deletedAt;
  private String merchantName;
  private Integer totalCount;

  public static MerchantPolicyRelation fromRow(Row row) {
    if (row == null) {
      return null;
    }
    return MerchantPolicyRelation.builder()
        .merchantPolicyId(row.getLong("merchant_policy_id"))
        .merchantId(row.getLong("merchant_id"))
        .policyType(row.getString("policy_type"))
        .title(row.getString("title"))
        .description(row.getString("description"))
        .createdAt(getTimestampFromRow(row, "created_at"))
        .updatedAt(getTimestampFromRow(row, "updated_at"))
        .deletedAt(getTimestampFromRow(row, "deleted_at"))
        .merchantName(row.getString("merchant_name"))
        .totalCount(row.getInteger("total_count"))
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
