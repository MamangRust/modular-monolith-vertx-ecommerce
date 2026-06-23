package io.example.transaction.service;

import java.util.List;

import io.example.transaction.domain.requests.FindMonthlyStatsRequest;
import io.example.transaction.model.*;
import io.vertx.core.Future;

public interface TransactionStatsService {
    Future<List<TransactionMonthlyAmountSuccess>> getMonthlyAmountTransactionSuccess(FindMonthlyStatsRequest req);

    Future<List<TransactionYearlyAmountSuccess>> getYearlyAmountTransactionSuccess(int year);

    Future<List<TransactionMonthlyAmountFailed>> getMonthlyAmountTransactionFailed(FindMonthlyStatsRequest req);

    Future<List<TransactionYearlyAmountFailed>> getYearlyAmountTransactionFailed(int year);

    Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsSuccess(FindMonthlyStatsRequest req);

    Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsFailed(FindMonthlyStatsRequest req);

    Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsSuccess(int year);

    Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsFailed(int year);
}