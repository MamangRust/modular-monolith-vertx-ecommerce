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
import io.example.merchant.model.MerchantDocument;
import io.example.merchant.model.MerchantDocumentResponse;
import io.example.merchant.model.MerchantDocumentResponseDeleteAt;
import io.example.merchant.repository.MerchantDocumentQueryRepository;
import io.example.merchant.service.MerchantDocumentQueryService;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.Future;
import pb.merchant_document.MerchantDocumentQuery.FindAllMerchantDocumentsRequest;

public class MerchantDocumentQueryServiceImpl implements MerchantDocumentQueryService {
  private static final Logger logger = LoggerFactory.getLogger(MerchantDocumentQueryServiceImpl.class);

  private final MerchantDocumentQueryRepository repo;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private static final String CACHE_PREFIX = "merchant_document:";
  private static final Duration CACHE_TTL = Duration.ofMinutes(10);

  public MerchantDocumentQueryServiceImpl(
      MerchantDocumentQueryRepository repo,
      RedisService redis,
      TracingMetrics metrics) {
    this.repo = repo;
    this.redis = redis;
    this.metrics = metrics;
  }

  @Override
  public Future<ApiResponsePagination<List<MerchantDocumentResponse>>> getAllDocuments(FindAllMerchantDocumentsRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantDocumentQueryService.getAllDocuments",
        Attributes.builder()
            .put("document.page", (long) req.getPage())
            .put("document.page_size", (long) req.getPageSize())
            .put("document.search", req.getSearch())
            .build());

    logger.info("Getting all documents: page={}, size={}, search={}", req.getPage(), req.getPageSize(), req.getSearch());

    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    String search = req.getSearch();

    return repo.getDocuments(search, page, pageSize)
        .map(result -> mapDocumentPagination(result, page, pageSize))
        .map(resp -> {
          metrics.completeSpanSuccess(tracingContext, "get_all", "Fetched " + resp.data().size() + " documents");
          return resp;
        })
        .recover(err -> {
          logger.error("Failed to get all documents", err);
          metrics.completeSpanError(tracingContext, "get_all", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponsePagination<List<MerchantDocumentResponse>>> getActiveDocuments(FindAllMerchantDocumentsRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantDocumentQueryService.getActiveDocuments",
        Attributes.builder()
            .put("document.page", (long) req.getPage())
            .put("document.page_size", (long) req.getPageSize())
            .put("document.search", req.getSearch())
            .build());

    logger.info("Getting active documents: page={}, size={}, search={}", req.getPage(), req.getPageSize(), req.getSearch());

    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    String search = req.getSearch();

    return repo.getActiveDocuments(search, page, pageSize)
        .map(result -> mapDocumentPagination(result, page, pageSize))
        .map(resp -> {
          metrics.completeSpanSuccess(tracingContext, "get_active", "Fetched " + resp.data().size() + " active documents");
          return resp;
        })
        .recover(err -> {
          logger.error("Failed to get active documents", err);
          metrics.completeSpanError(tracingContext, "get_active", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponsePagination<List<MerchantDocumentResponseDeleteAt>>> getTrashedDocuments(FindAllMerchantDocumentsRequest req) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantDocumentQueryService.getTrashedDocuments",
        Attributes.builder()
            .put("document.page", (long) req.getPage())
            .put("document.page_size", (long) req.getPageSize())
            .put("document.search", req.getSearch())
            .build());

    logger.info("Getting trashed documents: page={}, size={}, search={}", req.getPage(), req.getPageSize(), req.getSearch());

    int page = req.getPage() > 0 ? req.getPage() : 1;
    int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
    String search = req.getSearch();

    return repo.getTrashedDocuments(search, page, pageSize)
        .map(result -> mapDocumentPaginationDeleteAt(result, page, pageSize))
        .map(resp -> {
          metrics.completeSpanSuccess(tracingContext, "get_trashed", "Fetched " + resp.data().size() + " trashed documents");
          return resp;
        })
        .recover(err -> {
          logger.error("Failed to get trashed documents", err);
          metrics.completeSpanError(tracingContext, "get_trashed", err.getMessage());
          return Future.failedFuture(err);
        });
  }

  @Override
  public Future<ApiResponse<MerchantDocumentResponse>> getDocumentById(Integer documentId) {
    TracingMetrics.TracingContext tracingContext = metrics.startSpan(
        "MerchantDocumentQueryService.getDocumentById",
        Attributes.builder()
            .put("document.id", (long) documentId)
            .build());
    Span span = Span.fromContext(Objects.requireNonNull(tracingContext.getContext()));

    logger.info("Getting document by id: {}", documentId);

    String cacheKey = CACHE_PREFIX + "id:" + documentId;

    return redis.getJson(cacheKey, MerchantDocument.class)
        .compose(cached -> {
          if (cached != null) {
            logger.info("Serving document {} from cache", documentId);
            span.setAttribute("document.cache_hit", true);
            metrics.completeSpanSuccess(tracingContext, "get_by_id", "Document fetched from cache");
            return Future.succeededFuture(ApiResponse.success(
                "Document fetched successfully (from cache)",
                MerchantDocumentResponse.from(cached)));
          }
          span.setAttribute("document.cache_hit", false);
          return repo.getDocumentById(documentId)
              .compose(doc -> {
                if (doc == null) {
                  return Future.failedFuture(new NotFoundException("Merchant document not found"));
                }
                return redis.setJson(cacheKey, doc, CACHE_TTL).map(doc);
              })
              .map(doc -> {
                metrics.completeSpanSuccess(tracingContext, "get_by_id", "Document fetched from database");
                return ApiResponse.success("Document fetched successfully", MerchantDocumentResponse.from(doc));
              });
        })
        .recover(err -> {
          logger.error("Failed to get document by id: {}", documentId, err);
          metrics.completeSpanError(tracingContext, "get_by_id", err.getMessage());
          if (err instanceof NotFoundException) {
            return Future.succeededFuture(ApiResponse.error(err.getMessage()));
          }
          return Future.failedFuture(err);
        });
  }

  private ApiResponsePagination<List<MerchantDocumentResponse>> mapDocumentPagination(
      PagedResult<MerchantDocument> result,
      int page,
      int pageSize) {
    int totalRecords = result.getTotalRecords();
    int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
    List<MerchantDocumentResponse> data = result.getData().stream()
        .map(MerchantDocumentResponse::from)
        .toList();

    return new ApiResponsePagination<>(
        "success",
        "Documents found",
        data,
        new PaginationMeta(page, pageSize, totalPages, totalRecords));
  }

  private ApiResponsePagination<List<MerchantDocumentResponseDeleteAt>> mapDocumentPaginationDeleteAt(
      PagedResult<MerchantDocument> result,
      int page,
      int pageSize) {
    int totalRecords = result.getTotalRecords();
    int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
    List<MerchantDocumentResponseDeleteAt> data = result.getData().stream()
        .map(MerchantDocumentResponseDeleteAt::from)
        .toList();

    return new ApiResponsePagination<>(
        "success",
        "Documents found",
        data,
        new PaginationMeta(page, pageSize, totalPages, totalRecords));
  }
}
