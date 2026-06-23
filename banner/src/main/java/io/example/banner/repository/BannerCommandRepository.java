package io.example.banner.repository;

import io.example.banner.domain.requests.CreateBannerRequest;
import io.example.banner.domain.requests.UpdateBannerRequest;
import io.example.banner.model.Banner;
import io.vertx.core.Future;

public interface BannerCommandRepository {
    Future<Banner> createBanner(CreateBannerRequest req);

    Future<Banner> updateBanner(UpdateBannerRequest req);

    Future<Banner> trashed(Long bannerId);

    Future<Banner> restore(Long bannerId);

    Future<Void> deletePermanent(Long bannerId);

    Future<Void> restoreAll();

    Future<Void> deleteAll();
}
