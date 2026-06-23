package io.example.banner.domain.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBannerRequest {
    private Long bannerId;
    private String name;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private Boolean isActive;
}