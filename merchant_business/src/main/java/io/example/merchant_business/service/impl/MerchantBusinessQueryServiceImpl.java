package io.example.merchant_business.service.impl;

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
import io.example.merchant_business.model.MerchantBusiness;
import io.example.merchant_business.model.MerchantBusinessResponse;
import io.example.merchant_business.model.MerchantBusinessResponseDeleteAt;
import io.example.merchant_business.repository.MerchantBusinessQueryRepository;
import io.example.merchant_business.service.MerchantBusinessQueryService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import pb.merchant.MerchantQuery.FindAllMerchantRequest;

public class MerchantBusinessQueryServiceImpl implements MerchantBusinessQueryService {
  private static final Logger logger = LoggerFactory.getLogger(MerchantBusinessQueryServiceImpl.class);

  private final MerchantBusinessQueryRepository repo;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private static final String CACHE_PREFIX = "merchant_business:";
  private static final Duration CACHE_TTL = Duration.ofMinutes(10);

  public MerchantBusinessQueryServiceImpl(
      MerchantBusinessQueryRepository repo,
      RedisService redis,
      TracingMetrics metrics) {
    this.repo = repo;
    this.redis = redis;
    this.metrics = metrics;
  }

  @Override
  public Future<ApiResponsePagination<List<MerchantBusinessResponse>>> getAll(FindAllMerchantRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantBusinessQueryService.getAll",
        Attributes.builder()
            .put("business.page", (long) req.getPage())
            .put("business.page_size", (long) req.getPageSize())
            .put("business.search", req.getSearch())
            .build());

    logger.info("Getting all merchant business info: page={}, size={}, search={}", req.getPage(), req.getPageSize(), req.getSearch());

    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    String search = req.getSearch();

    return repo.getMerchantsBusinessInformation(search, page, pageSize)
        .map(result -> mapBusinessPagination(result, page, pageSize))
        .map(resp -> {
          metrics.completeSpanSuccess(tracingContext, "get_all", "Fetched " + resp.data().size() + " records");
          return resp;
        })
        .recover(err -> {
          logger.error("Failed to get all merchant business info", err);
          metrics.completeSpanError(tracingContext, "get_all", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponsePagination<List<MerchantBusinessResponseDeleteAt>>> getActive(FindAllMerchantRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantBusinessQueryService.getActive",
        Attributes.builder()
            .put("business.page", (long) req.getPage())
            .put("business.page_size", (long) req.getPageSize())
            .put("business.search", req.getSearch())
            .build());

    logger.info("Getting active merchant business info: page={}, size={}, search={}", req.getPage(), req.getPageSize(), req.getSearch());

    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    String search = req.getSearch();

    return repo.getMerchantsBusinessInformationActive(search, page, pageSize)
        .map(result -> mapBusinessPaginationDeleteAt(result, page, pageSize))
        .map(resp -> {
          metrics.completeSpanSuccess(tracingContext, "get_active", "Fetched " + resp.data().size() + " active records");
          return resp;
        })
        .recover(err -> {
          logger.error("Failed to get active merchant business info", err);
          metrics.completeSpanError(tracingContext, "get_active", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponsePagination<List<MerchantBusinessResponseDeleteAt>>> getTrashed(FindAllMerchantRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantBusinessQueryService.getTrashed",
        Attributes.builder()
            .put("business.page", (long) req.getPage())
            .put("business.page_size", (long) req.getPageSize())
            .put("business.search", req.getSearch())
            .build());

    logger.info("Getting trashed merchant business info: page={}, size={}, search={}", req.getPage(), req.getPageSize(), req.getSearch());

    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    String search = req.getSearch();

    return repo.getMerchantsBusinessInformationTrashed(search, page, pageSize)
        .map(result -> mapBusinessPaginationDeleteAt(result, page, pageSize))
        .map(resp -> {
          metrics.completeSpanSuccess(tracingContext, "get_trashed", "Fetched " + resp.data().size() + " trashed records");
          return resp;
        })
        .recover(err -> {
          logger.error("Failed to get trashed merchant business info", err);
          metrics.completeSpanError(tracingContext, "get_trashed", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponse<MerchantBusinessResponse>> getById(Long id) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantBusinessQueryService.getById",
        Attributes.builder()
            .put("business.id", id)
            .build());
    Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

    logger.info("Getting merchant business info by id: {}", id);

    String cacheKey = CACHE_PREFIX + "id:" + id;

    return redis.getJson(cacheKey, MerchantBusiness.class)
        .compose(cached -> {
          if (cached != null) {
            logger.info("Serving merchant business info {} from cache", id);
            span.setAttribute("business.cache_hit", true);
            metrics.completeSpanSuccess(tracingContext, "get_by_id", "Info fetched from cache");
            return Future.succeededFuture(ApiResponse.success(
                "Info fetched successfully (from cache)",
                MerchantBusinessResponse.from(cached)));
          }
          span.setAttribute("business.cache_hit", false);
          return repo.getMerchantBusinessInformation(id)
              .compose(mbi -> {
                if (mbi == null) {
                  return Future.failedFuture(new NotFoundException("Merchant Business Information not found"));
                }
                return redis.setJson(cacheKey, mbi, CACHE_TTL).map(mbi);
              })
              .map(mbi -> {
                metrics.completeSpanSuccess(tracingContext, "get_by_id", "Info fetched from database");
                return ApiResponse.success("Info fetched successfully", MerchantBusinessResponse.from(mbi));
              });
        })
        .recover(err -> {
          logger.error("Failed to get merchant business info by id: {}", id, err);
          metrics.completeSpanError(tracingContext, "get_by_id", err.getMessage());
          if (err instanceof NotFoundException) {
            return Future.succeededFuture(ApiResponse.error(err.getMessage()));
          }
          return Future.failedFuture(err);
        });
  }

  private ApiResponsePagination<List<MerchantBusinessResponse>> mapBusinessPagination(
      PagedResult<MerchantBusiness> result,
      int page,
      int pageSize) {
    int totalRecords = result.getTotalRecords();
    int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
    List<MerchantBusinessResponse> data = result.getData().stream()
        .map(MerchantBusinessResponse::from)
        .toList();

    return new ApiResponsePagination<>(
        "success",
        "Merchant business info found",
        data,
        new PaginationMeta(page, pageSize, totalPages, totalRecords));
  }

  private ApiResponsePagination<List<MerchantBusinessResponseDeleteAt>> mapBusinessPaginationDeleteAt(
      PagedResult<MerchantBusiness> result,
      int page,
      int pageSize) {
    int totalRecords = result.getTotalRecords();
    int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
    List<MerchantBusinessResponseDeleteAt> data = result.getData().stream()
        .map(MerchantBusinessResponseDeleteAt::from)
        .toList();

    return new ApiResponsePagination<>(
        "success",
        "Merchant business info found",
        data,
        new PaginationMeta(page, pageSize, totalPages, totalRecords));
  }
}
