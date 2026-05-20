package io.example.transaction.service;

import java.util.List;

import io.example.common.model.ApiResponse;
import io.example.transaction.model.*;
import io.vertx.core.Future;

public interface TransactionStatsService {
    Future<ApiResponse<List<TransactionMonthlyAmountSuccess>>> getMonthlyAmountTransactionSuccess(int year, int month);
    Future<ApiResponse<List<TransactionYearlyAmountSuccess>>> getYearlyAmountTransactionSuccess(int year);
    Future<ApiResponse<List<TransactionMonthlyAmountFailed>>> getMonthlyAmountTransactionFailed(int year, int month);
    Future<ApiResponse<List<TransactionYearlyAmountFailed>>> getYearlyAmountTransactionFailed(int year);
    Future<ApiResponse<List<TransactionMonthlyMethod>>> getMonthlyTransactionMethodsSuccess(int year, int month);
    Future<ApiResponse<List<TransactionMonthlyMethod>>> getMonthlyTransactionMethodsFailed(int year, int month);
    Future<ApiResponse<List<TransactionYearlyMethod>>> getYearlyTransactionMethodsSuccess(int year);
    Future<ApiResponse<List<TransactionYearlyMethod>>> getYearlyTransactionMethodsFailed(int year);
}
