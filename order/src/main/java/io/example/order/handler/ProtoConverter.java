package io.example.order.handler;

import com.google.protobuf.StringValue;
import io.example.order.model.OrderResponse;
import io.example.order.model.OrderResponseDeleteAt;
import io.example.order.model.OrderMonthly;
import io.example.order.model.OrderYearly;
import io.example.order.model.OrderMonthlyTotalRevenue;
import io.example.order.model.OrderYearlyTotalRevenue;

public class ProtoConverter {

    public static pb.order.OrderCommon.OrderResponse toOrderResponse(OrderResponse r) {
        if (r == null) {
            return pb.order.OrderCommon.OrderResponse.getDefaultInstance();
        }
        return pb.order.OrderCommon.OrderResponse.newBuilder()
                .setId(r.getId() != null ? r.getId().intValue() : 0)
                .setMerchantId(r.getMerchantId() != null ? r.getMerchantId() : 0)
                .setUserId(r.getUserId() != null ? r.getUserId().intValue() : 0)
                .setTotalPrice(r.getTotalPrice() != null ? r.getTotalPrice() : 0)
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
                .build();
    }

    public static pb.order.OrderCommon.OrderResponseDeleteAt toOrderResponseDeleteAt(OrderResponseDeleteAt r) {
        if (r == null) {
            return pb.order.OrderCommon.OrderResponseDeleteAt.getDefaultInstance();
        }
        pb.order.OrderCommon.OrderResponseDeleteAt.Builder b = pb.order.OrderCommon.OrderResponseDeleteAt.newBuilder()
                .setId(r.getId() != null ? r.getId().intValue() : 0)
                .setMerchantId(r.getMerchantId() != null ? r.getMerchantId() : 0)
                .setUserId(r.getUserId() != null ? r.getUserId().intValue() : 0)
                .setTotalPrice(r.getTotalPrice() != null ? r.getTotalPrice() : 0)
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "");

        if (r.getDeletedAt() != null) {
            b.setDeletedAt(StringValue.of(r.getDeletedAt()));
        }
        return b.build();
    }

    public static pb.order.OrderCommon.OrderResponseDeleteAt toOrderResponseDeleteAt(OrderResponse r) {
        if (r == null) {
            return pb.order.OrderCommon.OrderResponseDeleteAt.getDefaultInstance();
        }
        return pb.order.OrderCommon.OrderResponseDeleteAt.newBuilder()
                .setId(r.getId() != null ? r.getId().intValue() : 0)
                .setMerchantId(r.getMerchantId() != null ? r.getMerchantId() : 0)
                .setUserId(r.getUserId() != null ? r.getUserId().intValue() : 0)
                .setTotalPrice(r.getTotalPrice() != null ? r.getTotalPrice() : 0)
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
                .build();
    }

    public static pb.order.OrderCommon.OrderMonthlyResponse toMonthlyResponse(OrderMonthly m) {
        if (m == null) {
            return pb.order.OrderCommon.OrderMonthlyResponse.getDefaultInstance();
        }
        return pb.order.OrderCommon.OrderMonthlyResponse.newBuilder()
                .setMonth(m.getMonth() != null ? m.getMonth() : "")
                .setOrderCount(m.getOrderCount() != null ? m.getOrderCount().intValue() : 0)
                .setTotalRevenue(m.getTotalRevenue() != null ? m.getTotalRevenue().intValue() : 0)
                .setTotalItemsSold(m.getTotalItemsSold() != null ? m.getTotalItemsSold().intValue() : 0)
                .build();
    }

    public static pb.order.OrderCommon.OrderYearlyResponse toYearlyResponse(OrderYearly y) {
        if (y == null) {
            return pb.order.OrderCommon.OrderYearlyResponse.getDefaultInstance();
        }
        return pb.order.OrderCommon.OrderYearlyResponse.newBuilder()
                .setYear(y.getYear() != null ? y.getYear() : "")
                .setOrderCount(y.getOrderCount() != null ? y.getOrderCount().intValue() : 0)
                .setTotalRevenue(y.getTotalRevenue() != null ? y.getTotalRevenue().intValue() : 0)
                .setTotalItemsSold(y.getTotalItemsSold() != null ? y.getTotalItemsSold().intValue() : 0)
                .setActiveCashiers(y.getActiveCashiers() != null ? y.getActiveCashiers().intValue() : 0)
                .setUniqueProductsSold(y.getUniqueProductsSold() != null ? y.getUniqueProductsSold().intValue() : 0)
                .build();
    }

    public static pb.order.OrderCommon.OrderMonthlyTotalRevenueResponse toMonthlyTotalRevenueResponse(OrderMonthlyTotalRevenue m) {
        if (m == null) {
            return pb.order.OrderCommon.OrderMonthlyTotalRevenueResponse.getDefaultInstance();
        }
        return pb.order.OrderCommon.OrderMonthlyTotalRevenueResponse.newBuilder()
                .setYear(m.getYear() != null ? m.getYear() : "")
                .setMonth(m.getMonth() != null ? m.getMonth() : "")
                .setOrderCount(0)
                .setTotalRevenue(m.getTotalRevenue() != null ? m.getTotalRevenue().intValue() : 0)
                .setTotalItemsSold(0)
                .build();
    }

    public static pb.order.OrderCommon.OrderYearlyTotalRevenueResponse toYearlyTotalRevenueResponse(OrderYearlyTotalRevenue y) {
        if (y == null) {
            return pb.order.OrderCommon.OrderYearlyTotalRevenueResponse.getDefaultInstance();
        }
        return pb.order.OrderCommon.OrderYearlyTotalRevenueResponse.newBuilder()
                .setYear(y.getYear() != null ? y.getYear() : "")
                .setOrderCount(0)
                .setTotalRevenue(y.getTotalRevenue() != null ? y.getTotalRevenue().intValue() : 0)
                .setTotalItemsSold(0)
                .setActiveCashiers(0)
                .setUniqueProductsSold(0)
                .build();
    }
}
