package io.example.merchant_business.service.impl;

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
import io.example.merchant_business.domain.requests.FindAllMerchantBusinessRequest;
import io.example.merchant_business.model.MerchantBusiness;
import io.example.merchant_business.model.MerchantBusinessResponse;
import io.example.merchant_business.model.MerchantBusinessResponseDeleteAt;
import io.example.merchant_business.repository.MerchantBusinessQueryRepository;
import io.example.merchant_business.service.MerchantBusinessQueryService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantBusinessQueryServiceImpl implements MerchantBusinessQueryService {
  private static final Logger log = LoggerFactory.getLogger(MerchantBusinessQueryServiceImpl.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private final MerchantBusinessQueryRepository repository;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private static final String CACHE_PREFIX = "merchant_business:";
  private static final Duration CACHE_TTL = Duration.ofMinutes(10);

  private PagedResult<MerchantBusinessResponse> mapPagination(PagedResult<MerchantBusiness> res) {
    List<MerchantBusinessResponse> data = res.getData().stream().map(MerchantBusinessResponse::from).toList();
    return new PagedResult<>(data, res.getTotalRecords());
  }

  private PagedResult<MerchantBusinessResponseDeleteAt> mapPaginationDeleteAt(PagedResult<MerchantBusiness> res) {
    List<MerchantBusinessResponseDeleteAt> data = res.getData().stream().map(MerchantBusinessResponseDeleteAt::from)
        .toList();
    return new PagedResult<>(data, res.getTotalRecords());
  }

  @Override
  public Future<PagedResult<MerchantBusinessResponse>> getAll(FindAllMerchantBusinessRequest req) {
    var ctx = metrics.startSpan("MerchantBusinessQueryService.getAll");
    String cacheKey = CACHE_PREFIX + "list:all:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
        + req.getPage() + ":" + req.getPageSize();

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<MerchantBusiness> typedCached = mapper.readValue(jsonStr,
                  new TypeReference<PagedResult<MerchantBusiness>>() {
                  });
              return Future.succeededFuture(mapPagination(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached merchant business info: {}", e.getMessage());
            }
          }
          return repository.getMerchantsBusinessInformation(req)
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPagination);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getAll", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getAll", e.getMessage()));
  }

  @Override
  public Future<PagedResult<MerchantBusinessResponseDeleteAt>> getActive(FindAllMerchantBusinessRequest req) {
    var ctx = metrics.startSpan("MerchantBusinessQueryService.getActive");
    String cacheKey = CACHE_PREFIX + "list:active:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
        + req.getPage() + ":" + req.getPageSize();

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<MerchantBusiness> typedCached = mapper.readValue(jsonStr,
                  new TypeReference<PagedResult<MerchantBusiness>>() {
                  });
              return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached active merchant business info: {}", e.getMessage());
            }
          }
          return repository.getMerchantsBusinessInformationActive(req)
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPaginationDeleteAt);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getActive", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getActive", e.getMessage()));
  }

  @Override
  public Future<PagedResult<MerchantBusinessResponseDeleteAt>> getTrashed(FindAllMerchantBusinessRequest req) {
    var ctx = metrics.startSpan("MerchantBusinessQueryService.getTrashed");
    String cacheKey = CACHE_PREFIX + "list:trashed:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
        + req.getPage() + ":" + req.getPageSize();

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<MerchantBusiness> typedCached = mapper.readValue(jsonStr,
                  new TypeReference<PagedResult<MerchantBusiness>>() {
                  });
              return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached trashed merchant business info: {}", e.getMessage());
            }
          }
          return repository.getMerchantsBusinessInformationTrashed(req)
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPaginationDeleteAt);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTrashed", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getTrashed", e.getMessage()));
  }

  @Override
  public Future<MerchantBusinessResponse> getById(Long id) {
    var ctx = metrics.startSpan("MerchantBusinessQueryService.getById",
        Attributes.builder().put("business.id", id).build());
    String key = CACHE_PREFIX + "id:" + id;

    return redis.getJson(key, MerchantBusiness.class)
        .compose(cached -> {
          if (cached != null) {
            return Future.succeededFuture(MerchantBusinessResponse.from(cached));
          }
          return repository.getMerchantBusinessInformation(id)
              .compose(db -> {
                if (db == null) {
                  return Future
                      .<MerchantBusiness>failedFuture(new NotFoundException("Merchant Business Information not found"));
                }
                return redis.setJson(key, db, CACHE_TTL).<MerchantBusiness>map(v -> db);
              })
              .map(MerchantBusinessResponse::from);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getById", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getById", e.getMessage()));
  }
}