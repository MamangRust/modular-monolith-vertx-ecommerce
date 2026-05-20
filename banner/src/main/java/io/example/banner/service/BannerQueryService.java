package io.example.banner.service;

import java.util.List;

import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.banner.model.BannerResponse;
import io.example.banner.model.BannerResponseDeleteAt;
import io.vertx.core.Future;
import pb.banner.BannerQuery.FindAllBannerRequest;

public interface BannerQueryService {
    Future<ApiResponsePagination<List<BannerResponse>>> getAllBanners(FindAllBannerRequest req);
    Future<ApiResponsePagination<List<BannerResponseDeleteAt>>> getActiveBanners(FindAllBannerRequest req);
    Future<ApiResponsePagination<List<BannerResponseDeleteAt>>> getTrashedBanners(FindAllBannerRequest req);
    Future<ApiResponse<BannerResponse>> getBannerById(Long bannerId);
}
