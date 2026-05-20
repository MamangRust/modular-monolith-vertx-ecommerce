package io.example.transaction.repository;

import java.util.List;
import io.example.transaction.model.*;
import io.vertx.core.Future;

public interface TransactionStatsByMerchantRepository {
    Future<List<TransactionMonthlyAmountSuccess>> getMonthlyAmountTransactionSuccessByMerchant(Integer merchantId, int year, int month);
    Future<List<TransactionYearlyAmountSuccess>> getYearlyAmountTransactionSuccessByMerchant(Integer merchantId, int year);
    Future<List<TransactionMonthlyAmountFailed>> getMonthlyAmountTransactionFailedByMerchant(Integer merchantId, int year, int month);
    Future<List<TransactionYearlyAmountFailed>> getYearlyAmountTransactionFailedByMerchant(Integer merchantId, int year);
    Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsByMerchantSuccess(Integer merchantId, int year, int month);
    Future<List<TransactionMonthlyMethod>> getMonthlyTransactionMethodsByMerchantFailed(Integer merchantId, int year, int month);
    Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsByMerchantSuccess(Integer merchantId, int year);
    Future<List<TransactionYearlyMethod>> getYearlyTransactionMethodsByMerchantFailed(Integer merchantId, int year);
}
