package io.example.merchant_policy.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_policy.domain.requests.CreateMerchantPoliciesRequest;
import io.example.merchant_policy.domain.requests.UpdateMerchantPoliciesRequest;
import io.example.merchant_policy.model.MerchantPoliciesResponse;
import io.example.merchant_policy.model.MerchantPoliciesResponseDeleteAt;
import io.example.merchant_policy.repository.MerchantPoliciesCommandRepository;
import io.example.merchant_policy.repository.MerchantPoliciesQueryRepository;
import io.example.merchant_policy.repository.MerchantQueryRepository;
import io.example.merchant_policy.service.MerchantPoliciesCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantPoliciesCommandServiceImpl implements MerchantPoliciesCommandService {
  private static final Logger logger = LoggerFactory.getLogger(MerchantPoliciesCommandServiceImpl.class);

  private final MerchantPoliciesCommandRepository repository;
  private final MerchantPoliciesQueryRepository queryRepository;
  private final MerchantQueryRepository merchantQueryRepository;
  private final RedisService redisService;
  private final TracingMetrics tracingMetrics;

  private static final String CACHE_PREFIX = "merchant_policy:";

  private Future<Void> evict(Integer id) {
    return redisService.delete(CACHE_PREFIX + "id:" + id).<Void>mapEmpty();
  }

  @Override
  public Future<MerchantPoliciesResponse> create(CreateMerchantPoliciesRequest req) {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesCommandService.create",
        Attributes.builder()
            .put("policy.merchant_id", (long) req.getMerchantId())
            .build());

    return merchantQueryRepository.existsById(req.getMerchantId())
        .compose(exists -> {
          if (!exists) {
            return Future.failedFuture(
                new NotFoundException("Parent Merchant profile with ID " + req.getMerchantId() + " does not exist!"));
          }
          return repository.create(req);
        })
        .map(MerchantPoliciesResponse::from)
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "create", "Success"))
        .onFailure(e -> tracingMetrics.completeSpanError(ctx, "create", e.getMessage()));
  }

  @Override
  public Future<MerchantPoliciesResponse> update(UpdateMerchantPoliciesRequest req) {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesCommandService.update",
        Attributes.builder()
            .put("policy.id", (long) req.getMerchantPolicyId())
            .build());

    return repository.update(req)
        .compose(data -> {
          if (data == null) {
            return Future.failedFuture(new NotFoundException("Policy not found with ID: " + req.getMerchantPolicyId()));
          }
          return evict(req.getMerchantPolicyId()).map(v -> data);
        })
        .map(MerchantPoliciesResponse::from)
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "update", "Success"))
        .onFailure(e -> tracingMetrics.completeSpanError(ctx, "update", e.getMessage()));
  }

  @Override
  public Future<MerchantPoliciesResponseDeleteAt> trash(Long id) {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesCommandService.trash",
        Attributes.builder().put("policy.id", (long) id).build());

    return repository.trash(id)
        .compose(data -> {
          if (data == null) {
            return Future.failedFuture(new NotFoundException("Policy not found with ID: " + id));
          }
          return evict(id.intValue()).map(v -> data);
        })
        .map(MerchantPoliciesResponseDeleteAt::from)
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "trash", "Success"))
        .onFailure(e -> tracingMetrics.completeSpanError(ctx, "trash", e.getMessage()));
  }

  @Override
  public Future<MerchantPoliciesResponseDeleteAt> restore(Long id) {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesCommandService.restore",
        Attributes.builder().put("policy.id", (long) id).build());

    logger.info("Restoring policy: {}", id);

    return queryRepository.findByTrashedId(id)
        .compose(trashed -> {
          if (trashed == null) {
            return Future.failedFuture(new BadRequestException("Policy not found or must be trashed first"));
          }
          return repository.restore(id);
        })
        .compose(r -> {
          if (r == null) {
            return Future.failedFuture(new NotFoundException("Policy not found"));
          }
          return evict(id.intValue()).map(v -> r);
        })
        .map(MerchantPoliciesResponseDeleteAt::from)
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "restore", "Success"))
        .onFailure(e -> {
          logger.error("Failed to restore policy", e);
          tracingMetrics.completeSpanError(ctx, "restore", e.getMessage());
        });
  }

  @Override
  public Future<Void> deletePermanent(Long id) {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesCommandService.deletePermanent",
        Attributes.builder().put("policy.id", (long) id).build());

    return queryRepository.findByTrashedId(id)
        .compose(trashed -> {
          if (trashed == null) {
            return Future.<Void>failedFuture(
                new BadRequestException("Policy not found or must be trashed before permanent deletion"));
          }
          return repository.deletePermanent(id)
              .compose(v -> evict(id.intValue()));
        })
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "deletePermanent", "Policy deleted permanently"))
        .onFailure(err -> tracingMetrics.completeSpanError(ctx, "deletePermanent", err.getMessage()));
  }

  @Override
  public Future<Void> restoreAll() {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesCommandService.restoreAll");

    return repository.restoreAll()
        .compose(count -> {
          if (count == 0) {
            return Future.<Void>failedFuture(new NotFoundException("No trashed policies found"));
          }
          return redisService.delete(CACHE_PREFIX + "list:*").<Void>mapEmpty();
        })
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "restore_all", "Success"))
        .onFailure(err -> tracingMetrics.completeSpanError(ctx, "restore_all", err.getMessage()));
  }

  @Override
  public Future<Void> deleteAllPermanent() {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesCommandService.deleteAllPermanent");

    return repository.deleteAllPermanent()
        .compose(count -> {
          if (count == 0) {
            return Future.<Void>failedFuture(new NotFoundException("No trashed policies found"));
          }
          return redisService.delete(CACHE_PREFIX + "list:*").<Void>mapEmpty();
        })
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "delete_all_permanent", "Success"))
        .onFailure(err -> tracingMetrics.completeSpanError(ctx, "delete_all_permanent", err.getMessage()));
  }
}