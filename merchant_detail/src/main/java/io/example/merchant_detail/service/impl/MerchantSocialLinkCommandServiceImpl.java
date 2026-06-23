package io.example.merchant_detail.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_detail.domain.requests.CreateMerchantSocialRequest;
import io.example.merchant_detail.domain.requests.UpdateMerchantSocialRequest;
import io.example.merchant_detail.model.MerchantSocialMediaLinkResponse;
import io.example.merchant_detail.model.MerchantSocialMediaLinkResponseDeleteAt;
import io.example.merchant_detail.repository.MerchantSocialLinkCommandRepository;
import io.example.merchant_detail.repository.MerchantSocialLinkeQueryRepository;
import io.example.merchant_detail.service.MerchantSocialLinkCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantSocialLinkCommandServiceImpl implements MerchantSocialLinkCommandService {
  private static final Logger logger = LoggerFactory.getLogger(MerchantSocialLinkCommandServiceImpl.class);

  private final MerchantSocialLinkCommandRepository repository;
  private final MerchantSocialLinkeQueryRepository queryRepository;
  private final RedisService redisService;
  private final TracingMetrics tracingMetrics;

  private static final String CACHE_PREFIX = "merchant_social:";

  private Future<Void> evict(Long id) {
    return redisService.delete(CACHE_PREFIX + "id:" + id).mapEmpty();
  }

  private Future<Void> evictAll() {
    return redisService.deleteByPattern(CACHE_PREFIX + "list:*").mapEmpty();
  }

  @Override
  public Future<MerchantSocialMediaLinkResponse> create(CreateMerchantSocialRequest req) {
    var ctx = tracingMetrics.startSpan("MerchantSocialLinkCommandService.create",
        Attributes.builder()
            .put("social.merchant_detail_id", req.getMerchantDetailId())
            .put("social.platform", req.getPlatform())
            .build());

    logger.info("Creating social link for merchant detail: {}", req.getMerchantDetailId());

    return repository.create(req)
        .map(MerchantSocialMediaLinkResponse::from)
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "create", "Success"))
        .onFailure(e -> {
          logger.error("Failed to create social link", e);
          tracingMetrics.completeSpanError(ctx, "create", e.getMessage());
        });
  }

  @Override
  public Future<MerchantSocialMediaLinkResponse> update(UpdateMerchantSocialRequest req) {
    var ctx = tracingMetrics.startSpan("MerchantSocialLinkCommandService.update",
        Attributes.builder()
            .put("social.id", req.getId())
            .put("social.platform", req.getPlatform())
            .build());

    logger.info("Updating social link: {}", req.getId());

    return repository.update(req)
        .compose(data -> {
          if (data == null) {
            return Future.failedFuture(new NotFoundException("Social link not found with ID: " + req.getId()));
          }
          return evict(req.getId().longValue()).map(v -> data);
        })
        .map(MerchantSocialMediaLinkResponse::from)
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "update", "Success"))
        .onFailure(e -> {
          logger.error("Failed to update social link", e);
          tracingMetrics.completeSpanError(ctx, "update", e.getMessage());
        });
  }

  @Override
  public Future<MerchantSocialMediaLinkResponseDeleteAt> trash(Integer id) {
    var ctx = tracingMetrics.startSpan("MerchantSocialLinkCommandService.trash",
        Attributes.builder().put("social.id", id).build());

    logger.info("Trashing social link: {}", id);

    return repository.trash(id)
        .compose(data -> {
          if (data == null) {
            return Future.failedFuture(new NotFoundException("Social link not found with ID: " + id));
          }
          return evict(id.longValue()).map(v -> data);
        })
        .map(MerchantSocialMediaLinkResponseDeleteAt::from)
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "trash", "Success"))
        .onFailure(e -> {
          logger.error("Failed to trash social link", e);
          tracingMetrics.completeSpanError(ctx, "trash", e.getMessage());
        });
  }

  @Override
  public Future<MerchantSocialMediaLinkResponseDeleteAt> restore(Integer id) {
    var ctx = tracingMetrics.startSpan("MerchantSocialLinkCommandService.restore",
        Attributes.builder().put("social.id", id).build());

    logger.info("Restoring social link: {}", id);

    return queryRepository.findByTrashedId(id)
        .compose(trashed -> {
          if (trashed == null) {
            return Future.failedFuture(new BadRequestException("Social link not found or must be trashed first"));
          }
          return repository.restore(id);
        })
        .compose(r -> {
          if (r == null) {
            return Future.failedFuture(new NotFoundException("Social link not found"));
          }
          return evict(id.longValue()).map(v -> r);
        })
        .map(MerchantSocialMediaLinkResponseDeleteAt::from)
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "restore", "Success"))
        .onFailure(e -> {
          logger.error("Failed to restore social link", e);
          tracingMetrics.completeSpanError(ctx, "restore", e.getMessage());
        });
  }

  @Override
  public Future<Void> deletePermanent(Integer id) {
    var ctx = tracingMetrics.startSpan("MerchantSocialLinkCommandService.deletePermanent",
        Attributes.builder().put("social.id", id).build());

    logger.info("Deleting social link permanently: {}", id);

    return queryRepository.findByTrashedId(id)
        .compose(data -> {
          if (data == null) {
            return Future.failedFuture(new NotFoundException("Social link not found with ID: " + id));
          }
          return repository.deletePermanent(id);
        })
        .compose(v -> evict(id.longValue()))
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "deletePermanent", "Success"))
        .onFailure(e -> {
          logger.error("Failed to delete social link permanently", e);
          tracingMetrics.completeSpanError(ctx, "deletePermanent", e.getMessage());
        });
  }

  @Override
  public Future<Void> restoreAll() {
    var ctx = tracingMetrics.startSpan("MerchantSocialLinkCommandService.restoreAll");

    logger.info("Restoring all social links");

    return repository.restoreAll()
        .compose(count -> {
          if (count == 0) {
            return Future.failedFuture(new NotFoundException("No trashed social links found"));
          }
          return evictAll();
        })
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "restoreAll", "Success"))
        .onFailure(e -> {
          logger.error("Failed to restore all social links", e);
          tracingMetrics.completeSpanError(ctx, "restoreAll", e.getMessage());
        });
  }

  @Override
  public Future<Void> deleteAllPermanent() {
    var ctx = tracingMetrics.startSpan("MerchantSocialLinkCommandService.deleteAllPermanent");

    logger.info("Deleting all social links permanently");

    return repository.deleteAll()
        .compose(count -> {
          if (count == 0) {
            return Future.failedFuture(new NotFoundException("No trashed social links found"));
          }
          return evictAll();
        })
        .onSuccess(v -> tracingMetrics.completeSpanSuccess(ctx, "deleteAllPermanent", "Success"))
        .onFailure(e -> {
          logger.error("Failed to delete all social links permanently", e);
          tracingMetrics.completeSpanError(ctx, "deleteAllPermanent", e.getMessage());
        });
  }
}