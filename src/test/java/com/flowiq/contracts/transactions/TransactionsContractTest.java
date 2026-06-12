package com.flowiq.contracts.transactions;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.contracts.ContractAssertions;
import com.flowiq.contracts.ContractSchemas;
import com.flowiq.contracts.base.BaseContractTest;
import com.flowiq.models.request.CreateTransactionRequest;
import com.flowiq.models.response.TransactionPageResponse;
import com.flowiq.models.response.TransactionSummaryResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("Contract Testing")
@Feature("Transactions")
public class TransactionsContractTest extends BaseContractTest {

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    @Test(groups = {"contract", "transactions"})
    @Story("GET /api/transactions")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Transactions page response matches contract schema")
    public void listTransactionsShouldMatchContract() {
        ApiCallResult<TransactionPageResponse> result = transactionService.fetchList();

        ContractAssertions.assertAllRequired(result, 200, ContractSchemas.TRANSACTIONS_PAGE,
                "content", "page", "size", "totalElements", "totalPages");

        if (result.getResponse() != null) {
            ContractAssertions.assertEnumValuesInNestedArray(
                    result.getResponse(), "content", "type", CreateTransactionRequest.TransactionTypeDto.class);
        }
    }

    @Test(groups = {"contract", "transactions"})
    @Story("GET /api/transactions/summary")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Transaction summary response matches contract schema")
    public void transactionSummaryShouldMatchContract() {
        ApiCallResult<TransactionSummaryResponse> result = transactionService.fetchSummary();

        ContractAssertions.assertAllRequired(result, 200, ContractSchemas.TRANSACTIONS_SUMMARY,
                "totalRevenue", "totalExpenses", "netProfit", "transactionCount");
    }
}
