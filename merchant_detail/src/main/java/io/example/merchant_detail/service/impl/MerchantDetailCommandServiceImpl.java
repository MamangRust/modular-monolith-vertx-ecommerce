package io.example.merchant_detail.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.example.common.domain.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_detail.model.MerchantDetailResponse;
import io.example.merchant_detail.model.MerchantDetailResponseDeleteAt;
import io.example.merchant_detail.repository.MerchantDetailCommandRepository;
import io.example.merchant_detail.repository.MerchantQueryRepository;
import io.example.merchant_detail.service.MerchantDetailCommandService;
import io.vertx.core.Future;
import pb.merchant_detail.MerchantDetailCommand.CreateMerchantDetailRequest;
import pb.merchant_detail.MerchantDetailCommand.UpdateMerchantDetailRequest;

public class MerchantDetailCommandServiceImpl implements MerchantDetailCommandService {
  private static final Logger log = LoggerFactory.getLogger(MerchantDetailCommandServiceImpl.class);

  private final MerchantDetailCommandRepository repository;
  private final MerchantQueryRepository merchantQueryRepository;
  private final RedisService redisService;
  private final TracingMetrics tracingMetrics;

  public MerchantDetailCommandServiceImpl(
      MerchantDetailCommandRepository repository,
      MerchantQueryRepository merchantQueryRepository,
      RedisService redisService,
      TracingMetrics tracingMetrics) {
    this.repository = repository;
    this.merchantQueryRepository = merchantQueryRepository;
    this.redisService = redisService;
    this.tracingMetrics = tracingMetrics;
  }

  @Override
  public Future<ApiResponse<MerchantDetailResponse>> create(CreateMerchantDetailRequest req) {
    var ctx = tracingMetrics.startSpan("MerchantDetailCommandService.create");
    log.info("Creating merchant detail for merchantId: {}", req.getMerchantId());

    return merchantQueryRepository.findById(req.getMerchantId())
        .compose(exists -> {
          if (!exists) {
            return Future.failedFuture("Parent Merchant profile with ID " + req.getMerchantId() + " does not exist!");
          }
          return repository.create(req);
        })
        .map(data -> {
          tracingMetrics.completeSpanSuccess(ctx, "create", "Success");
          return ApiResponse.success("Merchant detail created successfully", MerchantDetailResponse.from(data));
        })
        .recover(err -> {
          log.error("Failed to create merchant detail", err);
          tracingMetrics.completeSpanError(ctx, "create", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<MerchantDetailResponse>> update(UpdateMerchantDetailRequest req) {
    var ctx = tracingMetrics.startSpan("MerchantDetailCommandService.update");
    log.info("Updating merchant detail id: {}", req.getMerchantDetailId());
    String cacheKey = "merchant_detail:" + req.getMerchantDetailId();

    return repository.update(req)
        .compose(data -> {
          if (data == null) {
            return Future.failedFuture("Merchant Detail not found or already deleted");
          }
          return redisService.delete(cacheKey).map(data);
        })
        .map(data -> {
          tracingMetrics.completeSpanSuccess(ctx, "update", "Success");
          return ApiResponse.success("Updated successfully", MerchantDetailResponse.from(data));
        })
        .recover(err -> {
          log.error("Failed to update merchant detail", err);
          tracingMetrics.completeSpanError(ctx, "update", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<MerchantDetailResponseDeleteAt>> trash(Long id) {
    var ctx = tracingMetrics.startSpan("MerchantDetailCommandService.trash");
    log.info("Trashing merchant detail id: {}", id);
    String cacheKey = "merchant_detail:" + id;

    return repository.trash(id)
        .compose(data -> {
          if (data == null) {
            return Future.failedFuture("Merchant Detail not found");
          }
          return redisService.delete(cacheKey).map(data);
        })
        .map(data -> {
          tracingMetrics.completeSpanSuccess(ctx, "trash", "Success");
          return ApiResponse.success("Trashed successfully", MerchantDetailResponseDeleteAt.from(data));
        })
        .recover(err -> {
          log.error("Failed to trash merchant detail", err);
          tracingMetrics.completeSpanError(ctx, "trash", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<MerchantDetailResponseDeleteAt>> restore(Long id) {
    var ctx = tracingMetrics.startSpan("MerchantDetailCommandService.restore");
    log.info("Restoring merchant detail id: {}", id);
    String cacheKey = "merchant_detail:" + id;

    return repository.restore(id)
        .compose(data -> {
          if (data == null) {
            return Future.failedFuture("Merchant Detail not found");
          }
          return redisService.delete(cacheKey).map(data);
        })
        .map(data -> {
          tracingMetrics.completeSpanSuccess(ctx, "restore", "Success");
          return ApiResponse.success("Restored successfully", MerchantDetailResponseDeleteAt.from(data));
        })
        .recover(err -> {
          log.error("Failed to restore merchant detail", err);
          tracingMetrics.completeSpanError(ctx, "restore", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Boolean>> deletePermanent(Long id) {
    var ctx = tracingMetrics.startSpan("MerchantDetailCommandService.deletePermanent");
    log.info("Deleting permanently merchant detail id: {}", id);
    String cacheKey = "merchant_detail:" + id;

    return repository.deletePermanent(id)
        .compose(v -> redisService.delete(cacheKey))
        .map(v -> {
          tracingMetrics.completeSpanSuccess(ctx, "delete_permanent", "Success");
          return ApiResponse.success("Deleted permanently successfully", true);
        })
        .recover(err -> {
          log.error("Failed to delete permanently merchant detail", err);
          tracingMetrics.completeSpanError(ctx, "delete_permanent", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Integer>> restoreAll() {
    var ctx = tracingMetrics.startSpan("MerchantDetailCommandService.restoreAll");
    log.info("Restoring all merchant details");

    return repository.restoreAll()
        .map(count -> {
          tracingMetrics.completeSpanSuccess(ctx, "restore_all", "Success");
          return ApiResponse.success("Restored all successfully", count);
        })
        .recover(err -> {
          log.error("Failed to restore all merchant details", err);
          tracingMetrics.completeSpanError(ctx, "restore_all", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Integer>> deleteAllPermanent() {
    var ctx = tracingMetrics.startSpan("MerchantDetailCommandService.deleteAllPermanent");
    log.info("Deleting all merchant details permanently");

    return repository.deleteAll()
        .map(count -> {
          tracingMetrics.completeSpanSuccess(ctx, "delete_all_permanent", "Success");
          return ApiResponse.success("Deleted all permanently successfully", count);
        })
        .recover(err -> {
          log.error("Failed to delete all merchant details permanently", err);
          tracingMetrics.completeSpanError(ctx, "delete_all_permanent", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }
}
