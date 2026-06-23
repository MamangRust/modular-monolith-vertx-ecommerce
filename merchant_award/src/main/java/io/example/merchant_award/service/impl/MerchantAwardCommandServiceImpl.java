package io.example.merchant_award.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_award.domain.requests.CreateMerchantAwardRequest;
import io.example.merchant_award.domain.requests.UpdateMerchantAwardRequest;
import io.example.merchant_award.model.MerchantAwardResponse;
import io.example.merchant_award.model.MerchantAwardResponseDeleteAt;
import io.example.merchant_award.repository.MerchantAwardCommandRepository;
import io.example.merchant_award.repository.MerchantAwardQueryRepository;
import io.example.merchant_award.repository.MerchantQueryRepository;
import io.example.merchant_award.service.MerchantAwardCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantAwardCommandServiceImpl implements MerchantAwardCommandService {
  private static final Logger logger = LoggerFactory.getLogger(MerchantAwardCommandServiceImpl.class);

  private final MerchantAwardCommandRepository repo;
  private final MerchantAwardQueryRepository queryRepository;
  private final MerchantQueryRepository merchantRepo;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private static final String CACHE_PREFIX = "merchant_award:";

  private Future<Void> evict(Long id) {
    return redis.delete(CACHE_PREFIX + "id:" + id).<Void>mapEmpty();
  }

  @Override
  public Future<MerchantAwardResponse> create(CreateMerchantAwardRequest req) {
    var ctx = metrics.startSpan("MerchantAwardCommandService.create",
        Attributes.builder()
            .put("award.merchant_id", (long) req.getMerchantId())
            .put("award.title", req.getTitle())
            .build());

    return merchantRepo.findById(req.getMerchantId())
        .compose(exists -> {
          if (!exists) {
            return Future.failedFuture(new NotFoundException("Merchant not found with ID: " + req.getMerchantId()));
          }
          return repo.create(req);
        })
        .map(MerchantAwardResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "create", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "create", e.getMessage()));
  }

  @Override
  public Future<MerchantAwardResponse> update(UpdateMerchantAwardRequest req) {
    var ctx = metrics.startSpan("MerchantAwardCommandService.update",
        Attributes.builder()
            .put("award.id", req.getMerchantCertificationId())
            .put("award.title", req.getTitle())
            .build());

    return repo.update(req)
        .compose(mca -> {
          if (mca == null) {
            return Future
                .failedFuture(new NotFoundException("Award not found with ID: " + req.getMerchantCertificationId()));
          }
          return evict(req.getMerchantCertificationId()).map(v -> mca);
        })
        .map(MerchantAwardResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "update", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "update", e.getMessage()));
  }

  @Override
  public Future<MerchantAwardResponseDeleteAt> trash(Long id) {
    var ctx = metrics.startSpan("MerchantAwardCommandService.trash",
        Attributes.builder().put("award.id", id).build());

    return repo.trash(id)
        .compose(mca -> {
          if (mca == null) {
            return Future.failedFuture(new NotFoundException("Award not found with ID: " + id));
          }
          return evict(id).map(v -> mca);
        })
        .map(MerchantAwardResponseDeleteAt::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "trash", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "trash", e.getMessage()));
  }

  @Override
  public Future<MerchantAwardResponseDeleteAt> restore(Long id) {
    var ctx = metrics.startSpan("MerchantAwardCommandService.restore",
        Attributes.builder().put("award.id", id).build());

    logger.info("Restoring award: {}", id);

    return queryRepository.findByTrashedId(id)
        .compose(trashed -> {
          if (trashed == null) {
            return Future.failedFuture(new BadRequestException("Award not found or must be trashed first"));
          }
          return repo.restore(id);
        })
        .compose(r -> {
          if (r == null) {
            return Future.failedFuture(new NotFoundException("Award not found"));
          }
          return evict(id).map(v -> r);
        })
        .map(MerchantAwardResponseDeleteAt::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore", "Success"))
        .onFailure(e -> {
          logger.error("Failed to restore award", e);
          metrics.completeSpanError(ctx, "restore", e.getMessage());
        });
  }

  @Override
  public Future<Void> deletePermanent(Long id) {
    var ctx = metrics.startSpan("MerchantAwardCommandService.deletePermanent",
        Attributes.builder().put("award.id", id).build());

    return queryRepository.findByTrashedId(id)
        .compose(trashed -> {
          if (trashed == null) {
            return Future.<Void>failedFuture(
                new BadRequestException("Award not found or must be trashed before permanent deletion"));
          }
          return repo.deletePermanent(id)
              .compose(v -> evict(id));
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deletePermanent", "Award deleted permanently"))
        .onFailure(err -> metrics.completeSpanError(ctx, "deletePermanent", err.getMessage()));
  }

  @Override
  public Future<Void> restoreAll() {
    var ctx = metrics.startSpan("MerchantAwardCommandService.restoreAll");

    return repo.restoreAll()
        .compose(count -> {
          if (count == 0) {
            return Future.<Void>failedFuture(new NotFoundException("No trashed awards found"));
          }
          return redis.delete(CACHE_PREFIX + "list:*").<Void>mapEmpty();
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore_all", "Success"))
        .onFailure(err -> metrics.completeSpanError(ctx, "restore_all", err.getMessage()));
  }

  @Override
  public Future<Void> deleteAllPermanent() {
    var ctx = metrics.startSpan("MerchantAwardCommandService.deleteAllPermanent");

    return repo.deleteAllPermanent()
        .compose(count -> {
          if (count == 0) {
            return Future.<Void>failedFuture(new NotFoundException("No trashed awards found"));
          }
          return redis.delete(CACHE_PREFIX + "list:*").<Void>mapEmpty();
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "delete_all_permanent", "Success"))
        .onFailure(err -> metrics.completeSpanError(ctx, "delete_all_permanent", err.getMessage()));
  }
}