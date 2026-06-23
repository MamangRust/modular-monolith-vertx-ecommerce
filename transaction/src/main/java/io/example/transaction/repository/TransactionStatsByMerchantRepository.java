package io.example.transaction.repository;

import java.util.List;
import io.example.transaction.model.*;
import io.example.transaction.domain.requests.FindMonthlyMerchantStatsRequest;
import io.example.transaction.domain.requests.FindYearlyMerchantStatsRequest;
import io.vertx.core.Future;

public interface TransactionStatsByMerchantRepository {
    Future<List<TransactionMonthlyAmountSuccess>> getMonthlyAmountTransactionSuccessByMerchant(
            FindMonthlyMerchantStatsRequest req);

    Future<List<TransactionYearlyAmountSuccess>> getYearlyAmountTransactionSuccessByMerchant(
            FindYearlyMerchantStatsRequest req);

    Future<List<TransactionMonthlyAmountFailed>> getMonthlyAmountTransactionFailedByMerchant(
            FindMonthlyMerchantStatsRequest req);

    Future<List<TransactionYearlyAmountFailed>> getYearlyAmountTransactionFailedByMerchant(
            FindYearlyMerchantStatsRequest req);

    Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsByMerchantSuccess(
            FindMonthlyMerchantStatsRequest req);

    Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsByMerchantFailed(
            FindMonthlyMerchantStatsRequest req);

    Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsByMerchantSuccess(
            FindYearlyMerchantStatsRequest req);

    Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsByMerchantFailed(
            FindYearlyMerchantStatsRequest req);
}
