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
public class MerchantPolicy {
  private Long merchantPolicyId;
  private Integer merchantId;
  private String policyType;
  private String title;
  private String description;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  private Timestamp deletedAt;

  public static MerchantPolicy fromRow(Row row) {
    if (row == null) {
      return null;
    }
    return MerchantPolicy.builder()
        .merchantPolicyId(row.getLong("merchant_policy_id"))
        .merchantId(row.getInteger("merchant_id"))
        .policyType(row.getString("policy_type"))
        .title(row.getString("title"))
        .description(row.getString("description"))
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
