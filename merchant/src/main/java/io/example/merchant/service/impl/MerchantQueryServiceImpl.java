package io.example.merchant.service.impl;

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
import io.example.merchant.model.Merchant;
import io.example.merchant.model.MerchantResponse;
import io.example.merchant.model.MerchantResponseDeleteAt;
import io.example.merchant.repository.MerchantQueryRepository;
import io.example.merchant.service.MerchantQueryService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import pb.merchant.MerchantQuery.FindAllMerchantRequest;

public class MerchantQueryServiceImpl implements MerchantQueryService {
  private static final Logger logger = LoggerFactory.getLogger(MerchantQueryServiceImpl.class);

  private final MerchantQueryRepository repo;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private static final String CACHE_PREFIX = "merchant:";
  private static final Duration CACHE_TTL = Duration.ofMinutes(10);

  public MerchantQueryServiceImpl(
      MerchantQueryRepository repo,
      RedisService redis,
      TracingMetrics metrics) {
    this.repo = repo;
    this.redis = redis;
    this.metrics = metrics;
  }

  @Override
  public Future<ApiResponsePagination<List<MerchantResponse>>> getAllMerchants(FindAllMerchantRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantQueryService.getAllMerchants",
        Attributes.builder()
            .put("merchant.page", (long) req.getPage())
            .put("merchant.page_size", (long) req.getPageSize())
            .put("merchant.search", req.getSearch())
            .build());

    logger.info("Getting all merchants: page={}, size={}, search={}", req.getPage(), req.getPageSize(), req.getSearch());

    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    String search = req.getSearch();

    return repo.getMerchants(search, page, pageSize)
        .map(result -> mapMerchantPagination(result, page, pageSize))
        .map(resp -> {
          metrics.completeSpanSuccess(tracingContext, "get_all", "Fetched " + resp.data().size() + " merchants");
          return resp;
        })
        .recover(err -> {
          logger.error("Failed to get all merchants", err);
          metrics.completeSpanError(tracingContext, "get_all", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponsePagination<List<MerchantResponseDeleteAt>>> getActiveMerchants(FindAllMerchantRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantQueryService.getActiveMerchants",
        Attributes.builder()
            .put("merchant.page", (long) req.getPage())
            .put("merchant.page_size", (long) req.getPageSize())
            .put("merchant.search", req.getSearch())
            .build());

    logger.info("Getting active merchants: page={}, size={}, search={}", req.getPage(), req.getPageSize(), req.getSearch());

    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    String search = req.getSearch();

    return repo.getActiveMerchants(search, page, pageSize)
        .map(result -> mapMerchantPaginationDeleteAt(result, page, pageSize))
        .map(resp -> {
          metrics.completeSpanSuccess(tracingContext, "get_active", "Fetched " + resp.data().size() + " active merchants");
          return resp;
        })
        .recover(err -> {
          logger.error("Failed to get active merchants", err);
          metrics.completeSpanError(tracingContext, "get_active", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponsePagination<List<MerchantResponseDeleteAt>>> getTrashedMerchants(FindAllMerchantRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantQueryService.getTrashedMerchants",
        Attributes.builder()
            .put("merchant.page", (long) req.getPage())
            .put("merchant.page_size", (long) req.getPageSize())
            .put("merchant.search", req.getSearch())
            .build());

    logger.info("Getting trashed merchants: page={}, size={}, search={}", req.getPage(), req.getPageSize(), req.getSearch());

    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    String search = req.getSearch();

    return repo.getTrashedMerchants(search, page, pageSize)
        .map(result -> mapMerchantPaginationDeleteAt(result, page, pageSize))
        .map(resp -> {
          metrics.completeSpanSuccess(tracingContext, "get_trashed", "Fetched " + resp.data().size() + " trashed merchants");
          return resp;
        })
        .recover(err -> {
          logger.error("Failed to get trashed merchants", err);
          metrics.completeSpanError(tracingContext, "get_trashed", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponse<MerchantResponse>> getMerchantById(Integer merchantId) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantQueryService.getMerchantById",
        Attributes.builder()
            .put("merchant.id", (long) merchantId)
            .build());
    Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

    logger.info("Getting merchant by id: {}", merchantId);

    String cacheKey = CACHE_PREFIX + "id:" + merchantId;

    return redis.getJson(cacheKey, Merchant.class)
        .compose(cached -> {
          if (cached != null) {
            logger.info("Serving merchant {} from cache", merchantId);
            span.setAttribute("merchant.cache_hit", true);
            metrics.completeSpanSuccess(tracingContext, "get_by_id", "Merchant fetched from cache");
            return Future.succeededFuture(ApiResponse.success(
                "Merchant fetched successfully (from cache)",
                MerchantResponse.from(cached)));
          }
          span.setAttribute("merchant.cache_hit", false);
          return repo.getMerchantById(merchantId)
              .compose(merchant -> {
                if (merchant == null) {
                  return Future.failedFuture(new NotFoundException("Merchant not found"));
                }
                return redis.setJson(cacheKey, merchant, CACHE_TTL).map(merchant);
              })
              .map(merchant -> {
                metrics.completeSpanSuccess(tracingContext, "get_by_id", "Merchant fetched from database");
                return ApiResponse.success("Merchant fetched successfully", MerchantResponse.from(merchant));
              });
        })
        .recover(err -> {
          logger.error("Failed to get merchant by id: {}", merchantId, err);
          metrics.completeSpanError(tracingContext, "get_by_id", err.getMessage());
          if (err instanceof NotFoundException) {
            return Future.succeededFuture(ApiResponse.error(err.getMessage()));
          }
          return Future.failedFuture(err);
        });
  }

  private ApiResponsePagination<List<MerchantResponse>> mapMerchantPagination(
      PagedResult<Merchant> result,
      int page,
      int pageSize) {
    int totalRecords = result.getTotalRecords();
    int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
    List<MerchantResponse> data = result.getData().stream()
        .map(MerchantResponse::from)
        .toList();

    return new ApiResponsePagination<>(
        "success",
        "Merchants found",
        data,
        new PaginationMeta(page, pageSize, totalPages, totalRecords));
  }

  private ApiResponsePagination<List<MerchantResponseDeleteAt>> mapMerchantPaginationDeleteAt(
      PagedResult<Merchant> result,
      int page,
      int pageSize) {
    int totalRecords = result.getTotalRecords();
    int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
    List<MerchantResponseDeleteAt> data = result.getData().stream()
        .map(MerchantResponseDeleteAt::from)
        .toList();

    return new ApiResponsePagination<>(
        "success",
        "Merchants found",
        data,
        new PaginationMeta(page, pageSize, totalPages, totalRecords));
  }
}
