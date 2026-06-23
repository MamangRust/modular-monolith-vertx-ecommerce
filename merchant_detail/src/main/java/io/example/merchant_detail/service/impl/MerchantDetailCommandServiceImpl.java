package io.example.merchant_detail.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_detail.domain.requests.CreateMerchantDetailRequest;
import io.example.merchant_detail.domain.requests.UpdateMerchantDetailRequest;
import io.example.merchant_detail.model.MerchantDetailResponse;
import io.example.merchant_detail.model.MerchantDetailResponseDeleteAt;
import io.example.merchant_detail.repository.MerchantDetailCommandRepository;
import io.example.merchant_detail.repository.MerchantDetailQueryRepository;
import io.example.merchant_detail.repository.MerchantQueryRepository;
import io.example.merchant_detail.service.MerchantDetailCommandService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantDetailCommandServiceImpl implements MerchantDetailCommandService {
  private static final Logger logger = LoggerFactory.getLogger(MerchantDetailCommandServiceImpl.class);

  private final MerchantDetailCommandRepository repo;
  private final MerchantDetailQueryRepository queryRepository;
  private final MerchantQueryRepository merchantRepo;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private static final String CACHE_PREFIX = "merchant_detail:";

  private Future<Void> evict(Integer id) {
    return redis.delete(CACHE_PREFIX + "id:" + id).<Void>mapEmpty();
  }

  @Override
  public Future<MerchantDetailResponse> create(CreateMerchantDetailRequest req) {
    var ctx = metrics.startSpan("MerchantDetailCommandService.create",
        Attributes.builder()
            .put("detail.merchant_id", (long) req.getMerchantId())
            .put("detail.display_name", req.getDisplayName())
            .build());

    return merchantRepo.findById(req.getMerchantId())
        .compose(exists -> {
          if (!exists) {
            return Future.failedFuture(
                new NotFoundException("Parent Merchant profile with ID " + req.getMerchantId() + " does not exist!"));
          }
          return repo.create(req);
        })
        .map(MerchantDetailResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "create", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "create", e.getMessage()));
  }

  @Override
  public Future<MerchantDetailResponse> update(UpdateMerchantDetailRequest req) {
    var ctx = metrics.startSpan("MerchantDetailCommandService.update",
        Attributes.builder()
            .put("detail.id", req.getMerchantDetailId())
            .put("detail.display_name", req.getDisplayName())
            .build());

    return repo.update(req)
        .compose(md -> {
          if (md == null) {
            return Future
                .failedFuture(new NotFoundException("Merchant Detail not found with ID: " + req.getMerchantDetailId()));
          }
          return evict(req.getMerchantDetailId()).map(v -> md);
        })
        .map(MerchantDetailResponse::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "update", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "update", e.getMessage()));
  }

  @Override
  public Future<MerchantDetailResponseDeleteAt> trash(Long id) {
    var ctx = metrics.startSpan("MerchantDetailCommandService.trash",
        Attributes.builder().put("detail.id", id).build());

    return repo.trash(id)
        .compose(md -> {
          if (md == null) {
            return Future.failedFuture(new NotFoundException("Merchant Detail not found with ID: " + id));
          }
          return evict(id.intValue()).map(v -> md);
        })
        .map(MerchantDetailResponseDeleteAt::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "trash", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "trash", e.getMessage()));
  }

  @Override
  public Future<MerchantDetailResponseDeleteAt> restore(Long id) {
    var ctx = metrics.startSpan("MerchantDetailCommandService.restore",
        Attributes.builder().put("detail.id", id).build());

    logger.info("Restoring merchant detail: {}", id);

    return queryRepository.findByTrashedId(id)
        .compose(trashed -> {
          if (trashed == null) {
            return Future.failedFuture(new BadRequestException("Merchant Detail not found or must be trashed first"));
          }
          return repo.restore(id);
        })
        .compose(r -> {
          if (r == null) {
            return Future.failedFuture(new NotFoundException("Merchant Detail not found"));
          }
          return evict(id.intValue()).map(v -> r);
        })
        .map(MerchantDetailResponseDeleteAt::from)
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore", "Success"))
        .onFailure(e -> {
          logger.error("Failed to restore merchant detail", e);
          metrics.completeSpanError(ctx, "restore", e.getMessage());
        });
  }

  @Override
  public Future<Void> deletePermanent(Long id) {
    var ctx = metrics.startSpan("MerchantDetailCommandService.deletePermanent",
        Attributes.builder().put("detail.id", id).build());

    return queryRepository.findByTrashedId(id)
        .compose(md -> {
          if (md == null) {
            return Future.<Void>failedFuture(
                new BadRequestException("Merchant detail not found or must be trashed before permanent deletion"));
          }
          return repo.deletePermanent(id)
              .compose(v -> evict(id.intValue()));
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "deletePermanent", "Merchant detail deleted permanently"))
        .onFailure(err -> metrics.completeSpanError(ctx, "deletePermanent", err.getMessage()));
  }

  @Override
  public Future<Void> restoreAll() {
    var ctx = metrics.startSpan("MerchantDetailCommandService.restoreAll");

    return repo.restoreAll()
        .compose(count -> {
          if (count == 0) {
            return Future.<Void>failedFuture(new NotFoundException("No trashed merchant details found"));
          }
          return redis.delete(CACHE_PREFIX + "list:*").<Void>mapEmpty();
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "restore_all", "Success"))
        .onFailure(err -> metrics.completeSpanError(ctx, "restore_all", err.getMessage()));
  }

  @Override
  public Future<Void> deleteAllPermanent() {
    var ctx = metrics.startSpan("MerchantDetailCommandService.deleteAllPermanent");

    return repo.deleteAll()
        .compose(count -> {
          if (count == 0) {
            return Future.<Void>failedFuture(new NotFoundException("No trashed merchant details found"));
          }
          return redis.delete(CACHE_PREFIX + "list:*").<Void>mapEmpty();
        })
        .onSuccess(v -> metrics.completeSpanSuccess(ctx, "delete_all_permanent", "Success"))
        .onFailure(err -> metrics.completeSpanError(ctx, "delete_all_permanent", err.getMessage()));
  }
}