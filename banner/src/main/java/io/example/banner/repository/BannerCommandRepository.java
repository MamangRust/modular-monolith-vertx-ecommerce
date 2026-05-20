package io.example.banner.repository;

import io.example.banner.model.Banner;
import io.vertx.core.Future;
import pb.banner.BannerCommand.CreateBannerRequest;
import pb.banner.BannerCommand.UpdateBannerRequest;

public interface BannerCommandRepository {
    Future<Banner> createBanner(CreateBannerRequest req);
    Future<Banner> updateBanner(UpdateBannerRequest req);
    Future<Banner> trashed(Long bannerId);
    Future<Banner> restore(Long bannerId);
    Future<Void> deletePermanent(Long bannerId);
    Future<Void> restoreAll();
    Future<Void> deleteAll();
}
