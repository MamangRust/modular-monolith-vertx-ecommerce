package io.example.merchant_detail.service.impl;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.domain.PagedResult;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_detail.domain.requests.FindAllMerchantDetailRequest;
import io.example.merchant_detail.model.MerchantDetailResponse;
import io.example.merchant_detail.model.MerchantDetailResponseDeleteAt;
import io.example.merchant_detail.model.MerchantDetailsRelation;
import io.example.merchant_detail.repository.MerchantDetailQueryRepository;
import io.example.merchant_detail.service.MerchantDetailQueryService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantDetailQueryServiceImpl implements MerchantDetailQueryService {
  private static final Logger log = LoggerFactory.getLogger(MerchantDetailQueryServiceImpl.class);
  private static final ObjectMapper mapper = new ObjectMapper();

  private final MerchantDetailQueryRepository repository;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private static final String CACHE_PREFIX = "merchant_detail:";
  private static final Duration CACHE_TTL = Duration.ofMinutes(60);

  private PagedResult<MerchantDetailResponse> mapPagination(PagedResult<MerchantDetailsRelation> res) {
    List<MerchantDetailResponse> data = res.getData().stream().map(MerchantDetailResponse::from).toList();
    return new PagedResult<>(data, res.getTotalRecords());
  }

  private PagedResult<MerchantDetailResponseDeleteAt> mapPaginationDeleteAt(PagedResult<MerchantDetailsRelation> res) {
    List<MerchantDetailResponseDeleteAt> data = res.getData().stream().map(MerchantDetailResponseDeleteAt::from)
        .toList();
    return new PagedResult<>(data, res.getTotalRecords());
  }

  private String buildListCacheKey(String scope, FindAllMerchantDetailRequest req) {
    return CACHE_PREFIX + "list:" + scope + ":"
        + (req.getSearch() != null ? req.getSearch() : "")
        + ":" + req.getPage() + ":" + req.getPageSize();
  }

  @Override
  public Future<PagedResult<MerchantDetailResponse>> getMerchantDetails(FindAllMerchantDetailRequest req) {
    var ctx = metrics.startSpan("MerchantDetailQueryService.getMerchantDetails");
    String cacheKey = buildListCacheKey("all", req);

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<MerchantDetailsRelation> typedCached = mapper.readValue(
                  jsonStr, new TypeReference<PagedResult<MerchantDetailsRelation>>() {
                  });
              return Future.succeededFuture(mapPagination(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached merchant details: {}", e.getMessage());
            }
          }
          return repository.getMerchantDetails(req)
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPagination);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "get_all", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "get_all", e.getMessage()));
  }

  @Override
  public Future<PagedResult<MerchantDetailResponseDeleteAt>> getMerchantDetailsActive(FindAllMerchantDetailRequest req) {
    var ctx = metrics.startSpan("MerchantDetailQueryService.getMerchantDetailsActive");
    String cacheKey = buildListCacheKey("active", req);

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<MerchantDetailsRelation> typedCached = mapper.readValue(
                  jsonStr, new TypeReference<PagedResult<MerchantDetailsRelation>>() {
                  });
              return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached active merchant details: {}", e.getMessage());
            }
          }
          return repository.getMerchantDetailsActive(req)
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPaginationDeleteAt);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "get_active", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "get_active", e.getMessage()));
  }

  @Override
  public Future<PagedResult<MerchantDetailResponseDeleteAt>> getMerchantDetailsTrashed(
      FindAllMerchantDetailRequest req) {
    var ctx = metrics.startSpan("MerchantDetailQueryService.getMerchantDetailsTrashed");
    String cacheKey = buildListCacheKey("trashed", req);

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<MerchantDetailsRelation> typedCached = mapper.readValue(
                  jsonStr, new TypeReference<PagedResult<MerchantDetailsRelation>>() {
                  });
              return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached trashed merchant details: {}", e.getMessage());
            }
          }
          return repository.getMerchantDetailsTrashed(req)
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPaginationDeleteAt);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "get_trashed", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "get_trashed", e.getMessage()));
  }

  @Override
  public Future<MerchantDetailResponse> getMerchantDetail(Long merchantDetailId) {
    var ctx = metrics.startSpan("MerchantDetailQueryService.getMerchantDetail",
        Attributes.builder().put("merchant_detail.id", merchantDetailId).build());
    String cacheKey = CACHE_PREFIX + "id:" + merchantDetailId;

    return redis.getJson(cacheKey, MerchantDetailsRelation.class)
        .compose(cached -> {
          if (cached != null) {
            return Future.succeededFuture(MerchantDetailResponse.from(cached));
          }
          return repository.getMerchantDetail(merchantDetailId)
              .compose(db -> {
                if (db == null) {
                  return Future.<MerchantDetailsRelation>failedFuture(
                      new NotFoundException("Merchant Detail not found"));
                }
                return redis.setJson(cacheKey, db, CACHE_TTL).<MerchantDetailsRelation>map(v -> db);
              })
              .map(MerchantDetailResponse::from);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "get_by_id", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "get_by_id", e.getMessage()));
  }
}