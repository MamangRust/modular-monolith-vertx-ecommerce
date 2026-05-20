package io.example.transaction.repository;

import java.util.List;
import io.example.transaction.model.*;
import io.vertx.core.Future;

public interface TransactionStatsRepository {
    Future<List<TransactionMonthlyAmountSuccess>> getMonthlyAmountTransactionSuccess(int year, int month);
    Future<List<TransactionYearlyAmountSuccess>> getYearlyAmountTransactionSuccess(int year);
    Future<List<TransactionMonthlyAmountFailed>> getMonthlyAmountTransactionFailed(int year, int month);
    Future<List<TransactionYearlyAmountFailed>> getYearlyAmountTransactionFailed(int year);
    Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsSuccess(int year, int month);
    Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsFailed(int year, int month);
    Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsSuccess(int year);
    Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsFailed(int year);
}
