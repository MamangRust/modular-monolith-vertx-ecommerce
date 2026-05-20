package io.example.banner.handler;

import com.google.protobuf.StringValue;
import io.example.banner.model.BannerResponse;
import io.example.banner.model.BannerResponseDeleteAt;

public class ProtoConverter {

    public static pb.banner.BannerCommon.BannerResponse fromBannerResponse(BannerResponse r) {
        if (r == null) {
            return pb.banner.BannerCommon.BannerResponse.getDefaultInstance();
        }
        return pb.banner.BannerCommon.BannerResponse.newBuilder()
                .setBannerId(r.getId() != null ? r.getId().intValue() : 0)
                .setName(r.getName() != null ? r.getName() : "")
                .setStartDate(r.getStartDate() != null ? r.getStartDate() : "")
                .setEndDate(r.getEndDate() != null ? r.getEndDate() : "")
                .setStartTime(r.getStartTime() != null ? r.getStartTime() : "")
                .setEndTime(r.getEndTime() != null ? r.getEndTime() : "")
                .setIsActive(r.getIsActive() != null ? r.getIsActive() : false)
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "")
                .build();
    }

    public static pb.banner.BannerCommon.BannerResponseDeleteAt fromBannerResponseDeleteAt(BannerResponseDeleteAt r) {
        if (r == null) {
            return pb.banner.BannerCommon.BannerResponseDeleteAt.getDefaultInstance();
        }
        pb.banner.BannerCommon.BannerResponseDeleteAt.Builder b = pb.banner.BannerCommon.BannerResponseDeleteAt.newBuilder()
                .setBannerId(r.getId() != null ? r.getId().intValue() : 0)
                .setName(r.getName() != null ? r.getName() : "")
                .setStartDate(r.getStartDate() != null ? r.getStartDate() : "")
                .setEndDate(r.getEndDate() != null ? r.getEndDate() : "")
                .setStartTime(r.getStartTime() != null ? r.getStartTime() : "")
                .setEndTime(r.getEndTime() != null ? r.getEndTime() : "")
                .setIsActive(r.getIsActive() != null ? r.getIsActive() : false)
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt() : "");

        if (r.getDeletedAt() != null) {
            b.setDeletedAt(StringValue.of(r.getDeletedAt()));
        }
        return b.build();
    }
}
