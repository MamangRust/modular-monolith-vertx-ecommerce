package io.example.merchant.service.impl;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.NotFoundException;
import io.example.common.model.ApiResponse;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.KafkaService;
import io.example.common.service.RedisService;
import io.example.common.utils.EmailTemplate;
import io.example.merchant.model.MerchantDocument;
import io.example.merchant.model.MerchantDocumentResponse;
import io.example.merchant.repository.MerchantDocumentCommandRepository;
import io.example.merchant.repository.MerchantQueryRepository;
import io.example.merchant.repository.UserQueryRepository;
import io.example.merchant.service.MerchantDocumentCommandService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import pb.merchant_document.MerchantDocumentCommand.CreateMerchantDocumentRequest;
import pb.merchant_document.MerchantDocumentCommand.UpdateMerchantDocumentRequest;
import pb.merchant_document.MerchantDocumentCommand.UpdateMerchantDocumentStatusRequest;

public class MerchantDocumentCommandServiceImpl implements MerchantDocumentCommandService {
  private static final Logger logger = LoggerFactory.getLogger(MerchantDocumentCommandServiceImpl.class);

  private final MerchantDocumentCommandRepository repo;
  private final MerchantQueryRepository merchantRepo;
  private final UserQueryRepository userRepo;
  private final RedisService redis;
  private final TracingMetrics metrics;
  private final KafkaService kafka;

  private static final String CACHE_PREFIX = "merchant_document:";

  public MerchantDocumentCommandServiceImpl(
      MerchantDocumentCommandRepository repo,
      MerchantQueryRepository merchantRepo,
      UserQueryRepository userRepo,
      RedisService redis,
      TracingMetrics metrics,
      KafkaService kafka) {
    this.repo = repo;
    this.merchantRepo = merchantRepo;
    this.userRepo = userRepo;
    this.redis = redis;
    this.metrics = metrics;
    this.kafka = kafka;
  }

  @Override
  public Future<ApiResponse<MerchantDocumentResponse>> createDocument(CreateMerchantDocumentRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantDocumentCommandService.createDocument",
        Attributes.builder()
            .put("document.merchant_id", (long) req.getMerchantId())
            .put("document.type", Objects.requireNonNull(req.getDocumentType()))
            .build());
    Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

    logger.info("Creating merchant document for merchant: {}, type: {}", req.getMerchantId(), req.getDocumentType());

    return merchantRepo.getMerchantById(req.getMerchantId())
        .compose(merchant -> {
          if (merchant == null) {
            return Future.failedFuture(new NotFoundException("Merchant not found with ID: " + req.getMerchantId()));
          }
          return userRepo.getUserById(merchant.getUserId())
              .compose(user -> {
                return repo.createDocument(req.getMerchantId(), req.getDocumentType(), req.getDocumentUrl())
                    .compose(doc -> {
                      String htmlBody = EmailTemplate.generateHtml(Map.of(
                          "Title", "Welcome to SanEdge Merchant Portal",
                          "Message",
                          "Thank you for registering your merchant account. Your account is currently <b>inactive</b> and under initial review. To proceed, please upload all required documents for verification. Once your documents are submitted, our team will review them and activate your account accordingly.",
                          "Button", "Upload Documents",
                          "Link", String.format("https://sanedge.example.com/merchant/%d/documents", user.getId())));

                      JsonObject emailPayload = new JsonObject()
                          .put("email", user.getEmail())
                          .put("subject", "Merchant Verification Pending - Action Required")
                          .put("body", htmlBody);

                      return kafka.sendMessage("email-service-topic-merchant-document-create",
                          String.valueOf(doc.getDocumentId()), emailPayload)
                          .map(v -> doc)
                          .recover(err -> {
                            metrics.completeSpanSuccess(tracingContext, "create", "Success (email failed)");
                            return Future.succeededFuture(doc);
                          });
                    });
              });
        })
        .map(created -> {
          span.setAttribute("document.id", (long) created.getDocumentId());
          metrics.completeSpanSuccess(tracingContext, "create", "Merchant document created successfully");
          return ApiResponse.success(
              "Merchant document created successfully",
              MerchantDocumentResponse.from(created));
        })
        .recover(err -> {
          logger.error("Failed to create merchant document for merchant: {}", req.getMerchantId(), err);
          metrics.completeSpanError(tracingContext, "create", err.getMessage());
          return Future.succeededFuture(
              ApiResponse.error("Failed to create merchant document: " + err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<MerchantDocumentResponse>> updateDocument(UpdateMerchantDocumentRequest req) {
    Integer docId = req.getDocumentId();
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantDocumentCommandService.updateDocument",
        Attributes.builder()
            .put("document.id", (long) docId)
            .put("document.merchant_id", (long) req.getMerchantId())
            .build());

    logger.info("Updating document: {}, type: {}", docId, req.getDocumentType());

    return repo
        .updateDocument(docId, req.getMerchantId(), req.getDocumentType(), req.getDocumentUrl(), req.getNote(),
            req.getStatus())
        .compose((MerchantDocument updated) -> {
          if (updated == null) {
            return Future.failedFuture(new NotFoundException("Document not found"));
          }
          String cacheKey = CACHE_PREFIX + "id:" + docId;
          return redis.delete(cacheKey)
              .onSuccess(deleted -> {
                if (deleted > 0) {
                  logger.debug("Document {} cache invalidated", docId);
                }
              })
              .onFailure(err -> logger.warn("Failed to invalidate cache for document {}: {}", docId, err.getMessage()))
              .map(updated);
        })
        .map((MerchantDocument updated) -> {
          metrics.completeSpanSuccess(tracingContext, "update", "Document updated successfully");
          return ApiResponse.success(
              "Document updated successfully",
              MerchantDocumentResponse.from(updated));
        })
        .recover(err -> {
          logger.error("Failed to update document: {}", docId, err);
          metrics.completeSpanError(tracingContext, "update", err.getMessage());
          return Future.succeededFuture(
              ApiResponse.error("Failed to update document: " + err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<MerchantDocumentResponse>> updateStatus(UpdateMerchantDocumentStatusRequest req) {
    Integer docId = req.getDocumentId();
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantDocumentCommandService.updateStatus",
        Attributes.builder()
            .put("document.id", (long) docId)
            .put("document.status", Objects.requireNonNull(req.getStatus()))
            .build());

    logger.info("Updating document status: {}, status: {}", docId, req.getStatus());

    return repo.updateStatus(docId, req.getNote(), req.getStatus())
        .compose((MerchantDocument updated) -> {
          if (updated == null) {
            return Future.failedFuture(new NotFoundException("Document not found"));
          }
          return merchantRepo.getMerchantById(updated.getMerchantId())
              .compose(merchant -> {
                if (merchant == null) {
                  return Future.succeededFuture(updated);
                }
                return userRepo.getUserById(merchant.getUserId())
                    .compose(user -> {
                      String status = req.getStatus();
                      String subject = "";
                      String message = "";
                      String link = String.format("https://sanedge.example.com/merchant/%d/documents",
                          user.getId());

                      switch (status) {
                        case "approved" -> {
                          subject = "Merchant Document Approved";
                          message = String.format(
                              "Your submitted document of type <b>%s</b> has been successfully <b>approved</b>.",
                              updated.getDocumentType());
                        }
                        case "rejected" -> {
                          subject = "Merchant Document Rejected";
                          message = String.format(
                              "We're sorry to inform you that your submitted document of type <b>%s</b> has been <b>rejected</b>. Reason/Note: %s. Please review and re-submit the required documents.",
                              updated.getDocumentType(), req.getNote());
                        }
                        default -> {
                          return Future.succeededFuture(updated);
                        }
                      }

                      String htmlBody = EmailTemplate.generateHtml(Map.of(
                          "Title", subject,
                          "Message", message,
                          "Button", "Review Documents",
                          "Link", link));

                      JsonObject emailPayload = new JsonObject()
                          .put("email", user.getEmail())
                          .put("subject", subject)
                          .put("body", htmlBody);

                      return kafka.sendMessage("email-service-topic-merchant-document-update-status",
                          String.valueOf(updated.getDocumentId()), emailPayload)
                          .compose(v -> redis.delete(CACHE_PREFIX + "id:" + docId))
                          .map(v -> updated)
                          .recover(err -> {
                            return redis.delete(CACHE_PREFIX + "id:" + docId).map(v -> updated);
                          });
                    });
              });
        })
        .map((MerchantDocument updated) -> {
          metrics.completeSpanSuccess(tracingContext, "update_status", "Document status updated successfully");
          return ApiResponse.success(
              "Document status updated successfully",
              MerchantDocumentResponse.from(updated));
        })
        .recover(err -> {
          logger.error("Failed to update document status: {}", docId, err);
          metrics.completeSpanError(tracingContext, "update_status", err.getMessage());
          return Future.succeededFuture(
              ApiResponse.error("Failed to update document status: " + err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<MerchantDocumentResponse>> trashDocument(Integer documentId) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantDocumentCommandService.trashDocument",
        Attributes.builder()
            .put("document.id", (long) documentId)
            .build());

    logger.info("Trashing document: {}", documentId);

    return repo.trashDocument(documentId)
        .compose(doc -> {
          if (doc == null) {
            return Future.failedFuture(new NotFoundException("Document not found with ID: " + documentId));
          }
          String cacheKey = CACHE_PREFIX + "id:" + documentId;
          return redis.delete(cacheKey)
              .onSuccess(deleted -> {
                if (deleted > 0) {
                  logger.debug("Document {} cache invalidated on trash", documentId);
                }
              })
              .onFailure(err -> logger.warn("Failed to invalidate cache for trashed document {}: {}", documentId,
                  err.getMessage()))
              .map(doc);
        })
        .map(doc -> {
          metrics.completeSpanSuccess(tracingContext, "trash", "Document trashed successfully");
          return ApiResponse.success("Document trashed successfully", MerchantDocumentResponse.from(doc));
        })
        .recover(err -> {
          logger.error("Failed to trash document: {}", documentId, err);
          metrics.completeSpanError(tracingContext, "trash", err.getMessage());
          return Future.succeededFuture(
              ApiResponse.error("Failed to trash document: " + err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<MerchantDocumentResponse>> restoreDocument(Integer documentId) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantDocumentCommandService.restoreDocument",
        Attributes.builder()
            .put("document.id", (long) documentId)
            .build());

    logger.info("Restoring document: {}", documentId);

    return repo.restoreDocument(documentId)
        .compose(doc -> {
          if (doc == null) {
            return Future.failedFuture(new NotFoundException("Document not found with ID: " + documentId));
          }
          String cacheKey = CACHE_PREFIX + "id:" + documentId;
          return redis.delete(cacheKey)
              .onSuccess(deleted -> {
                if (deleted > 0) {
                  logger.debug("Document {} cache invalidated on restore", documentId);
                }
              })
              .onFailure(err -> logger.warn("Failed to invalidate cache for restored document {}: {}", documentId,
                  err.getMessage()))
              .map(doc);
        })
        .map(doc -> {
          metrics.completeSpanSuccess(tracingContext, "restore", "Document restored successfully");
          return ApiResponse.success("Document restored successfully", MerchantDocumentResponse.from(doc));
        })
        .recover(err -> {
          logger.error("Failed to restore document: {}", documentId, err);
          metrics.completeSpanError(tracingContext, "restore", err.getMessage());
          return Future.succeededFuture(
              ApiResponse.error("Failed to restore document: " + err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Void>> deleteDocumentPermanently(Integer documentId) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantDocumentCommandService.deletePermanent",
        Attributes.builder()
            .put("document.id", (long) documentId)
            .build());

    logger.info("Permanently deleting document: {}", documentId);

    return repo.deleteDocumentPermanently(documentId)
        .compose(v -> {
          String cacheKey = CACHE_PREFIX + "id:" + documentId;
          return redis.delete(cacheKey)
              .onSuccess(deleted -> {
                if (deleted > 0) {
                  logger.debug("Document {} cache invalidated on permanent delete", documentId);
                }
              })
              .onFailure(err -> logger.warn("Failed to invalidate cache for deleted document {}: {}", documentId,
                  err.getMessage()))
              .map(v);
        })
        .map(v -> {
          logger.info("Document deleted permanently: {}", documentId);
          metrics.completeSpanSuccess(tracingContext, "deletePermanent", "Document deleted permanently");
          return ApiResponse.<Void>success("Document deleted permanently", null);
        })
        .recover(err -> {
          logger.error("Failed to deletePermanent document: {}", documentId, err);
          metrics.completeSpanError(tracingContext, "deletePermanent", err.getMessage());
          return Future.succeededFuture(
              ApiResponse.error("Failed to delete document: " + err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Void>> restoreAllDocuments() {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan("MerchantDocumentCommandService.restoreAll");

    logger.info("Restoring all trashed documents");

    return repo.restoreAllDocuments()
        .compose(v -> {
          logger.info("All documents restored successfully");
          metrics.completeSpanSuccess(tracingContext, "restore_all", "All documents restored");
          return Future.succeededFuture(
              ApiResponse.<Void>success("All documents restored successfully"));
        })
        .recover(err -> {
          logger.error("Failed to restore all documents", err);
          metrics.completeSpanError(tracingContext, "restore_all", err.getMessage());
          return Future.succeededFuture(
              ApiResponse.error("Failed to restore all documents: " + err.getMessage()));
        });
  }

  @Override
  public Future<ApiResponse<Void>> deleteAllPermanentDocuments() {
    TracingMetrics.TracingContext tracingContext = metrics
        .startSpan("MerchantDocumentCommandService.deleteAllPermanent");

    logger.info("Permanently deleting all trashed documents");

    return repo.deleteAllPermanentDocuments()
        .compose(v -> {
          logger.info("All trashed documents permanently deleted");
          metrics.completeSpanSuccess(tracingContext, "deleteAllPermanent",
              "All trashed documents permanently deleted");
          return Future.succeededFuture(
              ApiResponse.<Void>success("All trashed documents permanently deleted successfully"));
        })
        .recover(err -> {
          logger.error("Failed to permanently delete all documents", err);
          metrics.completeSpanError(tracingContext, "deleteAllPermanent", err.getMessage());
          return Future.succeededFuture(
              ApiResponse.error("Failed to permanently delete all documents: " + err.getMessage()));
        });
  }
}
