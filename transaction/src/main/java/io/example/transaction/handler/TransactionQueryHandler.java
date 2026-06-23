package io.example.transaction.handler;

import io.example.common.domain.PagedResult;
import io.example.common.grpc.GrpcExceptionMapper;
import io.example.transaction.domain.requests.FindAllTransaction;
import io.example.transaction.domain.requests.FindAllTransactionByMerchant;
import io.example.transaction.model.Transaction;
import io.example.transaction.service.TransactionQueryService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.transaction.TransactionCommon.ApiResponsePaginationTransaction;
import pb.transaction.TransactionCommon.ApiResponsePaginationTransactionDeleteAt;
import pb.transaction.TransactionCommon.ApiResponseTransaction;
import pb.transaction.TransactionCommon.FindByIdTransactionRequest;
import pb.transaction.TransactionQuery.FindAllTransactionRequest;
import pb.transaction.TransactionQuery.FindAllTransactionByMerchantRequest;
import pb.transaction.TransactionQuery.FindByOrderIdTransactionRequest;
import pb.transaction.VertxTransactionQueryServiceGrpcServer.TransactionQueryServiceApi;

@RequiredArgsConstructor
public class TransactionQueryHandler implements TransactionQueryServiceApi {
        private final TransactionQueryService service;

        private FindAllTransaction toDomainReq(FindAllTransactionRequest req) {
                return FindAllTransaction.builder()
                                .search(req.getSearch())
                                .page(req.getPage() > 0 ? req.getPage() : 1)
                                .pageSize(req.getPageSize() > 0 ? req.getPageSize() : 10)
                                .build();
        }

        private FindAllTransactionByMerchant toDomainReqByMerchant(FindAllTransactionByMerchantRequest req) {
                return FindAllTransactionByMerchant.builder()
                                .merchantId(req.getMerchantId())
                                .search(req.getSearch())
                                .page(req.getPage() > 0 ? req.getPage() : 1)
                                .pageSize(req.getPageSize() > 0 ? req.getPageSize() : 10)
                                .build();
        }

        private pb.Api.PaginationMeta toMeta(int totalRecords, int page, int pageSize) {
                int currentPage = page > 0 ? page : 1;
                int size = pageSize > 0 ? pageSize : 10;
                int totalPages = size > 0 ? (int) Math.ceil((double) totalRecords / size) : 0;
                return pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(currentPage)
                                .setPageSize(size)
                                .setTotalPages(totalPages)
                                .setTotalRecords(totalRecords)
                                .build();
        }

        @Override
        public Future<ApiResponsePaginationTransaction> findAllTransactions(FindAllTransactionRequest req) {
                FindAllTransaction domainReq = toDomainReq(req);
                Future<PagedResult<Transaction>> transactionsFuture = service.getTransactions(domainReq);
                return transactionsFuture
                                .map(res -> ApiResponsePaginationTransaction.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponse)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
        }

        @Override
        public Future<ApiResponsePaginationTransaction> findByActive(FindAllTransactionRequest req) {
                FindAllTransaction domainReq = toDomainReq(req);
                Future<PagedResult<Transaction>> activeFuture = service.getTransactionsActive(domainReq);
                return activeFuture
                                .map(res -> ApiResponsePaginationTransaction.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponse)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
        }

        @Override
        public Future<ApiResponsePaginationTransactionDeleteAt> findByTrashed(FindAllTransactionRequest req) {
                FindAllTransaction domainReq = toDomainReq(req);
                Future<PagedResult<Transaction>> trashedFuture = service.getTransactionsTrashed(domainReq);
                return trashedFuture
                                .map(res -> ApiResponsePaginationTransactionDeleteAt.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream()
                                                                .map(ProtoConverter::toProtoResponseDeleteAt).toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
        }

        @Override
        public Future<ApiResponsePaginationTransaction> findByMerchant(FindAllTransactionByMerchantRequest req) {
                FindAllTransactionByMerchant domainReq = toDomainReqByMerchant(req);
                Future<PagedResult<Transaction>> merchantFuture = service.getTransactionByMerchant(domainReq);
                return merchantFuture
                                .map(res -> ApiResponsePaginationTransaction.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .addAllData(res.getData().stream().map(ProtoConverter::toProtoResponse)
                                                                .toList())
                                                .setPagination(toMeta(res.getTotalRecords(), domainReq.getPage(),
                                                                domainReq.getPageSize()))
                                                .build())
                                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
        }

        @Override
        public Future<ApiResponseTransaction> findById(FindByIdTransactionRequest req) {
                return service.getTransactionById((long) req.getId())
                                .map(res -> ApiResponseTransaction.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponse(res))
                                                .build())
                                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
        }

        @Override
        public Future<ApiResponseTransaction> findByOrderId(FindByOrderIdTransactionRequest req) {
                return service.getTransactionByOrderId((long) req.getOrderId())
                                .map(res -> ApiResponseTransaction.newBuilder()
                                                .setStatus("success")
                                                .setMessage("OK")
                                                .setData(ProtoConverter.toProtoResponse(res))
                                                .build())
                                .recover(err -> GrpcExceptionMapper.toFailedFuture(err));
        }
}