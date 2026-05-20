package io.example.banner.repository;

import io.example.common.domain.PagedResult;
import io.example.banner.model.Banner;
import io.vertx.core.Future;

public interface BannerQueryRepository {
    Future<PagedResult<Banner>> getBanners(String search, int page, int pageSize);
    Future<PagedResult<Banner>> getActiveBanners(String search, int page, int pageSize);
    Future<PagedResult<Banner>> getTrashedBanners(String search, int page, int pageSize);
    Future<Banner> getBannerById(Long bannerId);
}
