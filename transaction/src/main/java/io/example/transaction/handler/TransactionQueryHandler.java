package io.example.transaction.handler;

import io.example.transaction.model.FindAllTransaction;
import io.example.transaction.model.FindAllTransactionByMerchant;
import io.example.transaction.service.TransactionQueryService;
import io.vertx.core.Future;
import pb.transaction.TransactionCommon.*;
import pb.transaction.TransactionQuery.*;
import pb.transaction.VertxTransactionQueryServiceGrpcServer;

public class TransactionQueryHandler implements VertxTransactionQueryServiceGrpcServer.TransactionQueryServiceApi {
    private final TransactionQueryService service;

    public TransactionQueryHandler(TransactionQueryService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponsePaginationTransaction> findAllTransactions(FindAllTransactionRequest req) {
        FindAllTransaction reqDto = FindAllTransaction.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getTransactions(reqDto)
                .map(res -> {
                    ApiResponsePaginationTransaction.Builder builder = ApiResponsePaginationTransaction.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null && res.data().getData() != null) {
                        builder.addAllData(res.data().getData().stream()
                                .map(ProtoConverter::toProtoResponse)
                                .toList());
                    }

                    if (res.data() != null) {
                        int currentPage = req.getPage() > 0 ? req.getPage() : 1;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        int totalRecords = res.data().getTotalRecords();
                        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

                        builder.setPagination(pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(currentPage)
                                .setPageSize(pageSize)
                                .setTotalPages(totalPages)
                                .setTotalRecords(totalRecords)
                                .build());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponsePaginationTransaction> findByMerchant(FindAllTransactionByMerchantRequest req) {
        FindAllTransactionByMerchant reqDto = FindAllTransactionByMerchant.builder()
                .merchantId(req.getMerchantId())
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getTransactionByMerchant(reqDto)
                .map(res -> {
                    ApiResponsePaginationTransaction.Builder builder = ApiResponsePaginationTransaction.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null && res.data().getData() != null) {
                        builder.addAllData(res.data().getData().stream()
                                .map(ProtoConverter::toProtoResponse)
                                .toList());
                    }

                    if (res.data() != null) {
                        int currentPage = req.getPage() > 0 ? req.getPage() : 1;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        int totalRecords = res.data().getTotalRecords();
                        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

                        builder.setPagination(pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(currentPage)
                                .setPageSize(pageSize)
                                .setTotalPages(totalPages)
                                .setTotalRecords(totalRecords)
                                .build());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseTransaction> findById(FindByIdTransactionRequest req) {
        return service.getTransactionById((long) req.getId())
                .map(res -> {
                    ApiResponseTransaction.Builder builder = ApiResponseTransaction.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponse(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseTransaction> findByOrderId(FindByOrderIdTransactionRequest req) {
        return service.getTransactionByOrderId((long) req.getOrderId())
                .map(res -> {
                    ApiResponseTransaction.Builder builder = ApiResponseTransaction.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null) {
                        builder.setData(ProtoConverter.toProtoResponse(res.data()));
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponsePaginationTransaction> findByActive(FindAllTransactionRequest req) {
        FindAllTransaction reqDto = FindAllTransaction.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getTransactionsActive(reqDto)
                .map(res -> {
                    ApiResponsePaginationTransaction.Builder builder = ApiResponsePaginationTransaction.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null && res.data().getData() != null) {
                        builder.addAllData(res.data().getData().stream()
                                .map(ProtoConverter::toProtoResponse)
                                .toList());
                    }

                    if (res.data() != null) {
                        int currentPage = req.getPage() > 0 ? req.getPage() : 1;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        int totalRecords = res.data().getTotalRecords();
                        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

                        builder.setPagination(pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(currentPage)
                                .setPageSize(pageSize)
                                .setTotalPages(totalPages)
                                .setTotalRecords(totalRecords)
                                .build());
                    }

                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponsePaginationTransactionDeleteAt> findByTrashed(FindAllTransactionRequest req) {
        FindAllTransaction reqDto = FindAllTransaction.builder()
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .search(req.getSearch())
                .build();

        return service.getTransactionsTrashed(reqDto)
                .map(res -> {
                    ApiResponsePaginationTransactionDeleteAt.Builder builder = ApiResponsePaginationTransactionDeleteAt.newBuilder()
                            .setStatus(res.status() != null ? res.status() : "error")
                            .setMessage(res.message() != null ? res.message() : "");

                    if (res.data() != null && res.data().getData() != null) {
                        builder.addAllData(res.data().getData().stream()
                                .map(ProtoConverter::toProtoResponseDeleteAt)
                                .toList());
                    }

                    if (res.data() != null) {
                        int currentPage = req.getPage() > 0 ? req.getPage() : 1;
                        int pageSize = req.getPageSize() > 0 ? req.getPageSize() : 10;
                        int totalRecords = res.data().getTotalRecords();
                        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

                        builder.setPagination(pb.Api.PaginationMeta.newBuilder()
                                .setCurrentPage(currentPage)
                                .setPageSize(pageSize)
                                .setTotalPages(totalPages)
                                .setTotalRecords(totalRecords)
                                .build());
                    }

                    return builder.build();
                });
    }
}
