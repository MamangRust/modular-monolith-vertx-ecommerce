package io.example.merchant_business.service.impl;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.NotFoundException;
import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_business.model.MerchantBusiness;
import io.example.merchant_business.model.MerchantBusinessResponse;
import io.example.merchant_business.model.MerchantBusinessResponseDeleteAt;
import io.example.merchant_business.repository.MerchantBusinessCommandRepository;
import io.example.merchant_business.repository.MerchantQueryRepository;
import io.example.merchant_business.service.MerchantBusinessCommandService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import pb.merchant_business.MerchantBusinessCommand.CreateMerchantBusinessRequest;
import pb.merchant_business.MerchantBusinessCommand.UpdateMerchantBusinessRequest;

public class MerchantBusinessCommandServiceImpl implements MerchantBusinessCommandService {
  private static final Logger logger = LoggerFactory.getLogger(MerchantBusinessCommandServiceImpl.class);

  private final MerchantBusinessCommandRepository repo;
  private final MerchantQueryRepository merchantRepo;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private static final String CACHE_PREFIX = "merchant_business:";

  public MerchantBusinessCommandServiceImpl(
      MerchantBusinessCommandRepository repo,
      MerchantQueryRepository merchantRepo,
      RedisService redis,
      TracingMetrics metrics) {
    this.repo = repo;
    this.merchantRepo = merchantRepo;
    this.redis = redis;
    this.metrics = metrics;
  }

  @Override
  public Future<ApiResponse<MerchantBusinessResponse>> create(CreateMerchantBusinessRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantBusinessCommandService.create",
        Attributes.builder()
            .put("business.merchant_id", (long) req.getMerchantId())
            .put("business.business_type", req.getBusinessType())
            .build());
    Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

    logger.info("Creating business info for merchant: {}", req.getMerchantId());

    return merchantRepo.findById(req.getMerchantId())
        .compose(exists -> {
          if (!exists) {
            return Future.failedFuture(new NotFoundException("Merchant not found with ID: " + req.getMerchantId()));
          }
          return repo.create(req);
        })
        .map(mbi -> {
          if (mbi == null) {
            throw new RuntimeException("Failed to create business info");
          }
          span.setAttribute("business.id", mbi.getMerchantBusinessInfoId());
          metrics.completeSpanSuccess(tracingContext, "create", "Business info created successfully");
          return ApiResponse.success("Business info created successfully", MerchantBusinessResponse.from(mbi));
        })
        .recover(err -> {
          logger.error("Failed to create business info", err);
          metrics.completeSpanError(tracingContext, "create", err.getMessage());
          if (err instanceof NotFoundException) {
            return Future.succeededFuture(ApiResponse.error(err.getMessage()));
          }
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponse<MerchantBusinessResponse>> update(UpdateMerchantBusinessRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantBusinessCommandService.update",
        Attributes.builder()
            .put("business.id", (long) req.getMerchantBusinessInfoId())
            .put("business.business_type", req.getBusinessType())
            .build());
    Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

    logger.info("Updating business info: {}", req.getMerchantBusinessInfoId());

    String cacheKey = CACHE_PREFIX + "id:" + req.getMerchantBusinessInfoId();

    return repo.update(req)
        .compose(mbi -> {
          if (mbi == null) {
            return Future.failedFuture(new NotFoundException("Business info not found with ID: " + req.getMerchantBusinessInfoId()));
          }
          return redis.delete(cacheKey).map(mbi);
        })
        .map(mbi -> {
          metrics.completeSpanSuccess(tracingContext, "update", "Business info updated successfully");
          return ApiResponse.success("Business info updated successfully", MerchantBusinessResponse.from(mbi));
        })
        .recover(err -> {
          logger.error("Failed to update business info", err);
          metrics.completeSpanError(tracingContext, "update", err.getMessage());
          if (err instanceof NotFoundException) {
            return Future.succeededFuture(ApiResponse.error(err.getMessage()));
          }
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponse<MerchantBusinessResponseDeleteAt>> trash(Long id) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantBusinessCommandService.trash",
        Attributes.builder()
            .put("business.id", id)
            .build());

    logger.info("Trashing business info: {}", id);

    String cacheKey = CACHE_PREFIX + "id:" + id;

    return repo.trash(id)
        .compose(mbi -> {
          if (mbi == null) {
            return Future.failedFuture(new NotFoundException("Business info not found with ID: " + id));
          }
          return redis.delete(cacheKey).map(mbi);
        })
        .map(mbi -> {
          metrics.completeSpanSuccess(tracingContext, "trash", "Business info trashed successfully");
          return ApiResponse.success("Business info trashed successfully", MerchantBusinessResponseDeleteAt.from(mbi));
        })
        .recover(err -> {
          logger.error("Failed to trash business info", err);
          metrics.completeSpanError(tracingContext, "trash", err.getMessage());
          if (err instanceof NotFoundException) {
            return Future.succeededFuture(ApiResponse.error(err.getMessage()));
          }
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponse<MerchantBusinessResponseDeleteAt>> restore(Long id) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantBusinessCommandService.restore",
        Attributes.builder()
            .put("business.id", id)
            .build());

    logger.info("Restoring business info: {}", id);

    String cacheKey = CACHE_PREFIX + "id:" + id;

    return repo.restore(id)
        .compose(mbi -> {
          if (mbi == null) {
            return Future.failedFuture(new NotFoundException("Business info not found with ID: " + id));
          }
          return redis.delete(cacheKey).map(mbi);
        })
        .map(mbi -> {
          metrics.completeSpanSuccess(tracingContext, "restore", "Business info restored successfully");
          return ApiResponse.success("Business info restored successfully", MerchantBusinessResponseDeleteAt.from(mbi));
        })
        .recover(err -> {
          logger.error("Failed to restore business info", err);
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
        "MerchantBusinessCommandService.deletePermanent",
        Attributes.builder()
            .put("business.id", id)
            .build());

    logger.info("Permanently deleting business info: {}", id);

    String cacheKey = CACHE_PREFIX + "id:" + id;

    return repo.deletePermanent(id)
        .compose(success -> {
          if (!success) {
            return Future.failedFuture(new NotFoundException("Business info not found with ID: " + id));
          }
          return redis.delete(cacheKey).map(true);
        })
        .map(success -> {
          metrics.completeSpanSuccess(tracingContext, "delete_permanent", "Business info permanently deleted");
          return ApiResponse.success("Business info permanently deleted", true);
        })
        .recover(err -> {
          logger.error("Failed to permanently delete business info", err);
          metrics.completeSpanError(tracingContext, "delete_permanent", err.getMessage());
          if (err instanceof NotFoundException) {
            return Future.succeededFuture(ApiResponse.error(err.getMessage()));
          }
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponse<Integer>> restoreAll() {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan("MerchantBusinessCommandService.restoreAll");

    logger.info("Restoring all trashed business info");

    return repo.restoreAll()
        .map(count -> {
          metrics.completeSpanSuccess(tracingContext, "restore_all", "All business info restored");
          return ApiResponse.success("All business info restored successfully", count);
        })
        .recover(err -> {
          logger.error("Failed to restore all business info", err);
          metrics.completeSpanError(tracingContext, "restore_all", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponse<Integer>> deleteAllPermanent() {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan("MerchantBusinessCommandService.deleteAllPermanent");

    logger.info("Permanently deleting all trashed business info");

    return repo.deleteAllPermanent()
        .map(count -> {
          metrics.completeSpanSuccess(tracingContext, "delete_all_permanent", "All trashed business info deleted");
          return ApiResponse.success("All trashed business info deleted permanently", count);
        })
        .recover(err -> {
          logger.error("Failed to delete all trashed business info", err);
          metrics.completeSpanError(tracingContext, "delete_all_permanent", err.getMessage());
          return Future.failedFuture(err);
        });
  }
}
