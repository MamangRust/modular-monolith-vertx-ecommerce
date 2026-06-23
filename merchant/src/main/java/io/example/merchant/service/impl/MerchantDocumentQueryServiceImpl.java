package io.example.merchant.service.impl;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.example.common.domain.PagedResult;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.observability.TracingMetrics;
import io.example.common.service.RedisService;
import io.example.merchant.domain.requests.FindAllMerchantDocumentsRequest;
import io.example.merchant.model.MerchantDocument;
import io.example.merchant.model.MerchantDocumentResponse;
import io.example.merchant.model.MerchantDocumentResponseDeleteAt;
import io.example.merchant.repository.MerchantDocumentQueryRepository;
import io.example.merchant.service.MerchantDocumentQueryService;
import io.opentelemetry.api.common.Attributes;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MerchantDocumentQueryServiceImpl implements MerchantDocumentQueryService {
  private static final Logger log = LoggerFactory.getLogger(MerchantDocumentQueryServiceImpl.class);
  private static final ObjectMapper mapper = new ObjectMapper();
  private final MerchantDocumentQueryRepository repository;
  private final RedisService redis;
  private final TracingMetrics metrics;

  private static final String CACHE_PREFIX = "merchant_document:";
  private static final Duration CACHE_TTL = Duration.ofMinutes(10);

  private PagedResult<MerchantDocumentResponse> mapPagination(PagedResult<MerchantDocument> res) {
    List<MerchantDocumentResponse> data = res.getData().stream().map(MerchantDocumentResponse::from).toList();
    return new PagedResult<>(data, res.getTotalRecords());
  }

  private PagedResult<MerchantDocumentResponseDeleteAt> mapPaginationDeleteAt(PagedResult<MerchantDocument> res) {
    List<MerchantDocumentResponseDeleteAt> data = res.getData().stream().map(MerchantDocumentResponseDeleteAt::from)
        .toList();
    return new PagedResult<>(data, res.getTotalRecords());
  }

  @Override
  public Future<PagedResult<MerchantDocumentResponse>> getAllDocuments(FindAllMerchantDocumentsRequest req) {
    var ctx = metrics.startSpan("MerchantDocumentQueryService.getAllDocuments");
    String cacheKey = CACHE_PREFIX + "list:all:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
        + req.getPage() + ":" + req.getPageSize();

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<MerchantDocument> typedCached = mapper.readValue(jsonStr,
                  new TypeReference<PagedResult<MerchantDocument>>() {
                  });
              return Future.succeededFuture(mapPagination(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached documents: {}", e.getMessage());
            }
          }
          return repository.getDocuments(req)
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPagination);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getAllDocuments", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getAllDocuments", e.getMessage()));
  }

  @Override
  public Future<PagedResult<MerchantDocumentResponse>> getActiveDocuments(FindAllMerchantDocumentsRequest req) {
    var ctx = metrics.startSpan("MerchantDocumentQueryService.getActiveDocuments");
    String cacheKey = CACHE_PREFIX + "list:active:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
        + req.getPage() + ":" + req.getPageSize();

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<MerchantDocument> typedCached = mapper.readValue(jsonStr,
                  new TypeReference<PagedResult<MerchantDocument>>() {
                  });
              return Future.succeededFuture(mapPagination(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached active documents: {}", e.getMessage());
            }
          }
          return repository.getActiveDocuments(req)
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPagination);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getActiveDocuments", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getActiveDocuments", e.getMessage()));
  }

  @Override
  public Future<PagedResult<MerchantDocumentResponseDeleteAt>> getTrashedDocuments(
      FindAllMerchantDocumentsRequest req) {
    var ctx = metrics.startSpan("MerchantDocumentQueryService.getTrashedDocuments");
    String cacheKey = CACHE_PREFIX + "list:trashed:" + (req.getSearch() != null ? req.getSearch() : "") + ":"
        + req.getPage() + ":" + req.getPageSize();

    return redis.get(cacheKey)
        .compose(jsonStr -> {
          if (jsonStr != null && !jsonStr.isEmpty()) {
            try {
              PagedResult<MerchantDocument> typedCached = mapper.readValue(jsonStr,
                  new TypeReference<PagedResult<MerchantDocument>>() {
                  });
              return Future.succeededFuture(mapPaginationDeleteAt(typedCached));
            } catch (Exception e) {
              log.warn("Failed to deserialize cached trashed documents: {}", e.getMessage());
            }
          }
          return repository.getTrashedDocuments(req)
              .compose(res -> redis.setJson(cacheKey, res, CACHE_TTL).map(v -> res))
              .map(this::mapPaginationDeleteAt);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getTrashedDocuments", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getTrashedDocuments", e.getMessage()));
  }

  @Override
  public Future<MerchantDocumentResponse> getDocumentById(Long documentId) {
    var ctx = metrics.startSpan("MerchantDocumentQueryService.getDocumentById",
        Attributes.builder().put("document.id", (long) documentId).build());
    String key = CACHE_PREFIX + "id:" + documentId;

    return redis.getJson(key, MerchantDocument.class)
        .compose(cached -> {
          if (cached != null) {
            return Future.succeededFuture(MerchantDocumentResponse.from(cached));
          }
          return repository.getDocumentById(documentId)
              .compose(db -> {
                if (db == null) {
                  return Future.<MerchantDocument>failedFuture(new NotFoundException("Merchant document not found"));
                }
                return redis.setJson(key, db, CACHE_TTL).<MerchantDocument>map(v -> db);
              })
              .map(MerchantDocumentResponse::from);
        })
        .onSuccess(r -> metrics.completeSpanSuccess(ctx, "getDocumentById", "Success"))
        .onFailure(e -> metrics.completeSpanError(ctx, "getDocumentById", e.getMessage()));
  }
}