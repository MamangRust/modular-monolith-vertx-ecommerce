package io.example.banner.service;

import io.example.banner.domain.requests.CreateBannerRequest;
import io.example.banner.domain.requests.UpdateBannerRequest;
import io.example.banner.model.BannerResponse;
import io.example.banner.model.BannerResponseDeleteAt;
import io.example.common.domain.ApiResponse;
import io.vertx.core.Future;

public interface BannerCommandService {
    Future<ApiResponse<BannerResponse>> createBanner(CreateBannerRequest req);

    Future<ApiResponse<BannerResponse>> updateBanner(UpdateBannerRequest req);

    Future<ApiResponse<BannerResponseDeleteAt>> trashBanner(Long bannerId);

    Future<ApiResponse<BannerResponseDeleteAt>> restoreBanner(Long bannerId);

    Future<ApiResponse<Void>> deletePermanent(Long bannerId);

    Future<ApiResponse<Void>> restoreAllBanners();

    Future<ApiResponse<Void>> deleteAllPermanentBanners();
}
