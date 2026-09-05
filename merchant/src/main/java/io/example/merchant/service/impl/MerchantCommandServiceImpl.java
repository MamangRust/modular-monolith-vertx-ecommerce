package io.example.merchant.service.impl;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.KafkaService;
import io.example.common.service.RedisService;
import io.example.common.utils.EmailTemplate;
import io.example.merchant.model.MerchantResponse;
import io.example.merchant.model.MerchantResponseDeleteAt;
import io.example.merchant.repository.MerchantCommandRepository;
import io.example.merchant.repository.MerchantQueryRepository;
import io.example.merchant.repository.UserQueryRepository;
import io.example.merchant.domain.requests.CreateMerchantRequest;
import io.example.merchant.domain.requests.UpdateMerchantRequest;
import io.example.merchant.domain.requests.UpdateMerchantStatusRequest;
import io.example.merchant.service.MerchantCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantCommandServiceImpl implements MerchantCommandService {
  private static final Logger log = LoggerFactory.getLogger(MerchantCommandServiceImpl.class);

  private final MerchantCommandRepository repo;
  private final MerchantQueryRepository queryRepo;
  private final UserQueryRepository userRepo;
  private final RedisService redis;
  private final TracingMetrics metrics;
  private final KafkaService kafka;

  private static final String CACHE_PREFIX = "merchant:";

  private Future<Void> evict(Integer id) {
    return redis.delete(CACHE_PREFIX + "id:" + id).<Void>mapEmpty();
  }

  @Override
  public Future<MerchantResponse> createMerchant(CreateMerchantRequest request) {
    var ctx = metrics.startSpan("MerchantCommandService.createMerchant",
        Attributes.builder().put("merchant.user_id", request.getUserId()).build());

    return userRepo.getUserById(request.getUserId())
        .compose(user -> {
          CreateMerchantRequest repoRequest = CreateMerchantRequest.builder()
              .userId(request.getUserId())
              .name(request.getName())
              .description(request.getDescription())
              .address(request.getAddress())
              .contactEmail(request.getContactEmail())
              .contactPhone(request.getContactPhone())
              .status(request.getStatus())
              .build();
          return repo.createMerchant(repoRequest)
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
                    .map(v -> merchant)
                    .recover(err -> Future.succeededFuture(merchant));
              });
        })
        .map(MerchantResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "createMerchant", "Success"))
        .onFailure(e -> {
          log.error("Merchant creation failed for userId={} name={}", request.getUserId(), request.getName(), e);
          metrics.completeSpanError(ctx, "createMerchant", e.getMessage());
        });
  }

  @Override
  public Future<MerchantResponse> updateMerchant(UpdateMerchantRequest request) {
    var ctx = metrics.startSpan("MerchantCommandService.updateMerchant",
        Attributes.builder().put("merchant.id", (long) request.getMerchantId()).build());

    return repo.updateMerchant(request)
        .compose(merchant -> {
          if (merchant == null)
            return Future.failedFuture(new NotFoundException("Merchant not found"));
          return evict(merchant.getMerchantId()).map(merchant);
        })
        .map(MerchantResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "updateMerchant", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "updateMerchant", e.getMessage()));
  }

  @Override
  public Future<MerchantResponse> updateStatus(UpdateMerchantStatusRequest request) {
    var ctx = metrics.startSpan("MerchantCommandService.updateStatus",
        Attributes.builder().put("merchant.id", (long) request.getMerchantId()).build());

    return queryRepo.getMerchantById(request.getMerchantId().longValue())
        .compose(merchant -> {
          if (merchant == null)
            return Future.failedFuture(new NotFoundException("Merchant not found"));
          return userRepo.getUserById(merchant.getUserId())
              .compose(user -> {
                return repo.updateStatus(request)
                    .compose(updated -> {
                      String status = request.getStatus();
                      Integer merchantId = request.getMerchantId();

                      // Send merchant-status event to transaction module for cache invalidation
                      JsonObject statusPayload = new JsonObject()
                          .put("merchantId", merchantId)
                          .put("status", status)
                          .put("timestamp", System.currentTimeMillis());

                      kafka.sendMessage("transaction-service-topic-merchant-status-event",
                          String.valueOf(merchantId), statusPayload)
                          .onSuccess(v -> log.info("📤 Sent merchant status event to transaction-service"))
                          .onFailure(err -> log.error("❌ Failed to send merchant status event", err));

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
                          return evict(updated.getMerchantId()).map(v -> updated);
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
                          .compose(v -> evict(updated.getMerchantId()))
                          .map(v -> updated)
                          .recover(err -> evict(updated.getMerchantId()).map(v -> updated));
                    });
              });
        })
        .map(MerchantResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "updateStatus", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "updateStatus", e.getMessage()));
  }

  @Override
  public Future<MerchantResponseDeleteAt> trashMerchant(Long merchantId) {
    var ctx = metrics.startSpan("MerchantCommandService.trashMerchant",
        Attributes.builder().put("merchant.id", (long) merchantId).build());

    return repo.trashMerchant(merchantId)
        .compose(merchant -> {
          if (merchant == null)
            return Future.failedFuture(new NotFoundException("Merchant not found"));
          return evict(merchantId.intValue()).map(merchant);
        })
        .map(MerchantResponseDeleteAt::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "trashMerchant", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "trashMerchant", e.getMessage()));
  }

  @Override
  public Future<MerchantResponse> restoreMerchant(Long merchantId) {
    var ctx = metrics.startSpan("MerchantCommandService.restoreMerchant",
        Attributes.builder().put("merchant.id", (long) merchantId).build());

    return queryRepo.findByTrashedId(merchantId)
        .compose(merchant -> {
          if (merchant == null)
            return Future.failedFuture(new NotFoundException("Merchant not found"));
          return repo.restoreMerchant(merchantId);
        })
        .compose(merchant -> evict(merchantId.intValue()).map(merchant))
        .map(MerchantResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restoreMerchant", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "restoreMerchant", e.getMessage()));
  }

  @Override
  public Future<Void> deleteMerchantPermanently(Long merchantId) {
    var ctx = metrics.startSpan("MerchantCommandService.deletePermanent",
        Attributes.builder().put("merchant.id", (long) merchantId).build());

    return queryRepo.findByTrashedId(merchantId)
        .compose(merchant -> {
          if (merchant == null || merchant.getDeletedAt() == null) {
            return Future.<Void>failedFuture(
                new BadRequestException("Merchant not found or must be trashed before permanent deletion"));
          }
          return repo.deleteMerchantPermanently(merchantId)
              .compose(v -> evict(merchantId.intValue()));
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deletePermanent", "Merchant deleted permanently"))
        .onFailure(err -> metrics.completeSpanError(ctx, "deletePermanent", err.getMessage()));
  }

  @Override
  public Future<Void> restoreAllMerchants() {
    var ctx = metrics.startSpan("MerchantCommandService.restoreAll");

    return repo.restoreAllMerchants()
        .compose(count -> {
          if (count == 0) {
            return Future.<Void>failedFuture(new NotFoundException("No trashed merchants found"));
          }
          return redis.delete(CACHE_PREFIX + "list:*").<Void>mapEmpty();
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore_all", "Success"))
        .onFailure(err -> metrics.completeSpanError(ctx, "restore_all", err.getMessage()));
  }

  @Override
  public Future<Void> deleteAllPermanentMerchants() {
    var ctx = metrics.startSpan("MerchantCommandService.deleteAllPermanent");

    return repo.deleteAllPermanentMerchants()
        .compose(count -> {
          if (count == 0) {
            return Future.<Void>failedFuture(new NotFoundException("No trashed merchants found"));
          }
          return redis.delete(CACHE_PREFIX + "list:*").<Void>mapEmpty();
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "delete_all_permanent", "Success"))
        .onFailure(err -> metrics.completeSpanError(ctx, "delete_all_permanent", err.getMessage()));
  }
}