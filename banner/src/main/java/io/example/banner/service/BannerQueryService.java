package io.example.banner.service;

import io.example.common.domain.PagedResult;
import io.example.banner.model.BannerResponse;
import io.example.banner.model.BannerResponseDeleteAt;
import io.example.banner.domain.requests.FindAllBannerRequest;
import io.vertx.core.Future;

public interface BannerQueryService {
    Future<PagedResult<BannerResponse>> getBanners(FindAllBannerRequest req);

    Future<PagedResult<BannerResponseDeleteAt>> getActiveBanners(FindAllBannerRequest req);

    Future<PagedResult<BannerResponseDeleteAt>> getTrashedBanners(FindAllBannerRequest req);

    Future<BannerResponse> getBannerById(Long id);
}