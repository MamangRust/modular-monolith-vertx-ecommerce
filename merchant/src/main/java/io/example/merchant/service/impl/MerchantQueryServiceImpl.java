package io.example.merchant.service.impl;

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
import io.example.merchant.domain.requests.FindAllMerchantRequest;
import io.example.merchant.model.Merchant;
import io.example.merchant.model.MerchantResponse;
import io.example.merchant.model.MerchantResponseDeleteAt;
import io.example.merchant.repository.MerchantQueryRepository;
import io.example.merchant.service.MerchantQueryService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantQueryServiceImpl implements MerchantQueryService {
  private static final Logger log = LoggerFactory.getLogger(MerchantQueryServiceImpl.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private final MerchantQueryRepository repository;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private static final String CACHE_PREFIX = "merchant:";
  private static final Duration CACHE_TTL = Duration.ofMinutes(10);

  private PagedResult<MerchantResponse> mapPagination(PagedResult<Merchant> res) {
    List<MerchantResponse> data = res.getData().stream().map(MerchantResponse::from).toList();
    return new PagedResult<>(data, res.getTotalRecords());
  }

  private PagedResult<MerchantResponseDeleteAt> mapPaginationDeleteAt(PagedResult<Merchant> res) {
    List<MerchantResponseDeleteAt> data = res.getData().stream().map(MerchantResponseDeleteAt::from).toList();
    return new PagedResult<>(data, res.getTotalRecords());
  }

  @Override
  public Future<PagedResult<MerchantResponse>> getAllMerchants(FindAllMerchantRequest req) {
    var ctx = metrics.startSpan("MerchantQueryService.getAllMerchants");
    String cacheKey = CACHE_PREFIX + "list:all:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
        + req.getPage() + ":" + req.getPageSize();

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<Merchant> typedCached = mapper.readValue(jsonStr,
                  new TypeReference<PagedResult<Merchant>>() {
                  });
              return Future.succeededFuture(mapPagination(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached merchants: {}", e.getMessage());
            }
          }
          return repository.getMerchants(req)
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPagination);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getAllMerchants", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getAllMerchants", e.getMessage()));
  }

  @Override
  public Future<PagedResult<MerchantResponseDeleteAt>> getActiveMerchants(FindAllMerchantRequest req) {
    var ctx = metrics.startSpan("MerchantQueryService.getActiveMerchants");
    String cacheKey = CACHE_PREFIX + "list:active:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
        + req.getPage() + ":" + req.getPageSize();

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<Merchant> typedCached = mapper.readValue(jsonStr,
                  new TypeReference<PagedResult<Merchant>>() {
                  });
              return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached active merchants: {}", e.getMessage());
            }
          }
          return repository.getActiveMerchants(req)
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPaginationDeleteAt);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getActiveMerchants", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getActiveMerchants", e.getMessage()));
  }

  @Override
  public Future<PagedResult<MerchantResponseDeleteAt>> getTrashedMerchants(FindAllMerchantRequest req) {
    var ctx = metrics.startSpan("MerchantQueryService.getTrashedMerchants");
    String cacheKey = CACHE_PREFIX + "list:trashed:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
        + req.getPage() + ":" + req.getPageSize();

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<Merchant> typedCached = mapper.readValue(jsonStr,
                  new TypeReference<PagedResult<Merchant>>() {
                  });
              return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached trashed merchants: {}", e.getMessage());
            }
          }
          return repository.getTrashedMerchants(req)
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPaginationDeleteAt);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTrashedMerchants", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getTrashedMerchants", e.getMessage()));
  }

  @Override
  public Future<MerchantResponse> getMerchantById(Long merchantId) {
    var ctx = metrics.startSpan("MerchantQueryService.getMerchantById",
        Attributes.builder().put("merchant.id", (long) merchantId).build());
    String key = CACHE_PREFIX + "id:" + merchantId;

    return redis.getJson(key, Merchant.class)
        .compose(cached -> {
          if (cached != null) {
            return Future.succeededFuture(MerchantResponse.from(cached));
          }
          return repository.getMerchantById(merchantId)
              .compose(db -> {
                if (db == null) {
                  return Future.<Merchant>failedFuture(new NotFoundException("Merchant not found"));
                }
                return redis.setJson(key, db, CACHE_TTL).<Merchant>map(v -> db);
              })
              .map(MerchantResponse::from);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getMerchantById", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getMerchantById", e.getMessage()));
  }
}