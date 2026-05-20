package io.example.merchant.service.impl;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.NotFoundException;
import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.KafkaService;
import io.example.common.service.RedisService;
import io.example.common.utils.EmailTemplate;
import io.example.merchant.model.MerchantResponse;
import io.example.merchant.model.MerchantResponseDeleteAt;
import io.example.merchant.repository.MerchantCommandRepository;
import io.example.merchant.repository.MerchantQueryRepository;
import io.example.merchant.repository.UserQueryRepository;
import io.example.merchant.service.MerchantCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import pb.merchant.MerchantCommand.CreateMerchantRequest;
import pb.merchant.MerchantCommand.UpdateMerchantRequest;
import pb.merchant.MerchantCommand.UpdateMerchantStatusRequest;

public class MerchantCommandServiceImpl implements MerchantCommandService {
  private static final Logger logger = LoggerFactory.getLogger(MerchantCommandServiceImpl.class);

  private final MerchantCommandRepository repo;
  private final MerchantQueryRepository queryRepo;
  private final UserQueryRepository userRepo;
  private final RedisService redis;
  private final TracingMetrics metrics;
  private final KafkaService kafka;

  private static final String CACHE_PREFIX = "merchant:";

  public MerchantCommandServiceImpl(
      MerchantCommandRepository repo,
      MerchantQueryRepository queryRepo,
      UserQueryRepository userRepo,
      RedisService redis,
      TracingMetrics metrics,
      KafkaService kafka) {
    this.repo = repo;
    this.queryRepo = queryRepo;
    this.userRepo = userRepo;
    this.redis = redis;
    this.metrics = metrics;
    this.kafka = kafka;
  }

  @Override
  public Future<ApiResponse<MerchantResponse>> createMerchant(CreateMerchantRequest request) {
    TracingMetrics.TracingContext ctx = metrics.startSpan("MerchantCommandService.createMerchant");
    return userRepo.getUserById(request.getUserId())
        .compose(user -> {
          String apiKey = "mc_" + UUID.randomUUID().toString().replace("-", "");
          return repo.createMerchant(request.getUserId(), request.getName(), apiKey, request.getStatus())
              .compose(merchant -> {
                // Send Email via Kafka
                String htmlBody = EmailTemplate.generateHtml(Map.of(
                    "Title", "Welcome to SanEdge Merchant Portal",
                    "Message",
                    "Your merchant account has been created successfully. To continue, please upload the required documents for verification. Once completed, our team will review and activate your account.",
                    "Button", "Upload Documents",
                    "Link", String.format("https://sanedge.example.com/merchant/%d/documents", user.getId())));

                JsonObject emailPayload = new JsonObject()
                    .put("email", user.getEmail())
                    .put("subject", "Initial Verification - SanEdge")
                    .put("body", htmlBody);

                return kafka.sendMessage("email-service-topic-merchant-create",
                    String.valueOf(merchant.getMerchantId()), emailPayload)
                    .map(v -> ApiResponse.success("Merchant created successfully", MerchantResponse.from(merchant)))
                    .recover(err -> {
                      metrics.completeSpanSuccess(ctx, "createMerchant", "Success (email failed)");
                      return Future.succeededFuture(
                          ApiResponse.success("Merchant created successfully", MerchantResponse.from(merchant)));
                    });
              });
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "createMerchant", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "createMerchant", e.getMessage()));
  }

  @Override
  public Future<ApiResponse<MerchantResponse>> updateMerchant(UpdateMerchantRequest request) {
    TracingMetrics.TracingContext ctx = metrics.startSpan("MerchantCommandService.updateMerchant");
    return repo.updateMerchant(request.getMerchantId(), request.getName(), request.getStatus())
        .compose(merchant -> {
          if (merchant == null)
            return Future.failedFuture("Merchant not found");
          return redis.delete(CACHE_PREFIX + "id:" + merchant.getMerchantId())
              .map(v -> ApiResponse.success("Merchant updated successfully", MerchantResponse.from(merchant)));
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "updateMerchant", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "updateMerchant", e.getMessage()));
  }

  @Override
  public Future<ApiResponse<MerchantResponse>> updateStatus(UpdateMerchantStatusRequest request) {
    TracingMetrics.TracingContext ctx = metrics.startSpan("MerchantCommandService.updateStatus");
    return queryRepo.getMerchantById(request.getMerchantId())
        .compose(merchant -> {
          if (merchant == null)
            return Future.failedFuture("Merchant not found");
          return userRepo.getUserById(merchant.getUserId())
              .compose(user -> {
                return repo.updateStatus(request.getMerchantId(), request.getStatus())
                    .compose(updated -> {
                      String status = request.getStatus();
                      String subject = "";
                      String message = "";
                      String link = String.format("https://sanedge.example.com/merchant/%d/dashboard",
                          request.getMerchantId());

                      switch (status) {
                        case "active" -> {
                          subject = "Your Merchant Account is Now Active";
                          message = "Congratulations! Your merchant account has been verified and is now <b>active</b>. You can now fully access all features in the SanEdge Merchant Portal.";
                        }
                        case "inactive" -> {
                          subject = "Merchant Account Set to Inactive";
                          message = "Your merchant account status has been set to <b>inactive</b>. Please contact support if you believe this is a mistake.";
                        }
                        case "rejected" -> {
                          subject = "Merchant Account Rejected";
                          message = "We're sorry to inform you that your merchant account has been <b>rejected</b>. Please contact support or review your submissions.";
                        }
                        default -> {
                          return Future.succeededFuture(updated);
                        }
                      }

                      String htmlBody = EmailTemplate.generateHtml(Map.of(
                          "Title", subject,
                          "Message", message,
                          "Button", "Go to Portal",
                          "Link", link));

                      JsonObject emailPayload = new JsonObject()
                          .put("email", user.getEmail())
                          .put("subject", subject)
                          .put("body", htmlBody);

                      return kafka.sendMessage("email-service-topic-merchant-update-status",
                          String.valueOf(request.getMerchantId()), emailPayload)
                          .compose(v -> redis.delete(CACHE_PREFIX + "id:" + updated.getMerchantId()))
                          .map(v -> updated)
                          .recover(err -> {
                            return redis.delete(CACHE_PREFIX + "id:" + updated.getMerchantId()).map(v -> updated);
                          });
                    });
              })
              .map(updated -> ApiResponse.success("Merchant status updated successfully",
                  MerchantResponse.from(updated)));
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "updateStatus", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "updateStatus", e.getMessage()));
  }

  @Override
  public Future<ApiResponse<MerchantResponseDeleteAt>> trashMerchant(Integer merchantId) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantCommandService.trashMerchant",
        Attributes.builder()
            .put("merchant.id", (long) merchantId)
            .build());

    logger.info("Trashing merchant: {}", merchantId);

    return repo.trashMerchant(merchantId)
        .compose(merchant -> {
          if (merchant == null) {
            return Future.failedFuture(new NotFoundException("Merchant not found with ID: " + merchantId));
          }
          String cacheKey = CACHE_PREFIX + "id:" + merchantId;
          return redis.delete(cacheKey)
              .onSuccess(deleted -> {
                if (deleted > 0) {
                  logger.debug("Merchant {} cache invalidated on trash", merchantId);
                }
              })
              .onFailure(err -> logger.warn("Failed to invalidate cache for trashed merchant {}: {}", merchantId,
                  err.getMessage()))
              .map(merchant);
        })
        .map(merchant -> {
          metrics.completeSpanSuccess(tracingContext, "trash", "Merchant trashed successfully");
          return ApiResponse.success("Merchant trashed successfully", MerchantResponseDeleteAt.from(merchant));
        })
        .recover(err -> {
          logger.error("Failed to trash merchant: {}", merchantId, err);
          metrics.completeSpanError(tracingContext, "trash", err.getMessage());
          return Future.succeededFuture(
              ApiResponse.error("Failed to trash merchant: " + err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<MerchantResponse>> restoreMerchant(Integer merchantId) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantCommandService.restoreMerchant",
        Attributes.builder()
            .put("merchant.id", (long) merchantId)
            .build());

    logger.info("Restoring merchant: {}", merchantId);

    return repo.restoreMerchant(merchantId)
        .compose(merchant -> {
          if (merchant == null) {
            return Future.failedFuture(new NotFoundException("Merchant not found with ID: " + merchantId));
          }
          String cacheKey = CACHE_PREFIX + "id:" + merchantId;
          return redis.delete(cacheKey)
              .onSuccess(deleted -> {
                if (deleted > 0) {
                  logger.debug("Merchant {} cache invalidated on restore", merchantId);
                }
              })
              .onFailure(err -> logger.warn("Failed to invalidate cache for restored merchant {}: {}", merchantId,
                  err.getMessage()))
              .map(merchant);
        })
        .map(merchant -> {
          metrics.completeSpanSuccess(tracingContext, "restore", "Merchant restored successfully");
          return ApiResponse.success("Merchant restored successfully", MerchantResponse.from(merchant));
        })
        .recover(err -> {
          logger.error("Failed to restore merchant: {}", merchantId, err);
          metrics.completeSpanError(tracingContext, "restore", err.getMessage());
          return Future.succeededFuture(
              ApiResponse.error("Failed to restore merchant: " + err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Void>> deleteMerchantPermanently(Integer merchantId) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantCommandService.deletePermanent",
        Attributes.builder()
            .put("merchant.id", (long) merchantId)
            .build());

    logger.info("Permanently deleting merchant: {}", merchantId);

    return repo.deleteMerchantPermanently(merchantId)
        .compose(v -> {
          String cacheKey = CACHE_PREFIX + "id:" + merchantId;
          return redis.delete(cacheKey)
              .onSuccess(deleted -> {
                if (deleted > 0) {
                  logger.debug("Merchant {} cache invalidated on permanent delete", merchantId);
                }
              })
              .onFailure(err -> logger.warn("Failed to invalidate cache for deleted merchant {}: {}", merchantId,
                  err.getMessage()))
              .map(v);
        })
        .map(v -> {
          logger.info("Merchant deleted permanently: {}", merchantId);
          metrics.completeSpanSuccess(tracingContext, "deletePermanent", "Merchant deleted permanently");
          return ApiResponse.<Void>success("Merchant deleted permanently", null);
        })
        .recover(err -> {
          logger.error("Failed to deletePermanent merchant: {}", merchantId, err);
          metrics.completeSpanError(tracingContext, "deletePermanent", err.getMessage());
          return Future.succeededFuture(
              ApiResponse.error("Failed to delete merchant: " + err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Void>> restoreAllMerchants() {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan("MerchantCommandService.restoreAll");

    logger.info("Restoring all trashed merchants");

    return repo.restoreAllMerchants()
        .compose(v -> {
          logger.info("All merchants restored successfully");
          metrics.completeSpanSuccess(tracingContext, "restore_all", "All merchants restored");
          return Future.succeededFuture(
              ApiResponse.<Void>success("All merchants restored successfully"));
        })
        .recover(err -> {
          logger.error("Failed to restore all merchants", err);
          metrics.completeSpanError(tracingContext, "restore_all", err.getMessage());
          return Future.succeededFuture(
              ApiResponse.error("Failed to restore all merchants: " + err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Void>> deleteAllPermanentMerchants() {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan("MerchantCommandService.deleteAllPermanent");

    logger.info("Permanently deleting all trashed merchants");

    return repo.deleteAllPermanentMerchants()
        .compose(v -> {
          logger.info("All trashed merchants permanently deleted");
          metrics.completeSpanSuccess(tracingContext, "deleteAllPermanent",
              "All trashed merchants permanently deleted");
          return Future.succeededFuture(
              ApiResponse.<Void>success("All trashed merchants permanently deleted successfully"));
        })
        .recover(err -> {
          logger.error("Failed to permanently delete all merchants", err);
          metrics.completeSpanError(tracingContext, "deleteAllPermanent", err.getMessage());
          return Future.succeededFuture(
              ApiResponse.error("Failed to permanently delete all merchants: " + err.getMessage()));
        });
  }
}
