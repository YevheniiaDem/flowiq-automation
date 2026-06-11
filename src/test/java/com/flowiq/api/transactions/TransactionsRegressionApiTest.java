package com.flowiq.api.transactions;

import com.flowiq.base.BaseRegressionApiTest;
import com.flowiq.factories.builders.TransactionRequestBuilder;
import com.flowiq.models.request.CreateTransactionRequest;
import com.flowiq.models.response.TransactionPageResponse;
import com.flowiq.models.response.TransactionResponse;
import com.flowiq.models.response.TransactionSummaryResponse;
import com.flowiq.support.TestCleanupManager;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Regression")
@Feature("Transactions")
public class TransactionsRegressionApiTest extends BaseRegressionApiTest {

    @Test(groups = {"regression", "api", "transactions"})
    @Story("Transaction CRUD")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User can create, update, read and delete a transaction")
    public void shouldCreateUpdateAndDeleteTransaction() {
        CreateTransactionRequest createRequest = TransactionRequestBuilder.expense()
                .amount(new BigDecimal("150.25"))
                .uniqueDescription()
                .today()
                .build();

        TransactionResponse created = transactionService.create(createRequest);
        TestCleanupManager.registerTransactionCleanup(transactionService, created.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo(createRequest.getDescription());

        TransactionResponse fetched = transactionService.getById(created.getId());
        assertThat(fetched.getAmount()).isEqualByComparingTo(createRequest.getAmount());

        var updateRequest = TransactionRequestBuilder.expense()
                .amount(new BigDecimal("175.00"))
                .description(createRequest.getDescription() + " updated")
                .today()
                .toUpdateRequest();
        TransactionResponse updated = transactionService.update(created.getId(), updateRequest);
        assertThat(updated.getAmount()).isEqualByComparingTo("175.00");

        transactionService.deleteById(created.getId());
        TestCleanupManager.clear();

        TransactionPageResponse list = transactionService.search(createRequest.getDescription());
        assertThat(list.getContent()).noneMatch(tx -> created.getId().equals(tx.getId()));
    }

    @Test(groups = {"regression", "api", "transactions"})
    @Story("Transaction filters")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Transactions can be filtered by type and searched by description")
    public void shouldFilterAndSearchTransactions() {
        CreateTransactionRequest request = TransactionRequestBuilder.income()
                .uniqueDescription()
                .today()
                .build();
        TransactionResponse created = transactionService.create(request);
        TestCleanupManager.registerTransactionCleanup(transactionService, created.getId());

        TransactionPageResponse filtered = transactionService.list(
                Map.of("type", "INCOME", "search", request.getDescription(), "page", 0, "size", 20));
        assertThat(filtered.getContent())
                .anyMatch(tx -> created.getId().equals(tx.getId()));
    }

    @Test(groups = {"regression", "api", "transactions"})
    @Story("Transaction summary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Transaction summary returns revenue, expenses and net profit")
    public void shouldReturnTransactionSummary() {
        TransactionSummaryResponse summary = transactionService.getSummary(Map.of());

        assertThat(summary.getTotalRevenue()).isNotNull();
        assertThat(summary.getTotalExpenses()).isNotNull();
        assertThat(summary.getNetProfit()).isNotNull();
    }
}
