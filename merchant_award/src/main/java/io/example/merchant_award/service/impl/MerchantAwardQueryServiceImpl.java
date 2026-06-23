package io.example.merchant_award.service.impl;

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
import io.example.merchant_award.domain.requests.FindAllMerchantAwardsRequest;
import io.example.merchant_award.model.MerchantAward;
import io.example.merchant_award.model.MerchantAwardResponse;
import io.example.merchant_award.model.MerchantAwardResponseDeleteAt;
import io.example.merchant_award.repository.MerchantAwardQueryRepository;
import io.example.merchant_award.service.MerchantAwardQueryService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantAwardQueryServiceImpl implements MerchantAwardQueryService {
  private static final Logger log = LoggerFactory.getLogger(MerchantAwardQueryServiceImpl.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private final MerchantAwardQueryRepository repository;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private static final String CACHE_PREFIX = "merchant_award:";
  private static final Duration CACHE_TTL = Duration.ofMinutes(10);

  private PagedResult<MerchantAwardResponse> mapPagination(PagedResult<MerchantAward> res) {
    List<MerchantAwardResponse> data = res.getData().stream().map(MerchantAwardResponse::from).toList();
    return new PagedResult<>(data, res.getTotalRecords());
  }

  private PagedResult<MerchantAwardResponseDeleteAt> mapPaginationDeleteAt(PagedResult<MerchantAward> res) {
    List<MerchantAwardResponseDeleteAt> data = res.getData().stream().map(MerchantAwardResponseDeleteAt::from).toList();
    return new PagedResult<>(data, res.getTotalRecords());
  }

  @Override
  public Future<PagedResult<MerchantAwardResponse>> getAll(FindAllMerchantAwardsRequest req) {
    var ctx = metrics.startSpan("MerchantAwardQueryService.getAll");
    String cacheKey = CACHE_PREFIX + "list:all:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
        + req.getPage() + ":" + req.getPageSize();

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<MerchantAward> typedCached = mapper.readValue(jsonStr,
                  new TypeReference<PagedResult<MerchantAward>>() {
                  });
              return Future.succeededFuture(mapPagination(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached awards: {}", e.getMessage());
            }
          }
          return repository.getMerchantCertificationsAndAwards(req)
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPagination);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getAll", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getAll", e.getMessage()));
  }

  @Override
  public Future<PagedResult<MerchantAwardResponseDeleteAt>> getActive(FindAllMerchantAwardsRequest req) {
    var ctx = metrics.startSpan("MerchantAwardQueryService.getActive");
    String cacheKey = CACHE_PREFIX + "list:active:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
        + req.getPage() + ":" + req.getPageSize();

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<MerchantAward> typedCached = mapper.readValue(jsonStr,
                  new TypeReference<PagedResult<MerchantAward>>() {
                  });
              return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached active awards: {}", e.getMessage());
            }
          }
          return repository.getMerchantCertificationsAndAwardsActive(req)
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPaginationDeleteAt);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getActive", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getActive", e.getMessage()));
  }

  @Override
  public Future<PagedResult<MerchantAwardResponseDeleteAt>> getTrashed(FindAllMerchantAwardsRequest req) {
    var ctx = metrics.startSpan("MerchantAwardQueryService.getTrashed");
    String cacheKey = CACHE_PREFIX + "list:trashed:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
        + req.getPage() + ":" + req.getPageSize();

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<MerchantAward> typedCached = mapper.readValue(jsonStr,
                  new TypeReference<PagedResult<MerchantAward>>() {
                  });
              return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached trashed awards: {}", e.getMessage());
            }
          }
          return repository.getTrashedCertificationsAndAwards(req)
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPaginationDeleteAt);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTrashed", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getTrashed", e.getMessage()));
  }

  @Override
  public Future<MerchantAwardResponse> getById(Long id) {
    var ctx = metrics.startSpan("MerchantAwardQueryService.getById",
        Attributes.builder().put("award.id", id).build());
    String key = CACHE_PREFIX + "id:" + id;

    return redis.getJson(key, MerchantAward.class)
        .compose(cached -> {
          if (cached != null) {
            return Future.succeededFuture(MerchantAwardResponse.from(cached));
          }
          return repository.getMerchantCertificationOrAward(id)
              .compose(db -> {
                if (db == null) {
                  return Future.<MerchantAward>failedFuture(new NotFoundException("Award not found"));
                }
                return redis.setJson(key, db, CACHE_TTL).<MerchantAward>map(v -> db);
              })
              .map(MerchantAwardResponse::from);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getById", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getById", e.getMessage()));
  }
}