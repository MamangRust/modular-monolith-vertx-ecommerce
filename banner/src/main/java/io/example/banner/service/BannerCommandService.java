package io.example.banner.service;

import io.example.banner.domain.requests.CreateBannerRequest;
import io.example.banner.domain.requests.UpdateBannerRequest;
import io.example.banner.model.BannerResponse;
import io.example.banner.model.BannerResponseDeleteAt;
import io.vertx.core.Future;

public interface BannerCommandService {
    Future<BannerResponse> createBanner(CreateBannerRequest req);

    Future<BannerResponse> updateBanner(UpdateBannerRequest req);

    Future<BannerResponseDeleteAt> trashBanner(Long bannerId);

    Future<BannerResponseDeleteAt> restoreBanner(Long bannerId);

    Future<Void> deletePermanent(Long bannerId);

    Future<Void> restoreAllBanners();

    Future<Void> deleteAllPermanentBanners();
}