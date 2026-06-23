package io.example.banner.repository;

import io.example.common.domain.PagedResult;
import io.example.banner.domain.requests.FindAllBannerRequest;
import io.example.banner.model.Banner;
import io.vertx.core.Future;

public interface BannerQueryRepository {
    Future<PagedResult<Banner>> getBanners(FindAllBannerRequest req);

    Future<PagedResult<Banner>> getActiveBanners(FindAllBannerRequest req);

    Future<PagedResult<Banner>> getTrashedBanners(FindAllBannerRequest req);

    Future<Banner> getBannerById(Long bannerId);
}
