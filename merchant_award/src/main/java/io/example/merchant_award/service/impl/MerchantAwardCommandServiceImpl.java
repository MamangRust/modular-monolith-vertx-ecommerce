package io.example.merchant_award.service.impl;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.NotFoundException;
import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_award.model.MerchantAward;
import io.example.merchant_award.model.MerchantAwardResponse;
import io.example.merchant_award.model.MerchantAwardResponseDeleteAt;
import io.example.merchant_award.repository.MerchantAwardCommandRepository;
import io.example.merchant_award.repository.MerchantQueryRepository;
import io.example.merchant_award.service.MerchantAwardCommandService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import pb.merchant_award.MerchantAwardCommand.CreateMerchantAwardRequest;
import pb.merchant_award.MerchantAwardCommand.UpdateMerchantAwardRequest;

public class MerchantAwardCommandServiceImpl implements MerchantAwardCommandService {
  private static final Logger logger = LoggerFactory.getLogger(MerchantAwardCommandServiceImpl.class);

  private final MerchantAwardCommandRepository repo;
  private final MerchantQueryRepository merchantRepo;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private static final String CACHE_PREFIX = "merchant_award:";

  public MerchantAwardCommandServiceImpl(
      MerchantAwardCommandRepository repo,
      MerchantQueryRepository merchantRepo,
      RedisService redis,
      TracingMetrics metrics) {
    this.repo = repo;
    this.merchantRepo = merchantRepo;
    this.redis = redis;
    this.metrics = metrics;
  }

  @Override
  public Future<ApiResponse<MerchantAwardResponse>> create(CreateMerchantAwardRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantAwardCommandService.create",
        Attributes.builder()
            .put("award.merchant_id", (long) req.getMerchantId())
            .put("award.title", req.getTitle())
            .build());
    Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

    logger.info("Creating award for merchant: {}", req.getMerchantId());

    return merchantRepo.findById(req.getMerchantId())
        .compose(exists -> {
          if (!exists) {
            return Future.failedFuture(new NotFoundException("Merchant not found with ID: " + req.getMerchantId()));
          }
          return repo.create(req);
        })
        .map(mca -> {
          if (mca == null) {
            throw new RuntimeException("Failed to create award");
          }
          span.setAttribute("award.id", mca.getMerchantCertificationId());
          metrics.completeSpanSuccess(tracingContext, "create", "Award created successfully");
          return ApiResponse.success("Award created successfully", MerchantAwardResponse.from(mca));
        })
        .recover(err -> {
          logger.error("Failed to create award", err);
          metrics.completeSpanError(tracingContext, "create", err.getMessage());
          if (err instanceof NotFoundException) {
            return Future.succeededFuture(ApiResponse.error(err.getMessage()));
          }
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponse<MerchantAwardResponse>> update(UpdateMerchantAwardRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantAwardCommandService.update",
        Attributes.builder()
            .put("award.id", (long) req.getMerchantCertificationId())
            .put("award.title", req.getTitle())
            .build());
    Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

    logger.info("Updating award: {}", req.getMerchantCertificationId());

    String cacheKey = CACHE_PREFIX + "id:" + req.getMerchantCertificationId();

    return repo.update(req)
        .compose(mca -> {
          if (mca == null) {
            return Future.failedFuture(new NotFoundException("Award not found with ID: " + req.getMerchantCertificationId()));
          }
          return redis.delete(cacheKey).map(mca);
        })
        .map(mca -> {
          metrics.completeSpanSuccess(tracingContext, "update", "Award updated successfully");
          return ApiResponse.success("Award updated successfully", MerchantAwardResponse.from(mca));
        })
        .recover(err -> {
          logger.error("Failed to update award", err);
          metrics.completeSpanError(tracingContext, "update", err.getMessage());
          if (err instanceof NotFoundException) {
            return Future.succeededFuture(ApiResponse.error(err.getMessage()));
          }
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponse<MerchantAwardResponseDeleteAt>> trash(Long id) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantAwardCommandService.trash",
        Attributes.builder()
            .put("award.id", id)
            .build());

    logger.info("Trashing award: {}", id);

    String cacheKey = CACHE_PREFIX + "id:" + id;

    return repo.trash(id)
        .compose(mca -> {
          if (mca == null) {
            return Future.failedFuture(new NotFoundException("Award not found with ID: " + id));
          }
          return redis.delete(cacheKey).map(mca);
        })
        .map(mca -> {
          metrics.completeSpanSuccess(tracingContext, "trash", "Award trashed successfully");
          return ApiResponse.success("Award trashed successfully", MerchantAwardResponseDeleteAt.from(mca));
        })
        .recover(err -> {
          logger.error("Failed to trash award", err);
          metrics.completeSpanError(tracingContext, "trash", err.getMessage());
          if (err instanceof NotFoundException) {
            return Future.succeededFuture(ApiResponse.error(err.getMessage()));
          }
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponse<MerchantAwardResponseDeleteAt>> restore(Long id) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantAwardCommandService.restore",
        Attributes.builder()
            .put("award.id", id)
            .build());

    logger.info("Restoring award: {}", id);

    String cacheKey = CACHE_PREFIX + "id:" + id;

    return repo.restore(id)
        .compose(mca -> {
          if (mca == null) {
            return Future.failedFuture(new NotFoundException("Award not found with ID: " + id));
          }
          return redis.delete(cacheKey).map(mca);
        })
        .map(mca -> {
          metrics.completeSpanSuccess(tracingContext, "restore", "Award restored successfully");
          return ApiResponse.success("Award restored successfully", MerchantAwardResponseDeleteAt.from(mca));
        })
        .recover(err -> {
          logger.error("Failed to restore award", err);
          metrics.completeSpanError(tracingContext, "restore", err.getMessage());
          if (err instanceof NotFoundException) {
            return Future.succeededFuture(ApiResponse.error(err.getMessage()));
          }
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponse<Boolean>> deletePermanent(Long id) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantAwardCommandService.deletePermanent",
        Attributes.builder()
            .put("award.id", id)
            .build());

    logger.info("Permanently deleting award: {}", id);

    String cacheKey = CACHE_PREFIX + "id:" + id;

    return repo.deletePermanent(id)
        .compose(success -> {
          if (!success) {
            return Future.failedFuture(new NotFoundException("Award not found with ID: " + id));
          }
          return redis.delete(cacheKey).map(true);
        })
        .map(success -> {
          metrics.completeSpanSuccess(tracingContext, "delete_permanent", "Award permanently deleted");
          return ApiResponse.success("Award permanently deleted", true);
        })
        .recover(err -> {
          logger.error("Failed to permanently delete award", err);
          metrics.completeSpanError(tracingContext, "delete_permanent", err.getMessage());
          if (err instanceof NotFoundException) {
            return Future.succeededFuture(ApiResponse.error(err.getMessage()));
          }
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponse<Integer>> restoreAll() {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan("MerchantAwardCommandService.restoreAll");

    logger.info("Restoring all trashed awards");

    return repo.restoreAll()
        .map(count -> {
          metrics.completeSpanSuccess(tracingContext, "restore_all", "All awards restored");
          return ApiResponse.success("All awards restored successfully", count);
        })
        .recover(err -> {
          logger.error("Failed to restore all awards", err);
          metrics.completeSpanError(tracingContext, "restore_all", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponse<Integer>> deleteAllPermanent() {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan("MerchantAwardCommandService.deleteAllPermanent");

    logger.info("Permanently deleting all trashed awards");

    return repo.deleteAllPermanent()
        .map(count -> {
          metrics.completeSpanSuccess(tracingContext, "delete_all_permanent", "All trashed awards deleted");
          return ApiResponse.success("All trashed awards deleted permanently", count);
        })
        .recover(err -> {
          logger.error("Failed to delete all trashed awards", err);
          metrics.completeSpanError(tracingContext, "delete_all_permanent", err.getMessage());
          return Future.failedFuture(err);
        });
  }
}
