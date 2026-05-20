package io.example.category.handler;

import com.google.protobuf.StringValue;
import io.example.category.model.*;

public class ProtoConverter {

    public static pb.category.CategoryCommon.CategoryResponse toCategoryResponse(CategoryResponse r) {
        if (r == null) {
            return pb.category.CategoryCommon.CategoryResponse.getDefaultInstance();
        }
        return pb.category.CategoryCommon.CategoryResponse.newBuilder()
                .setId(r.getId() != null ? r.getId().intValue() : 0)
                .setName(r.getName() != null ? r.getName() : "")
                .setDescription(r.getDescription() != null ? r.getDescription() : "")
                .setSlugCategory(r.getSlugCategory() != null ? r.getSlugCategory() : "")
                .setImageCategory(r.getImageCategory() != null ? r.getImageCategory() : "")
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
                .build();
    }

    public static pb.category.CategoryCommon.CategoryResponseDeleteAt toCategoryResponseDeleteAt(CategoryResponseDeleteAt r) {
        if (r == null) {
            return pb.category.CategoryCommon.CategoryResponseDeleteAt.getDefaultInstance();
        }
        pb.category.CategoryCommon.CategoryResponseDeleteAt.Builder builder = pb.category.CategoryCommon.CategoryResponseDeleteAt.newBuilder()
                .setId(r.getId() != null ? r.getId().intValue() : 0)
                .setName(r.getName() != null ? r.getName() : "")
                .setDescription(r.getDescription() != null ? r.getDescription() : "")
                .setSlugCategory(r.getSlugCategory() != null ? r.getSlugCategory() : "")
                .setImageCategory(r.getImageCategory() != null ? r.getImageCategory() : "")
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "");

        if (r.getDeletedAt() != null) {
            builder.setDeletedAt(StringValue.of(r.getDeletedAt()));
        }
        return builder.build();
    }

    public static pb.category.CategoryCommon.CategoryResponseDeleteAt toCategoryResponseDeleteAt(CategoryResponse r) {
        if (r == null) {
            return pb.category.CategoryCommon.CategoryResponseDeleteAt.getDefaultInstance();
        }
        return pb.category.CategoryCommon.CategoryResponseDeleteAt.newBuilder()
                .setId(r.getId() != null ? r.getId().intValue() : 0)
                .setName(r.getName() != null ? r.getName() : "")
                .setDescription(r.getDescription() != null ? r.getDescription() : "")
                .setSlugCategory(r.getSlugCategory() != null ? r.getSlugCategory() : "")
                .setImageCategory(r.getImageCategory() != null ? r.getImageCategory() : "")
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
                .build();
    }

    public static pb.category.CategoryCommon.CategoryMonthPriceResponse toCategoryMonthPriceResponse(CategoriesMonthPrice r) {
        if (r == null) {
            return pb.category.CategoryCommon.CategoryMonthPriceResponse.getDefaultInstance();
        }
        return pb.category.CategoryCommon.CategoryMonthPriceResponse.newBuilder()
                .setMonth(r.getMonth() != null ? r.getMonth() : "")
                .setCategoryId(r.getCategoryId() != null ? r.getCategoryId() : 0)
                .setCategoryName(r.getCategoryName() != null ? r.getCategoryName() : "")
                .setOrderCount(r.getOrderCount() != null ? r.getOrderCount() : 0)
                .setItemsSold(r.getItemsSold() != null ? r.getItemsSold() : 0)
                .setTotalRevenue(r.getTotalRevenue() != null ? r.getTotalRevenue().intValue() : 0)
                .build();
    }

    public static pb.category.CategoryCommon.CategoryYearPriceResponse toCategoryYearPriceResponse(CategoriesYearPrice r) {
        if (r == null) {
            return pb.category.CategoryCommon.CategoryYearPriceResponse.getDefaultInstance();
        }
        return pb.category.CategoryCommon.CategoryYearPriceResponse.newBuilder()
                .setYear(r.getYear() != null ? r.getYear() : "")
                .setCategoryId(r.getCategoryId() != null ? r.getCategoryId() : 0)
                .setCategoryName(r.getCategoryName() != null ? r.getCategoryName() : "")
                .setOrderCount(r.getOrderCount() != null ? r.getOrderCount() : 0)
                .setItemsSold(r.getItemsSold() != null ? r.getItemsSold() : 0)
                .setTotalRevenue(r.getTotalRevenue() != null ? r.getTotalRevenue().intValue() : 0)
                .setUniqueProductsSold(r.getUniqueProductsSold() != null ? r.getUniqueProductsSold() : 0)
                .build();
    }

    public static pb.category.CategoryCommon.CategoriesMonthlyTotalPriceResponse toCategoriesMonthlyTotalPriceResponse(CategoriesMonthlyTotalPrice r) {
        if (r == null) {
            return pb.category.CategoryCommon.CategoriesMonthlyTotalPriceResponse.getDefaultInstance();
        }
        return pb.category.CategoryCommon.CategoriesMonthlyTotalPriceResponse.newBuilder()
                .setYear(r.getYear() != null ? r.getYear() : "")
                .setMonth(r.getMonth() != null ? r.getMonth() : "")
                .setTotalRevenue(r.getTotalRevenue() != null ? r.getTotalRevenue().intValue() : 0)
                .build();
    }

    public static pb.category.CategoryCommon.CategoriesYearlyTotalPriceResponse toCategoriesYearlyTotalPriceResponse(CategoriesYearlyTotalPrice r) {
        if (r == null) {
            return pb.category.CategoryCommon.CategoriesYearlyTotalPriceResponse.getDefaultInstance();
        }
        return pb.category.CategoryCommon.CategoriesYearlyTotalPriceResponse.newBuilder()
                .setYear(r.getYear() != null ? r.getYear() : "")
                .setTotalRevenue(r.getTotalRevenue() != null ? r.getTotalRevenue().intValue() : 0)
                .build();
    }
}
