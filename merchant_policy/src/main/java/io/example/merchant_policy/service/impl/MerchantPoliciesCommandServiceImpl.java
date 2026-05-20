package io.example.merchant_policy.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.example.common.domain.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_policy.model.MerchantPoliciesResponse;
import io.example.merchant_policy.model.MerchantPoliciesResponseDeleteAt;
import io.example.merchant_policy.repository.MerchantPoliciesCommandRepository;
import io.example.merchant_policy.repository.MerchantQueryRepository;
import io.example.merchant_policy.service.MerchantPoliciesCommandService;
import io.vertx.core.Future;

public class MerchantPoliciesCommandServiceImpl implements MerchantPoliciesCommandService {
  private static final Logger log = LoggerFactory.getLogger(MerchantPoliciesCommandServiceImpl.class);

  private final MerchantPoliciesCommandRepository repository;
  private final MerchantQueryRepository merchantQueryRepository;
  private final RedisService redisService;
  private final TracingMetrics tracingMetrics;

  public MerchantPoliciesCommandServiceImpl(
      MerchantPoliciesCommandRepository repository,
      MerchantQueryRepository merchantQueryRepository,
      RedisService redisService,
      TracingMetrics tracingMetrics) {
    this.repository = repository;
    this.merchantQueryRepository = merchantQueryRepository;
    this.redisService = redisService;
    this.tracingMetrics = tracingMetrics;
  }

  @Override
  public Future<ApiResponse<MerchantPoliciesResponse>> create(pb.merchant_policy.MerchantPolicyCommand.CreateMerchantPoliciesRequest req) {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesCommandService.create");
    log.info("Creating policy for merchantId: {}", req.getMerchantId());

    return merchantQueryRepository.existsById(req.getMerchantId())
        .compose(exists -> {
          if (!exists) {
            return Future.failedFuture("Parent Merchant profile with ID " + req.getMerchantId() + " does not exist!");
          }
          return repository.create(req);
        })
        .map(data -> {
          tracingMetrics.completeSpanSuccess(ctx, "create", "Success");
          return ApiResponse.success("Policy created successfully", MerchantPoliciesResponse.from(data));
        })
        .recover(err -> {
          log.error("Failed to create policy", err);
          tracingMetrics.completeSpanError(ctx, "create", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<MerchantPoliciesResponse>> update(pb.merchant_policy.MerchantPolicyCommand.UpdateMerchantPoliciesRequest req) {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesCommandService.update");
    log.info("Updating policy id: {}", req.getMerchantPolicyId());
    String cacheKey = "merchant_policy:" + req.getMerchantPolicyId();

    return repository.update(req)
        .compose(data -> {
          if (data == null) {
            return Future.failedFuture("Policy not found or already deleted");
          }
          return redisService.delete(cacheKey).map(data);
        })
        .map(data -> {
          tracingMetrics.completeSpanSuccess(ctx, "update", "Success");
          return ApiResponse.success("Policy updated successfully", MerchantPoliciesResponse.from(data));
        })
        .recover(err -> {
          log.error("Failed to update policy", err);
          tracingMetrics.completeSpanError(ctx, "update", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<MerchantPoliciesResponseDeleteAt>> trash(Long id) {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesCommandService.trash");
    log.info("Trashing policy id: {}", id);
    String cacheKey = "merchant_policy:" + id;

    return repository.trash(id)
        .compose(data -> {
          if (data == null) {
            return Future.failedFuture("Policy not found");
          }
          return redisService.delete(cacheKey).map(data);
        })
        .map(data -> {
          tracingMetrics.completeSpanSuccess(ctx, "trash", "Success");
          return ApiResponse.success("Policy trashed successfully", MerchantPoliciesResponseDeleteAt.from(data));
        })
        .recover(err -> {
          log.error("Failed to trash policy", err);
          tracingMetrics.completeSpanError(ctx, "trash", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<MerchantPoliciesResponseDeleteAt>> restore(Long id) {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesCommandService.restore");
    log.info("Restoring policy id: {}", id);
    String cacheKey = "merchant_policy:" + id;

    return repository.restore(id)
        .compose(data -> {
          if (data == null) {
            return Future.failedFuture("Policy not found");
          }
          return redisService.delete(cacheKey).map(data);
        })
        .map(data -> {
          tracingMetrics.completeSpanSuccess(ctx, "restore", "Success");
          return ApiResponse.success("Policy restored successfully", MerchantPoliciesResponseDeleteAt.from(data));
        })
        .recover(err -> {
          log.error("Failed to restore policy", err);
          tracingMetrics.completeSpanError(ctx, "restore", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Boolean>> deletePermanent(Long id) {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesCommandService.deletePermanent");
    log.info("Deleting permanently policy id: {}", id);
    String cacheKey = "merchant_policy:" + id;

    return repository.deletePermanent(id)
        .compose(v -> redisService.delete(cacheKey))
        .map(v -> {
          tracingMetrics.completeSpanSuccess(ctx, "delete_permanent", "Success");
          return ApiResponse.success("Policy deleted permanently", true);
        })
        .recover(err -> {
          log.error("Failed to delete policy permanently", err);
          tracingMetrics.completeSpanError(ctx, "delete_permanent", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Integer>> restoreAll() {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesCommandService.restoreAll");
    log.info("Restoring all policies");

    return repository.restoreAll()
        .map(count -> {
          tracingMetrics.completeSpanSuccess(ctx, "restore_all", "Success");
          return ApiResponse.success("All policies restored successfully", count);
        })
        .recover(err -> {
          log.error("Failed to restore all policies", err);
          tracingMetrics.completeSpanError(ctx, "restore_all", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Integer>> deleteAllPermanent() {
    var ctx = tracingMetrics.startSpan("MerchantPoliciesCommandService.deleteAllPermanent");
    log.info("Deleting all policies permanently");

    return repository.deleteAllPermanent()
        .map(count -> {
          tracingMetrics.completeSpanSuccess(ctx, "delete_all_permanent", "Success");
          return ApiResponse.success("All policies deleted permanently", count);
        })
        .recover(err -> {
          log.error("Failed to delete all policies permanently", err);
          tracingMetrics.completeSpanError(ctx, "delete_all_permanent", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }
}
