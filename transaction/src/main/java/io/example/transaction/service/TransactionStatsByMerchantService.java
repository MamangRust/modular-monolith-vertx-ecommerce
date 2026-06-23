package io.example.transaction.service;

import java.util.List;

import io.example.transaction.domain.requests.FindMonthlyMerchantStatsRequest;
import io.example.transaction.domain.requests.FindYearlyMerchantStatsRequest;
import io.example.transaction.model.TransactionMonthlyAmountFailed;
import io.example.transaction.model.TransactionMonthlyAmountSuccess;
import io.example.transaction.model.TransactionMonthlyMethod;
import io.example.transaction.model.TransactionYearlyAmountFailed;
import io.example.transaction.model.TransactionYearlyAmountSuccess;
import io.example.transaction.model.TransactionYearlyMethod;
import io.vertx.core.Future;

public interface TransactionStatsByMerchantService {
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