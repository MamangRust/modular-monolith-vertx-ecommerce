package io.example.category.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriesYearPrice {
    private String year;
    private Integer categoryId;
    private String categoryName;
    private Integer orderCount;
    private Integer itemsSold;
    private Long totalRevenue;
    private Integer uniqueProductsSold;
}
