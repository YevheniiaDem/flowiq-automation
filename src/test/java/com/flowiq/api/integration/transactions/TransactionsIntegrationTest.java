package com.flowiq.api.integration.transactions;

import com.flowiq.api.integration.base.BaseApiIntegrationTest;
import com.flowiq.api.integration.support.IntegrationAssertions;
import com.flowiq.auth.TokenManager;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.factories.builders.TransactionRequestBuilder;
import com.flowiq.models.request.CreateTransactionRequest;
import com.flowiq.models.request.RegisterRequest;
import com.flowiq.models.response.TransactionPageResponse;
import com.flowiq.models.response.TransactionResponse;
import com.flowiq.support.TestCleanupManager;
import com.flowiq.utils.DateUtils;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Integration")
@Feature("Transactions")
public class TransactionsIntegrationTest extends BaseApiIntegrationTest {

    @Test(groups = {"api-integration", "api", "transactions"})
    @Story("Create transaction")
    @Severity(SeverityLevel.BLOCKER)
    @Description("POST /transactions creates a transaction with all required fields")
    public void shouldCreateTransaction() {
        CreateTransactionRequest request = TransactionRequestBuilder.expense()
                .amount(new BigDecimal("99.99"))
                .uniqueDescription()
                .today()
                .build();

        TransactionResponse created = transactionService.create(request);
        TestCleanupManager.registerTransactionCleanup(transactionService, created.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getType()).isEqualTo(request.getType());
        assertThat(created.getAmount()).isEqualByComparingTo(request.getAmount());
        assertThat(created.getDescription()).isEqualTo(request.getDescription());
    }

    @Test(groups = {"api-integration", "api", "transactions"})
    @Story("Get by id")
    @Severity(SeverityLevel.BLOCKER)
    @Description("GET /transactions/{id} returns the created transaction")
    public void shouldGetTransactionById() {
        TransactionResponse created = createTrackedTransaction();

        TransactionResponse fetched = transactionService.getById(created.getId());

        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getDescription()).isEqualTo(created.getDescription());
    }

    @Test(groups = {"api-integration", "api", "transactions"})
    @Story("Update transaction")
    @Severity(SeverityLevel.CRITICAL)
    @Description("PUT /transactions/{id} updates transaction fields")
    public void shouldUpdateTransaction() {
        TransactionResponse created = createTrackedTransaction();
        var updateRequest = TransactionRequestBuilder.expense()
                .amount(new BigDecimal("200.00"))
                .description(created.getDescription() + " updated")
                .today()
                .toUpdateRequest();

        TransactionResponse updated = transactionService.update(created.getId(), updateRequest);

        assertThat(updated.getAmount()).isEqualByComparingTo("200.00");
        assertThat(updated.getDescription()).endsWith("updated");
    }

    @Test(groups = {"api-integration", "api", "transactions"})
    @Story("Delete transaction")
    @Severity(SeverityLevel.CRITICAL)
    @Description("DELETE /transactions/{id} removes the transaction")
    public void shouldDeleteTransaction() {
        TransactionResponse created = transactionService.create(
                TransactionRequestBuilder.income().uniqueDescription().today().build());
        long id = created.getId();

        transactionService.deleteById(id);

        IntegrationAssertions.assertNotFound(transactionService.fetchById(id));
    }

    @Test(groups = {"api-integration", "api", "transactions"})
    @Story("Pagination")
    @Severity(SeverityLevel.NORMAL)
    @Description("Transactions list supports page and size parameters")
    public void shouldPaginateTransactions() {
        createTrackedTransaction();

        TransactionPageResponse page0 = transactionService.list(Map.of("page", 0, "size", 5));
        TransactionPageResponse page1 = transactionService.list(Map.of("page", 1, "size", 5));

        assertThat(page0.getPage()).isZero();
        assertThat(page0.getSize()).isEqualTo(5);
        assertThat(page0.getContent()).isNotNull();
        assertThat(page1.getPage()).isEqualTo(1);
    }

    @Test(groups = {"api-integration", "api", "transactions"})
    @Story("Sorting ASC")
    @Severity(SeverityLevel.NORMAL)
    @Description("Transactions can be sorted by amount ascending")
    public void shouldSortTransactionsAscending() {
        TransactionResponse low = transactionService.create(
                TransactionRequestBuilder.expense().amount(new BigDecimal("10.00")).uniqueDescription().today().build());
        TransactionResponse high = transactionService.create(
                TransactionRequestBuilder.expense().amount(new BigDecimal("9999.00")).uniqueDescription().today().build());
        TestCleanupManager.registerTransactionCleanup(transactionService, low.getId());
        TestCleanupManager.registerTransactionCleanup(transactionService, high.getId());

        TransactionPageResponse sorted = transactionService.list(
                Map.of("sort", "amount,asc", "page", 0, "size", 100));

        List<BigDecimal> amounts = sorted.getContent().stream()
                .map(TransactionResponse::getAmount)
                .toList();
        assertThat(amounts).isSortedAccordingTo(Comparator.naturalOrder());
    }

    @Test(groups = {"api-integration", "api", "transactions"})
    @Story("Sorting DESC")
    @Severity(SeverityLevel.NORMAL)
    @Description("Transactions can be sorted by amount descending")
    public void shouldSortTransactionsDescending() {
        TransactionPageResponse sorted = transactionService.list(
                Map.of("sort", "amount,desc", "page", 0, "size", 50));

        List<BigDecimal> amounts = sorted.getContent().stream()
                .map(TransactionResponse::getAmount)
                .toList();
        assertThat(amounts).isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test(groups = {"api-integration", "api", "transactions"})
    @Story("Search")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Search filter returns transactions matching description")
    public void shouldSearchTransactionsByDescription() {
        String uniqueToken = "search-" + System.currentTimeMillis();
        TransactionResponse created = transactionService.create(
                TransactionRequestBuilder.expense().description(uniqueToken).today().build());
        TestCleanupManager.registerTransactionCleanup(transactionService, created.getId());

        TransactionPageResponse results = transactionService.search(uniqueToken);

        assertThat(results.getContent())
                .anyMatch(tx -> created.getId().equals(tx.getId()));
    }

    @Test(groups = {"api-integration", "api", "transactions"})
    @Story("Date filters")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Transactions can be filtered by dateFrom and dateTo")
    public void shouldFilterTransactionsByDateRange() {
        TransactionResponse created = createTrackedTransaction();
        String today = DateUtils.today();

        TransactionPageResponse filtered = transactionService.list(Map.of(
                "dateFrom", today,
                "dateTo", today,
                "page", 0,
                "size", 50
        ));

        assertThat(filtered.getContent())
                .anyMatch(tx -> created.getId().equals(tx.getId()));
    }

    @Test(groups = {"api-integration", "api", "transactions"})
    @Story("Validation errors")
    @Severity(SeverityLevel.NORMAL)
    @Description("Invalid transaction payload returns 400/422")
    public void shouldRejectInvalidTransactionPayload() {
        IntegrationAssertions.assertValidationError(
                transactionService.attemptCreate(TestDataFactory.invalidTransactionRequest()));
    }

    @Test(groups = {"api-integration", "api", "transactions"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated requests to transactions are rejected")
    public void shouldRejectUnauthorizedAccess() {
        TokenManager.clear();
        IntegrationAssertions.assertUnauthorized(transactionService.fetchListUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-integration", "api", "transactions"})
    @Story("User isolation")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User cannot access another user's transaction")
    public void shouldEnforceUserIsolation() {
        long transactionId = createTransactionForIsolationTest();
        RegisterRequest secondary = registerSecondaryUser();
        loginAsSecondaryUser(secondary);

        IntegrationAssertions.assertForbiddenOrNotFound(transactionService.fetchById(transactionId));

        restoreDefaultUserSession();
    }
}
