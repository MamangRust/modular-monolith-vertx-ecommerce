package io.example.merchant_award.service.impl;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.example.common.domain.PagedResult;
import io.example.common.exception.NotFoundException;
import io.example.common.model.ApiResponse;
import io.example.common.model.ApiResponsePagination;
import io.example.common.model.PaginationMeta;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant_award.model.MerchantAward;
import io.example.merchant_award.model.MerchantAwardResponse;
import io.example.merchant_award.model.MerchantAwardResponseDeleteAt;
import io.example.merchant_award.repository.MerchantAwardQueryRepository;
import io.example.merchant_award.service.MerchantAwardQueryService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import pb.merchant.MerchantQuery.FindAllMerchantRequest;

public class MerchantAwardQueryServiceImpl implements MerchantAwardQueryService {
  private static final Logger logger = LoggerFactory.getLogger(MerchantAwardQueryServiceImpl.class);

  private final MerchantAwardQueryRepository repo;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private static final String CACHE_PREFIX = "merchant_award:";
  private static final Duration CACHE_TTL = Duration.ofMinutes(10);

  public MerchantAwardQueryServiceImpl(
      MerchantAwardQueryRepository repo,
      RedisService redis,
      TracingMetrics metrics) {
    this.repo = repo;
    this.redis = redis;
    this.metrics = metrics;
  }

  @Override
  public Future<ApiResponsePagination<List<MerchantAwardResponse>>> getAll(FindAllMerchantRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantAwardQueryService.getAll",
        Attributes.builder()
            .put("award.page", (long) req.getPage())
            .put("award.page_size", (long) req.getPageSize())
            .put("award.search", req.getSearch())
            .build());

    logger.info("Getting all merchant awards: page={}, size={}, search={}", req.getPage(), req.getPageSize(), req.getSearch());

    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    String search = req.getSearch();

    return repo.getMerchantCertificationsAndAwards(search, page, pageSize)
        .map(result -> mapAwardPagination(result, page, pageSize))
        .map(resp -> {
          metrics.completeSpanSuccess(tracingContext, "get_all", "Fetched " + resp.data().size() + " awards");
          return resp;
        })
        .recover(err -> {
          logger.error("Failed to get all awards", err);
          metrics.completeSpanError(tracingContext, "get_all", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponsePagination<List<MerchantAwardResponseDeleteAt>>> getActive(FindAllMerchantRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantAwardQueryService.getActive",
        Attributes.builder()
            .put("award.page", (long) req.getPage())
            .put("award.page_size", (long) req.getPageSize())
            .put("award.search", req.getSearch())
            .build());

    logger.info("Getting active merchant awards: page={}, size={}, search={}", req.getPage(), req.getPageSize(), req.getSearch());

    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    String search = req.getSearch();

    return repo.getMerchantCertificationsAndAwardsActive(search, page, pageSize)
        .map(result -> mapAwardPaginationDeleteAt(result, page, pageSize))
        .map(resp -> {
          metrics.completeSpanSuccess(tracingContext, "get_active", "Fetched " + resp.data().size() + " active awards");
          return resp;
        })
        .recover(err -> {
          logger.error("Failed to get active awards", err);
          metrics.completeSpanError(tracingContext, "get_active", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponsePagination<List<MerchantAwardResponseDeleteAt>>> getTrashed(FindAllMerchantRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantAwardQueryService.getTrashed",
        Attributes.builder()
            .put("award.page", (long) req.getPage())
            .put("award.page_size", (long) req.getPageSize())
            .put("award.search", req.getSearch())
            .build());

    logger.info("Getting trashed merchant awards: page={}, size={}, search={}", req.getPage(), req.getPageSize(), req.getSearch());

    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    String search = req.getSearch();

    return repo.getTrashedCertificationsAndAwards(search, page, pageSize)
        .map(result -> mapAwardPaginationDeleteAt(result, page, pageSize))
        .map(resp -> {
          metrics.completeSpanSuccess(tracingContext, "get_trashed", "Fetched " + resp.data().size() + " trashed awards");
          return resp;
        })
        .recover(err -> {
          logger.error("Failed to get trashed awards", err);
          metrics.completeSpanError(tracingContext, "get_trashed", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponse<MerchantAwardResponse>> getById(Long id) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantAwardQueryService.getById",
        Attributes.builder()
            .put("award.id", id)
            .build());
    Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

    logger.info("Getting award by id: {}", id);

    String cacheKey = CACHE_PREFIX + "id:" + id;

    return redis.getJson(cacheKey, MerchantAward.class)
        .compose(cached -> {
          if (cached != null) {
            logger.info("Serving award {} from cache", id);
            span.setAttribute("award.cache_hit", true);
            metrics.completeSpanSuccess(tracingContext, "get_by_id", "Award fetched from cache");
            return Future.succeededFuture(ApiResponse.success(
                "Award fetched successfully (from cache)",
                MerchantAwardResponse.from(cached)));
          }
          span.setAttribute("award.cache_hit", false);
          return repo.getMerchantCertificationOrAward(id)
              .compose(mca -> {
                if (mca == null) {
                  return Future.failedFuture(new NotFoundException("Award not found"));
                }
                return redis.setJson(cacheKey, mca, CACHE_TTL).map(mca);
              })
              .map(mca -> {
                metrics.completeSpanSuccess(tracingContext, "get_by_id", "Award fetched from database");
                return ApiResponse.success("Award fetched successfully", MerchantAwardResponse.from(mca));
              });
        })
        .recover(err -> {
          logger.error("Failed to get award by id: {}", id, err);
          metrics.completeSpanError(tracingContext, "get_by_id", err.getMessage());
          if (err instanceof NotFoundException) {
            return Future.succeededFuture(ApiResponse.error(err.getMessage()));
          }
          return Future.failedFuture(err);
        });
  }

  private ApiResponsePagination<List<MerchantAwardResponse>> mapAwardPagination(
      PagedResult<MerchantAward> result,
      int page,
      int pageSize) {
    int totalRecords = result.getTotalRecords();
    int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
    List<MerchantAwardResponse> data = result.getData().stream()
        .map(MerchantAwardResponse::from)
        .toList();

    return new ApiResponsePagination<>(
        "success",
        "Awards found",
        data,
        new PaginationMeta(page, pageSize, totalPages, totalRecords));
  }

  private ApiResponsePagination<List<MerchantAwardResponseDeleteAt>> mapAwardPaginationDeleteAt(
      PagedResult<MerchantAward> result,
      int page,
      int pageSize) {
    int totalRecords = result.getTotalRecords();
    int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
    List<MerchantAwardResponseDeleteAt> data = result.getData().stream()
        .map(MerchantAwardResponseDeleteAt::from)
        .toList();

    return new ApiResponsePagination<>(
        "success",
        "Awards found",
        data,
        new PaginationMeta(page, pageSize, totalPages, totalRecords));
  }
}
