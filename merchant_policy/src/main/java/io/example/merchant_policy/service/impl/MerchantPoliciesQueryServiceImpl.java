package io.example.merchant_policy.service.impl;

import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.example.common.domain.PagedResult;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_policy.model.MerchantPoliciesRelationResponse;
import io.example.merchant_policy.model.MerchantPoliciesRelationResponseDeleteAt;
import io.example.merchant_policy.model.MerchantPoliciesResponse;
import io.example.merchant_policy.model.MerchantPolicy;
import io.example.merchant_policy.model.MerchantPolicyRelation;
import io.example.merchant_policy.repository.MerchantPoliciesQueryRepository;
import io.example.merchant_policy.service.MerchantPoliciesQueryService;
import io.vertx.core.Future;
import io.vertx.core.json.Json;

public class MerchantPoliciesQueryServiceImpl implements MerchantPoliciesQueryService {
  private static final Logger log = LoggerFactory.getLogger(MerchantPoliciesQueryServiceImpl.class);

  private final MerchantPoliciesQueryRepository repository;
  private final RedisService redisService;
  private final TracingMetrics tracingMetrics;
  private final ObjectMapper mapper = new ObjectMapper();

  public MerchantPoliciesQueryServiceImpl(
      MerchantPoliciesQueryRepository repository,
      RedisService redisService,
      TracingMetrics tracingMetrics) {
    this.repository = repository;
    this.redisService = redisService;
    this.tracingMetrics = tracingMetrics;
  }

  @Override
  public Future<PagedResult<MerchantPoliciesRelationResponse>> getMerchantPolicies(String search, int page, int pageSize) {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesQueryService.getMerchantPolicies");
    log.info("Fetching all policies | search={}, page={}, pageSize={}", search, page, pageSize);

    String cacheKey = String.format("merchant_policies:page:%d:search:%s", page, search != null ? search : "");

    return redisService.get(cacheKey)
        .<PagedResult<MerchantPolicyRelation>>compose(cached -> {
          if (cached != null && !cached.isEmpty()) {
            try {
              PagedResult<MerchantPolicyRelation> result = mapper.readValue(
                  cached,
                  new TypeReference<PagedResult<MerchantPolicyRelation>>() {}
              );
              return Future.succeededFuture(result);
            } catch (Exception e) {
              log.warn("Failed to parse merchant policies cache: {}", e.getMessage());
            }
          }
          return repository.getMerchantPolicies(search, page, pageSize)
              .compose(result -> redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                  .onFailure(err -> log.warn("Failed to set merchant policies cache: {}", err.getMessage()))
                  .map(v -> result));
        })
        .map(result -> {
          List<MerchantPoliciesRelationResponse> data = result.getData().stream()
              .map(MerchantPoliciesRelationResponse::from)
              .toList();
          tracingMetrics.completeSpanSuccess(ctx, "get_all", "Success");
          return new PagedResult<>(data, result.getTotalRecords());
        })
        .recover(err -> {
          log.error("Failed to fetch all policies", err);
          tracingMetrics.completeSpanError(ctx, "get_all", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<PagedResult<MerchantPoliciesRelationResponseDeleteAt>> getMerchantPoliciesActive(String search, int page, int pageSize) {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesQueryService.getMerchantPoliciesActive");
    log.info("Fetching active policies | search={}, page={}, pageSize={}", search, page, pageSize);

    String cacheKey = String.format("merchant_policies:active:page:%d:search:%s", page, search != null ? search : "");

    return redisService.get(cacheKey)
        .<PagedResult<MerchantPolicyRelation>>compose(cached -> {
          if (cached != null && !cached.isEmpty()) {
            try {
              PagedResult<MerchantPolicyRelation> result = mapper.readValue(
                  cached,
                  new TypeReference<PagedResult<MerchantPolicyRelation>>() {}
              );
              return Future.succeededFuture(result);
            } catch (Exception e) {
              log.warn("Failed to parse active merchant policies cache: {}", e.getMessage());
            }
          }
          return repository.getMerchantPoliciesActive(search, page, pageSize)
              .compose(result -> redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                  .onFailure(err -> log.warn("Failed to set active merchant policies cache: {}", err.getMessage()))
                  .map(v -> result));
        })
        .map(result -> {
          List<MerchantPoliciesRelationResponseDeleteAt> data = result.getData().stream()
              .map(MerchantPoliciesRelationResponseDeleteAt::from)
              .toList();
          tracingMetrics.completeSpanSuccess(ctx, "get_active", "Success");
          return new PagedResult<>(data, result.getTotalRecords());
        })
        .recover(err -> {
          log.error("Failed to fetch active policies", err);
          tracingMetrics.completeSpanError(ctx, "get_active", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<PagedResult<MerchantPoliciesRelationResponseDeleteAt>> getMerchantPoliciesTrashed(String search, int page, int pageSize) {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesQueryService.getMerchantPoliciesTrashed");
    log.info("Fetching trashed policies | search={}, page={}, pageSize={}", search, page, pageSize);

    String cacheKey = String.format("merchant_policies:trashed:page:%d:search:%s", page, search != null ? search : "");

    return redisService.get(cacheKey)
        .<PagedResult<MerchantPolicyRelation>>compose(cached -> {
          if (cached != null && !cached.isEmpty()) {
            try {
              PagedResult<MerchantPolicyRelation> result = mapper.readValue(
                  cached,
                  new TypeReference<PagedResult<MerchantPolicyRelation>>() {}
              );
              return Future.succeededFuture(result);
            } catch (Exception e) {
              log.warn("Failed to parse trashed merchant policies cache: {}", e.getMessage());
            }
          }
          return repository.getMerchantPoliciesTrashed(search, page, pageSize)
              .compose(result -> redisService.set(cacheKey, Json.encode(result), Duration.ofMinutes(10))
                  .onFailure(err -> log.warn("Failed to set trashed merchant policies cache: {}", err.getMessage()))
                  .map(v -> result));
        })
        .map(result -> {
          List<MerchantPoliciesRelationResponseDeleteAt> data = result.getData().stream()
              .map(MerchantPoliciesRelationResponseDeleteAt::from)
              .toList();
          tracingMetrics.completeSpanSuccess(ctx, "get_trashed", "Success");
          return new PagedResult<>(data, result.getTotalRecords());
        })
        .recover(err -> {
          log.error("Failed to fetch trashed policies", err);
          tracingMetrics.completeSpanError(ctx, "get_trashed", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<MerchantPoliciesResponse> getMerchantPolicy(Long id) {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesQueryService.getMerchantPolicy");
    log.info("Fetching policy by id: {}", id);
    String cacheKey = "merchant_policy:" + id;

    return redisService.get(cacheKey)
        .<MerchantPoliciesResponse>compose(cached -> {
          if (cached != null && !cached.isEmpty()) {
            try {
              MerchantPolicy data = Json.decodeValue(cached, MerchantPolicy.class);
              return Future.succeededFuture(MerchantPoliciesResponse.from(data));
            } catch (Exception e) {
              log.warn("Cache parse error: {}", e.getMessage());
            }
          }
          return repository.getMerchantPolicy(id)
              .compose(data -> {
                if (data == null) {
                  return Future.failedFuture("Policy not found");
                }
                return redisService.set(cacheKey, Json.encode(data), Duration.ofMinutes(60))
                    .onFailure(err -> log.warn("Cache set failed: {}", err.getMessage()))
                    .map(v -> MerchantPoliciesResponse.from(data));
              });
        })
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "get_by_id", "Success"))
        .onFailure(err -> tracingMetrics.completeSpanError(ctx, "get_by_id", err.getMessage()));
  }
}
