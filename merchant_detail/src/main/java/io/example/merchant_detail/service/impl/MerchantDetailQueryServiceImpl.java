package io.example.merchant_detail.service.impl;

import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.example.common.domain.PagedResult;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_detail.model.MerchantDetailResponse;
import io.example.merchant_detail.model.MerchantDetailResponseDeleteAt;
import io.example.merchant_detail.model.MerchantDetailsRelation;
import io.example.merchant_detail.repository.MerchantDetailQueryRepository;
import io.example.merchant_detail.service.MerchantDetailQueryService;
import io.vertx.core.Future;
import io.vertx.core.json.Json;

public class MerchantDetailQueryServiceImpl implements MerchantDetailQueryService {
  private static final Logger log = LoggerFactory.getLogger(MerchantDetailQueryServiceImpl.class);

  private final MerchantDetailQueryRepository repository;
  private final RedisService redisService;
  private final TracingMetrics tracingMetrics;

  public MerchantDetailQueryServiceImpl(
      MerchantDetailQueryRepository repository,
      RedisService redisService,
      TracingMetrics tracingMetrics) {
    this.repository = repository;
    this.redisService = redisService;
    this.tracingMetrics = tracingMetrics;
  }

  @Override
  public Future<PagedResult<MerchantDetailResponse>> getMerchantDetails(String search, int page, int pageSize) {
    var ctx = tracingMetrics.startSpan("MerchantDetailQueryService.getMerchantDetails");
    log.info("Fetching all merchant details | search={}, page={}, pageSize={}", search, page, pageSize);

    return repository.getMerchantDetails(search, page, pageSize)
        .map(result -> {
          List<MerchantDetailResponse> data = result.getData().stream()
              .map(MerchantDetailResponse::from)
              .toList();
          tracingMetrics.completeSpanSuccess(ctx, "get_all", "Success");
          return new PagedResult<>(data, result.getTotalRecords());
        })
        .recover(err -> {
          log.error("Failed to fetch all merchant details", err);
          tracingMetrics.completeSpanError(ctx, "get_all", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<PagedResult<MerchantDetailResponse>> getMerchantDetailsActive(String search, int page, int pageSize) {
    var ctx = tracingMetrics.startSpan("MerchantDetailQueryService.getMerchantDetailsActive");
    log.info("Fetching active merchant details | search={}, page={}, pageSize={}", search, page, pageSize);

    return repository.getMerchantDetailsActive(search, page, pageSize)
        .map(result -> {
          List<MerchantDetailResponse> data = result.getData().stream()
              .map(MerchantDetailResponse::from)
              .toList();
          tracingMetrics.completeSpanSuccess(ctx, "get_active", "Success");
          return new PagedResult<>(data, result.getTotalRecords());
        })
        .recover(err -> {
          log.error("Failed to fetch active merchant details", err);
          tracingMetrics.completeSpanError(ctx, "get_active", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<PagedResult<MerchantDetailResponseDeleteAt>> getMerchantDetailsTrashed(String search, int page, int pageSize) {
    var ctx = tracingMetrics.startSpan("MerchantDetailQueryService.getMerchantDetailsTrashed");
    log.info("Fetching trashed merchant details | search={}, page={}, pageSize={}", search, page, pageSize);

    return repository.getMerchantDetailsTrashed(search, page, pageSize)
        .map(result -> {
          List<MerchantDetailResponseDeleteAt> data = result.getData().stream()
              .map(MerchantDetailResponseDeleteAt::from)
              .toList();
          tracingMetrics.completeSpanSuccess(ctx, "get_trashed", "Success");
          return new PagedResult<>(data, result.getTotalRecords());
        })
        .recover(err -> {
          log.error("Failed to fetch trashed merchant details", err);
          tracingMetrics.completeSpanError(ctx, "get_trashed", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<MerchantDetailResponse> getMerchantDetail(Long merchantDetailId) {
    var ctx = tracingMetrics.startSpan("MerchantDetailQueryService.getMerchantDetail");
    log.info("Fetching merchant detail by id: {}", merchantDetailId);
    String cacheKey = "merchant_detail:" + merchantDetailId;

    return redisService.get(cacheKey)
        .<MerchantDetailResponse>compose(cached -> {
          if (cached != null && !cached.isEmpty()) {
            try {
              MerchantDetailsRelation data = Json.decodeValue(cached, MerchantDetailsRelation.class);
              return Future.succeededFuture(MerchantDetailResponse.from(data));
            } catch (Exception e) {
              log.warn("Cache parse error: {}", e.getMessage());
            }
          }

          return repository.getMerchantDetail(merchantDetailId)
              .compose(data -> {
                if (data == null) {
                  return Future.failedFuture("Merchant Detail not found");
                }
                return redisService.set(cacheKey, Json.encode(data), Duration.ofMinutes(60))
                    .onFailure(err -> log.warn("Cache set failed: {}", err.getMessage()))
                    .map(v -> MerchantDetailResponse.from(data));
              });
        })
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "get_by_id", "Success"))
        .onFailure(err -> tracingMetrics.completeSpanError(ctx, "get_by_id", err.getMessage()));
  }
}
