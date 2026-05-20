package io.example.merchant_detail.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.example.common.domain.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_detail.model.MerchantSocialMediaLinkResponse;
import io.example.merchant_detail.repository.MerchantSocialLinkCommandRepository;
import io.example.merchant_detail.service.MerchantSocialLinkCommandService;
import io.vertx.core.Future;
import pb.MerchantSocialLinkCommand.CreateMerchantSocialRequest;
import pb.MerchantSocialLinkCommand.UpdateMerchantSocialRequest;

public class MerchantSocialLinkCommandServiceImpl implements MerchantSocialLinkCommandService {
  private static final Logger log = LoggerFactory.getLogger(MerchantSocialLinkCommandServiceImpl.class);

  private final MerchantSocialLinkCommandRepository repository;
  private final RedisService redisService;
  private final TracingMetrics tracingMetrics;

  public MerchantSocialLinkCommandServiceImpl(
      MerchantSocialLinkCommandRepository repository,
      RedisService redisService,
      TracingMetrics tracingMetrics) {
    this.repository = repository;
    this.redisService = redisService;
    this.tracingMetrics = tracingMetrics;
  }

  @Override
  public Future<ApiResponse<MerchantSocialMediaLinkResponse>> create(CreateMerchantSocialRequest req) {
    var ctx = tracingMetrics.startSpan("MerchantSocialLinkCommandService.create");
    log.info("Creating social link for merchant detail: {}", req.getMerchantDetailId());
    String parentCacheKey = "merchant_detail:" + req.getMerchantDetailId();

    return repository.create(req)
        .compose(data -> redisService.delete(parentCacheKey).map(data))
        .map(data -> {
          tracingMetrics.completeSpanSuccess(ctx, "create", "Success");
          return ApiResponse.success("Social link created", MerchantSocialMediaLinkResponse.from(data));
        })
        .recover(err -> {
          log.error("Failed to create social link", err);
          tracingMetrics.completeSpanError(ctx, "create", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<MerchantSocialMediaLinkResponse>> update(UpdateMerchantSocialRequest req) {
    var ctx = tracingMetrics.startSpan("MerchantSocialLinkCommandService.update");
    log.info("Updating social link: {}", req.getId());

    return repository.update(req)
        .compose(data -> {
          if (data == null) {
            return Future.failedFuture("Social link not found");
          }
          String parentCacheKey = "merchant_detail:" + data.getMerchantDetailId();
          return redisService.delete(parentCacheKey).map(data);
        })
        .map(data -> {
          tracingMetrics.completeSpanSuccess(ctx, "update", "Success");
          return ApiResponse.success("Social link updated", MerchantSocialMediaLinkResponse.from(data));
        })
        .recover(err -> {
          log.error("Failed to update social link", err);
          tracingMetrics.completeSpanError(ctx, "update", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Boolean>> trash(Long id) {
    var ctx = tracingMetrics.startSpan("MerchantSocialLinkCommandService.trash");
    log.info("Trashing social link: {}", id);

    return repository.trash(id)
        .compose(data -> {
          if (data == null) {
            return Future.failedFuture("Social link not found");
          }
          String parentCacheKey = "merchant_detail:" + data.getMerchantDetailId();
          return redisService.delete(parentCacheKey);
        })
        .map(v -> {
          tracingMetrics.completeSpanSuccess(ctx, "trash", "Success");
          return ApiResponse.success("Social link trashed successfully", true);
        })
        .recover(err -> {
          log.error("Failed to trash social link", err);
          tracingMetrics.completeSpanError(ctx, "trash", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Boolean>> restore(Long id) {
    var ctx = tracingMetrics.startSpan("MerchantSocialLinkCommandService.restore");
    log.info("Restoring social link: {}", id);

    return repository.restore(id)
        .compose(data -> {
          if (data == null) {
            return Future.failedFuture("Social link not found");
          }
          String parentCacheKey = "merchant_detail:" + data.getMerchantDetailId();
          return redisService.delete(parentCacheKey);
        })
        .map(v -> {
          tracingMetrics.completeSpanSuccess(ctx, "restore", "Success");
          return ApiResponse.success("Social link restored successfully", true);
        })
        .recover(err -> {
          log.error("Failed to restore social link", err);
          tracingMetrics.completeSpanError(ctx, "restore", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Boolean>> deletePermanent(Long id) {
    var ctx = tracingMetrics.startSpan("MerchantSocialLinkCommandService.deletePermanent");
    log.info("Deleting social link permanently: {}", id);

    return repository.deletePermanent(id)
        .map(v -> {
          tracingMetrics.completeSpanSuccess(ctx, "delete_permanent", "Success");
          return ApiResponse.success("Social link deleted permanently successfully", true);
        })
        .recover(err -> {
          log.error("Failed to delete social link permanently", err);
          tracingMetrics.completeSpanError(ctx, "delete_permanent", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Integer>> restoreAll() {
    var ctx = tracingMetrics.startSpan("MerchantSocialLinkCommandService.restoreAll");
    log.info("Restoring all social links");

    return repository.restoreAll()
        .map(count -> {
          tracingMetrics.completeSpanSuccess(ctx, "restore_all", "Success");
          return ApiResponse.success("Restored all successfully", count);
        })
        .recover(err -> {
          log.error("Failed to restore all social links", err);
          tracingMetrics.completeSpanError(ctx, "restore_all", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Integer>> deleteAllPermanent() {
    var ctx = tracingMetrics.startSpan("MerchantSocialLinkCommandService.deleteAllPermanent");
    log.info("Deleting all social links permanently");

    return repository.deleteAll()
        .map(count -> {
          tracingMetrics.completeSpanSuccess(ctx, "delete_all_permanent", "Success");
          return ApiResponse.success("Deleted all permanently successfully", count);
        })
        .recover(err -> {
          log.error("Failed to delete all social links permanently", err);
          tracingMetrics.completeSpanError(ctx, "delete_all_permanent", err.getMessage());
          return Future.succeededFuture(ApiResponse.error(err.getMessage()));
        });
  }
}
