package io.example.transaction.service;

import java.util.List;

import io.example.common.model.ApiResponse;
import io.example.transaction.model.*;
import io.vertx.core.Future;

public interface TransactionStatsByMerchantService {
    Future<ApiResponse<List<TransactionMonthlyAmountSuccess>>> getMonthlyAmountTransactionSuccessByMerchant(Integer merchantId, int year, int month);
    Future<ApiResponse<List<TransactionYearlyAmountSuccess>>> getYearlyAmountTransactionSuccessByMerchant(Integer merchantId, int year);
    Future<ApiResponse<List<TransactionMonthlyAmountFailed>>> getMonthlyAmountTransactionFailedByMerchant(Integer merchantId, int year, int month);
    Future<ApiResponse<List<TransactionYearlyAmountFailed>>> getYearlyAmountTransactionFailedByMerchant(Integer merchantId, int year);
    Future<ApiResponse<List<TransactionMonthlyMethod>>> getMonthlyTransactionMethodsByMerchantSuccess(Integer merchantId, int year, int month);
    Future<ApiResponse<List<TransactionMonthlyMethod>>> getMonthlyTransactionMethodsByMerchantFailed(Integer merchantId, int year, int month);
    Future<ApiResponse<List<TransactionYearlyMethod>>> getYearlyTransactionMethodsByMerchantSuccess(Integer merchantId, int year);
    Future<ApiResponse<List<TransactionYearlyMethod>>> getYearlyTransactionMethodsByMerchantFailed(Integer merchantId, int year);
}
