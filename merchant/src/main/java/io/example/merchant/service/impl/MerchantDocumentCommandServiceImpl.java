package io.example.merchant.service.impl;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.KafkaService;
import io.example.common.service.RedisService;
import io.example.common.utils.EmailTemplate;
import io.example.merchant.domain.requests.CreateMerchantDocumentRequest;
import io.example.merchant.domain.requests.UpdateMerchantDocumentRequest;
import io.example.merchant.domain.requests.UpdateMerchantDocumentStatusRequest;
import io.example.merchant.model.MerchantDocumentResponse;
import io.example.merchant.repository.MerchantDocumentCommandRepository;
import io.example.merchant.repository.MerchantDocumentQueryRepository;
import io.example.merchant.repository.MerchantQueryRepository;
import io.example.merchant.repository.UserQueryRepository;
import io.example.merchant.service.MerchantDocumentCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantDocumentCommandServiceImpl implements MerchantDocumentCommandService {
  private static final Logger log = LoggerFactory.getLogger(MerchantDocumentCommandServiceImpl.class);

  private final MerchantDocumentCommandRepository repo;
  private final MerchantDocumentQueryRepository queryRepository;
  private final MerchantQueryRepository merchantRepo;
  private final UserQueryRepository userRepo;
  private final RedisService redis;
  private final TracingMetrics metrics;
  private final KafkaService kafka;

  private static final String CACHE_PREFIX = "merchant_document:";

  private Future<Void> evict(Integer documentId) {
    return redis.delete(CACHE_PREFIX + "id:" + documentId).mapEmpty();
  }

  private Future<Void> evictAll() {
    return redis.deleteByPattern(CACHE_PREFIX + "list:*").mapEmpty();
  }

  @Override
  public Future<MerchantDocumentResponse> createDocument(CreateMerchantDocumentRequest req) {
    var ctx = metrics.startSpan("MerchantDocumentCommandService.createDocument",
        Attributes.builder()
            .put("document.merchant_id", (long) req.getMerchantId())
            .put("document.type", Objects.requireNonNull(req.getDocumentType()))
            .build());

    return merchantRepo.getMerchantById(req.getMerchantId().longValue())
        .compose(merchant -> {
          if (merchant == null) {
            return Future.failedFuture(new NotFoundException("Merchant not found with ID: " + req.getMerchantId()));
          }
          return userRepo.getUserById(merchant.getUserId())
              .compose(user -> repo.createDocument(req)
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
                          log.error("Failed to send document creation email", err);
                          return Future.succeededFuture(doc);
                        });
                  }));
        })
        .map(MerchantDocumentResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "createDocument", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "createDocument", e.getMessage()));
  }

  @Override
  public Future<MerchantDocumentResponse> updateDocument(UpdateMerchantDocumentRequest req) {
    Integer docId = req.getDocumentId();
    var ctx = metrics.startSpan("MerchantDocumentCommandService.updateDocument",
        Attributes.builder()
            .put("document.id", (long) docId)
            .put("document.merchant_id", (long) req.getMerchantId())
            .build());

    return repo.updateDocument(req)
        .compose(updated -> {
          if (updated == null) {
            return Future.failedFuture(new NotFoundException("Document not found"));
          }
          return evict(docId).map(v -> updated);
        })
        .map(MerchantDocumentResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "updateDocument", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "updateDocument", e.getMessage()));
  }

  @Override
  public Future<MerchantDocumentResponse> updateStatus(UpdateMerchantDocumentStatusRequest req) {
    Integer docId = req.getDocumentId();
    var ctx = metrics.startSpan("MerchantDocumentCommandService.updateStatus",
        Attributes.builder()
            .put("document.id", (long) docId)
            .put("document.status", Objects.requireNonNull(req.getStatus()))
            .build());

    return repo.updateStatus(req)
        .compose(updated -> {
          if (updated == null) {
            return Future.failedFuture(new NotFoundException("Document not found"));
          }
          return merchantRepo.getMerchantById(updated.getMerchantId().longValue())
              .compose(merchant -> {
                if (merchant == null) {
                  return Future.succeededFuture(updated);
                }
                return userRepo.getUserById(merchant.getUserId())
                    .compose(user -> {
                      String status = req.getStatus();
                      if (!status.equals("approved") && !status.equals("rejected")) {
                        return Future.succeededFuture(updated);
                      }

                      String subject = "";
                      String message = "";
                      String link = String.format("https://sanedge.example.com/merchant/%d/documents", user.getId());

                      if (status.equals("approved")) {
                        subject = "Merchant Document Approved";
                        message = String.format(
                            "Your submitted document of type <b>%s</b> has been successfully <b>approved</b>.",
                            updated.getDocumentType());
                      } else if (status.equals("rejected")) {
                        subject = "Merchant Document Rejected";
                        message = String.format(
                            "We're sorry to inform you that your submitted document of type <b>%s</b> has been <b>rejected</b>. Reason/Note: %s. Please review and re-submit the required documents.",
                            updated.getDocumentType(), req.getNote());
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
                          .map(v -> updated)
                          .recover(err -> {
                            log.error("Failed to send document status update email", err);
                            return Future.succeededFuture(updated);
                          });
                    });
              });
        })
        .compose(updated -> evict(docId).map(v -> updated))
        .map(MerchantDocumentResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "updateStatus", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "updateStatus", e.getMessage()));
  }

  @Override
  public Future<MerchantDocumentResponse> trashDocument(Long documentId) {
    var ctx = metrics.startSpan("MerchantDocumentCommandService.trashDocument",
        Attributes.builder().put("document.id", (long) documentId).build());

    return repo.trashDocument(documentId)
        .compose(doc -> {
          if (doc == null) {
            return Future.failedFuture(new NotFoundException("Document not found with ID: " + documentId));
          }
          return evict(documentId.intValue()).map(v -> doc);
        })
        .map(MerchantDocumentResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "trashDocument", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "trashDocument", e.getMessage()));
  }

  @Override
  public Future<MerchantDocumentResponse> restoreDocument(Long documentId) {
    var ctx = metrics.startSpan("MerchantDocumentCommandService.restoreDocument",
        Attributes.builder().put("document.id", (long) documentId).build());

    return queryRepository.findByTrashedId(documentId)
        .compose(doc -> {
          if (doc == null) {
            return Future.failedFuture(new NotFoundException("Document not found with ID: " + documentId));
          }
          return repo.restoreDocument(documentId);
        })
        .compose(doc -> evict(documentId.intValue()).map(doc))
        .map(MerchantDocumentResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restoreDocument", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "restoreDocument", e.getMessage()));
  }

  @Override
  public Future<Void> deleteDocumentPermanently(Long documentId) {
    var ctx = metrics.startSpan("MerchantDocumentCommandService.deleteDocumentPermanently",
        Attributes.builder().put("document.id", (long) documentId).build());

    return queryRepository.findByTrashedId(documentId)
        .compose(trashed -> {
          if (trashed == null) {
            return Future
                .failedFuture(new NotFoundException("Document not found or must be trashed before permanent deletion"));
          }
          return repo.deleteDocumentPermanently(documentId);
        })
        .compose(v -> evict(documentId.intValue()))
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deleteDocumentPermanently", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "deleteDocumentPermanently", e.getMessage()));
  }

  @Override
  public Future<Void> restoreAllDocuments() {
    var ctx = metrics.startSpan("MerchantDocumentCommandService.restoreAllDocuments");

    return repo.restoreAllDocuments()
        .compose(count -> {
          if (count == 0) {
            return Future.failedFuture(new NotFoundException("No trashed documents found"));
          }
          return evictAll();
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restoreAllDocuments", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "restoreAllDocuments", e.getMessage()));
  }

  @Override
  public Future<Void> deleteAllPermanentDocuments() {
    var ctx = metrics.startSpan("MerchantDocumentCommandService.deleteAllPermanentDocuments");

    return repo.deleteAllPermanentDocuments()
        .compose(count -> {
          if (count == 0) {
            return Future.failedFuture(new NotFoundException("No trashed documents found"));
          }
          return evictAll();
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deleteAllPermanentDocuments", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "deleteAllPermanentDocuments", e.getMessage()));
  }
}