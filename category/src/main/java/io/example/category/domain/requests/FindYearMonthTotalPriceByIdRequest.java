package io.example.category.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FindYearMonthTotalPriceByIdRequest {
    private Integer year;
    private Integer month;
    private Long categoryId;
}
