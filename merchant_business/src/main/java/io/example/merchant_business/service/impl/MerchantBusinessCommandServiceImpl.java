package io.example.merchant_business.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_business.domain.requests.CreateMerchantBusinessRequest;
import io.example.merchant_business.domain.requests.UpdateMerchantBusinessRequest;
import io.example.merchant_business.model.MerchantBusinessResponse;
import io.example.merchant_business.model.MerchantBusinessResponseDeleteAt;
import io.example.merchant_business.repository.MerchantBusinessCommandRepository;
import io.example.merchant_business.repository.MerchantBusinessQueryRepository;
import io.example.merchant_business.repository.MerchantQueryRepository;
import io.example.merchant_business.service.MerchantBusinessCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantBusinessCommandServiceImpl implements MerchantBusinessCommandService {
  private static final Logger log = LoggerFactory.getLogger(MerchantBusinessCommandServiceImpl.class);

  private final MerchantBusinessCommandRepository repo;
  private final MerchantBusinessQueryRepository queryRepository;
  private final MerchantQueryRepository merchantRepo;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private static final String CACHE_PREFIX = "merchant_business:";

  private Future<Void> evict(Long id) {
    return redis.delete(CACHE_PREFIX + "id:" + id).mapEmpty();
  }

  private Future<Void> evictAll() {
    return redis.deleteByPattern(CACHE_PREFIX + "list:*").mapEmpty();
  }

  @Override
  public Future<MerchantBusinessResponse> create(CreateMerchantBusinessRequest req) {
    var ctx = metrics.startSpan("MerchantBusinessCommandService.create",
        Attributes.builder()
            .put("business.merchant_id", (long) req.getMerchantId())
            .put("business.business_type", req.getBusinessType())
            .build());

    log.info("Creating business info for merchant: {}", req.getMerchantId());

    return merchantRepo.findById(req.getMerchantId())
        .compose(exists -> {
          if (!exists) {
            return Future.failedFuture(new NotFoundException("Merchant not found with ID: " + req.getMerchantId()));
          }
          return repo.create(req);
        })
        .map(MerchantBusinessResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "create", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "create", e.getMessage()));
  }

  @Override
  public Future<MerchantBusinessResponse> update(UpdateMerchantBusinessRequest req) {
    var ctx = metrics.startSpan("MerchantBusinessCommandService.update",
        Attributes.builder()
            .put("business.id", (long) req.getMerchantBusinessInfoId())
            .put("business.business_type", req.getBusinessType())
            .build());

    log.info("Updating business info: {}", req.getMerchantBusinessInfoId());

    return repo.update(req)
        .compose(mbi -> {
          if (mbi == null) {
            return Future.failedFuture(
                new NotFoundException("Business info not found with ID: " + req.getMerchantBusinessInfoId()));
          }
          return evict((long) req.getMerchantBusinessInfoId()).map(v -> mbi);
        })
        .map(MerchantBusinessResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "update", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "update", e.getMessage()));
  }

  @Override
  public Future<MerchantBusinessResponseDeleteAt> trash(Long id) {
    var ctx = metrics.startSpan("MerchantBusinessCommandService.trash",
        Attributes.builder().put("business.id", id).build());

    log.info("Trashing business info: {}", id);

    return repo.trash(id)
        .compose(mbi -> {
          if (mbi == null) {
            return Future.failedFuture(new NotFoundException("Business info not found with ID: " + id));
          }
          return evict(id).map(v -> mbi);
        })
        .map(MerchantBusinessResponseDeleteAt::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "trash", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "trash", e.getMessage()));
  }

  @Override
  public Future<MerchantBusinessResponseDeleteAt> restore(Long id) {
    var ctx = metrics.startSpan("MerchantBusinessCommandService.restore",
        Attributes.builder().put("business.id", id).build());

    log.info("Restoring business info: {}", id);

    return queryRepository.findByTrashedId(id)
        .compose(trashed -> {
          if (trashed == null) {
            return Future.failedFuture(new BadRequestException("Business info not found or must be trashed first"));
          }
          return repo.restore(id);
        })
        .compose(r -> {
          if (r == null) {
            return Future.failedFuture(new NotFoundException("Business info not found"));
          }
          return evict(id).map(v -> r);
        })
        .map(MerchantBusinessResponseDeleteAt::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore", "Success"))
        .onFailure(e -> {
          log.error("Failed to restore business info", e);
          metrics.completeSpanError(ctx, "restore", e.getMessage());
        });
  }

  @Override
  public Future<Void> deletePermanent(Long id) {
    var ctx = metrics.startSpan("MerchantBusinessCommandService.deletePermanent",
        Attributes.builder().put("business.id", id).build());

    log.info("Permanently deleting business info: {}", id);

    return queryRepository.findByTrashedId(id)
        .compose(existing -> {
          if (existing == null) {
            return Future.failedFuture(new BadRequestException("Business info not found or must be trashed first"));
          }
          return repo.deletePermanent(id);
        })
        .compose(success -> {
          if (!success) {
            return Future.failedFuture(new BadRequestException("Business info not found or must be trashed first"));
          }
          return evict(id);
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deletePermanent", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "deletePermanent", e.getMessage()));
  }

  @Override
  public Future<Void> restoreAll() {
    var ctx = metrics.startSpan("MerchantBusinessCommandService.restoreAll");

    log.info("Restoring all business info");

    return repo.restoreAll()
        .compose(count -> {
          if (count == 0) {
            return Future.failedFuture(new NotFoundException("No trashed business info found"));
          }
          return evictAll();
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restoreAll", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "restoreAll", e.getMessage()));
  }

  @Override
  public Future<Void> deleteAllPermanent() {
    var ctx = metrics.startSpan("MerchantBusinessCommandService.deleteAllPermanent");

    log.info("Permanently deleting all business info");

    return repo.deleteAllPermanent()
        .compose(count -> {
          if (count == 0) {
            return Future.failedFuture(new NotFoundException("No trashed business info found"));
          }
          return evictAll();
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deleteAllPermanent", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "deleteAllPermanent", e.getMessage()));
  }
}