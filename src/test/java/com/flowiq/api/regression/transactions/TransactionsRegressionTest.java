package com.flowiq.api.regression.transactions;

import com.flowiq.api.regression.base.BaseRegressionApiTest;
import com.flowiq.api.regression.support.RegressionAssertions;
import com.flowiq.api.regression.support.RegressionDataProviders;
import com.flowiq.auth.TokenManager;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.factories.builders.TransactionRequestBuilder;
import com.flowiq.models.request.CreateTransactionRequest;
import com.flowiq.models.request.RegisterRequest;
import com.flowiq.models.response.TransactionPageResponse;
import com.flowiq.models.response.TransactionResponse;
import com.flowiq.models.response.TransactionSummaryResponse;
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

@Epic("API Regression")
@Feature("Transactions")
public class TransactionsRegressionTest extends BaseRegressionApiTest {

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Transaction CRUD")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User can create an expense transaction")
    public void shouldCreateExpenseTransaction() {
        CreateTransactionRequest request = TransactionRequestBuilder.expense()
                .amount(new BigDecimal("150.25"))
                .uniqueDescription()
                .today()
                .build();

        TransactionResponse created = transactionService.create(request);
        TestCleanupManager.registerTransactionCleanup(transactionService, created.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getType()).isEqualTo(CreateTransactionRequest.TransactionTypeDto.EXPENSE);
        assertThat(created.getAmount()).isEqualByComparingTo(request.getAmount());
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Transaction CRUD")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User can create an income transaction")
    public void shouldCreateIncomeTransaction() {
        CreateTransactionRequest request = TransactionRequestBuilder.income()
                .uniqueDescription()
                .today()
                .build();

        TransactionResponse created = transactionService.create(request);
        TestCleanupManager.registerTransactionCleanup(transactionService, created.getId());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getType()).isEqualTo(CreateTransactionRequest.TransactionTypeDto.INCOME);
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Transaction CRUD")
    @Severity(SeverityLevel.BLOCKER)
    @Description("GET /transactions/{id} returns the created transaction")
    public void shouldGetTransactionById() {
        TransactionResponse created = createTrackedTransaction();

        TransactionResponse fetched = transactionService.getById(created.getId());

        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getDescription()).isEqualTo(created.getDescription());
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Transaction CRUD")
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

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Transaction CRUD")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User can create, update, read and delete a transaction")
    public void shouldCreateUpdateAndDeleteTransaction() {
        CreateTransactionRequest createRequest = TransactionRequestBuilder.expense()
                .amount(new BigDecimal("175.00"))
                .uniqueDescription()
                .today()
                .build();

        TransactionResponse created = transactionService.create(createRequest);
        TestCleanupManager.registerTransactionCleanup(transactionService, created.getId());

        TransactionResponse fetched = transactionService.getById(created.getId());
        assertThat(fetched.getAmount()).isEqualByComparingTo(createRequest.getAmount());

        var updateRequest = TransactionRequestBuilder.expense()
                .amount(new BigDecimal("180.00"))
                .description(createRequest.getDescription() + " updated")
                .today()
                .toUpdateRequest();
        TransactionResponse updated = transactionService.update(created.getId(), updateRequest);
        assertThat(updated.getAmount()).isEqualByComparingTo("180.00");

        transactionService.deleteById(created.getId());
        TestCleanupManager.clear();

        RegressionAssertions.assertNotFound(transactionService.fetchById(created.getId()));
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Transaction CRUD")
    @Severity(SeverityLevel.CRITICAL)
    @Description("DELETE /transactions/{id} removes the transaction")
    public void shouldDeleteTransaction() {
        TransactionResponse created = transactionService.create(
                TransactionRequestBuilder.income().uniqueDescription().today().build());

        transactionService.deleteById(created.getId());

        RegressionAssertions.assertNotFound(transactionService.fetchById(created.getId()));
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"},
            dataProvider = "paginationPages", dataProviderClass = RegressionDataProviders.class)
    @Story("Pagination")
    @Severity(SeverityLevel.NORMAL)
    @Description("Transactions list supports page parameter")
    public void shouldPaginateTransactionsByPage(int page) {
        createTrackedTransaction();

        TransactionPageResponse response = transactionService.list(Map.of("page", page, "size", 5));

        assertThat(response.getPage()).isEqualTo(page);
        assertThat(response.getContent()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"},
            dataProvider = "pageSizes", dataProviderClass = RegressionDataProviders.class)
    @Story("Pagination")
    @Severity(SeverityLevel.NORMAL)
    @Description("Transactions list supports size parameter")
    public void shouldPaginateTransactionsByPageSize(int size) {
        TransactionPageResponse response = transactionService.list(Map.of("page", 0, "size", size));

        assertThat(response.getSize()).isEqualTo(size);
        assertThat(response.getContent()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"},
            dataProvider = "transactionSorts", dataProviderClass = RegressionDataProviders.class)
    @Story("Sorting")
    @Severity(SeverityLevel.NORMAL)
    @Description("Transactions list supports sort parameter")
    public void shouldSortTransactions(String sort) {
        TransactionPageResponse sorted = transactionService.list(
                Map.of("sort", sort, "page", 0, "size", 50));

        assertThat(sorted.getContent()).isNotNull();
        if ("amount,asc".equals(sort) && sorted.getContent().size() > 1) {
            List<BigDecimal> amounts = sorted.getContent().stream()
                    .map(TransactionResponse::getAmount)
                    .toList();
            assertThat(amounts).isSortedAccordingTo(Comparator.naturalOrder());
        }
        if ("amount,desc".equals(sort) && sorted.getContent().size() > 1) {
            List<BigDecimal> amounts = sorted.getContent().stream()
                    .map(TransactionResponse::getAmount)
                    .toList();
            assertThat(amounts).isSortedAccordingTo(Comparator.reverseOrder());
        }
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"},
            dataProvider = "transactionTypes", dataProviderClass = RegressionDataProviders.class)
    @Story("Transaction filters")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Transactions can be filtered by type")
    public void shouldFilterTransactionsByType(String type) {
        CreateTransactionRequest request = "INCOME".equals(type)
                ? TransactionRequestBuilder.income().uniqueDescription().today().build()
                : TransactionRequestBuilder.expense().uniqueDescription().today().build();
        TransactionResponse created = transactionService.create(request);
        TestCleanupManager.registerTransactionCleanup(transactionService, created.getId());

        TransactionPageResponse filtered = transactionService.list(
                Map.of("type", type, "page", 0, "size", 50));

        assertThat(filtered.getContent())
                .allMatch(tx -> type.equals(tx.getType().name()));
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
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

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
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

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Transaction summary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Transaction summary returns revenue, expenses and net profit")
    public void shouldReturnTransactionSummary() {
        TransactionSummaryResponse summary = transactionService.getSummary(Map.of());

        assertThat(summary.getTotalRevenue()).isNotNull();
        assertThat(summary.getTotalExpenses()).isNotNull();
        assertThat(summary.getNetProfit()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Transaction summary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Transaction summary supports date range filters")
    public void shouldReturnTransactionSummaryForDateRange() {
        String today = DateUtils.today();
        TransactionSummaryResponse summary = transactionService.getSummary(Map.of(
                "dateFrom", today,
                "dateTo", today
        ));

        assertThat(summary.getTotalRevenue()).isNotNull();
        assertThat(summary.getTotalExpenses()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("User isolation")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User cannot access another user's transaction")
    public void shouldEnforceUserIsolation() {
        long transactionId = createTrackedTransaction().getId();
        RegisterRequest secondary = registerSecondaryUser();
        loginAsSecondaryUser(secondary);

        RegressionAssertions.assertForbidden(transactionService.fetchById(transactionId));

        restoreDefaultUserSession();
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Not found")
    @Severity(SeverityLevel.NORMAL)
    @Description("Non-existent transaction id returns 404")
    public void shouldReturnNotFoundForInvalidId() {
        RegressionAssertions.assertNotFound(transactionService.fetchById(INVALID_ID));
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated transaction list request is rejected")
    public void shouldRejectUnauthorizedListAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(transactionService.fetchListUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated transaction get-by-id request is rejected")
    public void shouldRejectUnauthorizedGetById() {
        long transactionId = createTrackedTransaction().getId();
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(transactionService.fetchByIdUnauthorized(transactionId));
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated transaction summary request is rejected")
    public void shouldRejectUnauthorizedSummaryAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(transactionService.fetchSummaryUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated transaction create request is rejected")
    public void shouldRejectUnauthorizedCreateAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(
                transactionService.attemptCreateUnauthorized(TransactionRequestBuilder.expense().uniqueDescription().today().build()));
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Validation errors")
    @Severity(SeverityLevel.NORMAL)
    @Description("Invalid transaction payload returns 400/422")
    public void shouldRejectInvalidTransactionPayload() {
        RegressionAssertions.assertValidationError(
                transactionService.attemptCreate(TestDataFactory.invalidTransactionRequest()));
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Validation errors")
    @Severity(SeverityLevel.NORMAL)
    @Description("Transaction with negative amount is rejected")
    public void shouldRejectNegativeAmount() {
        CreateTransactionRequest request = TransactionRequestBuilder.expense()
                .amount(new BigDecimal("-10.00"))
                .uniqueDescription()
                .today()
                .build();

        RegressionAssertions.assertValidationError(transactionService.attemptCreate(request));
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Pagination validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Invalid pagination parameters are rejected or handled safely")
    public void shouldRejectInvalidPagination() {
        var result = transactionService.fetchList(Map.of("page", -1, "size", 0));
        assertThat(result.getStatusCode()).isIn(200, 400, 422);
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("List transactions")
    @Severity(SeverityLevel.NORMAL)
    @Description("Default transactions list returns paginated content")
    public void shouldListTransactionsWithDefaults() {
        TransactionPageResponse page = transactionService.list();

        assertThat(page.getContent()).isNotNull();
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Not found")
    @Severity(SeverityLevel.NORMAL)
    @Description("Updating non-existent transaction returns 404")
    public void shouldRejectUpdateForInvalidId() {
        var updateRequest = TransactionRequestBuilder.expense()
                .uniqueDescription()
                .today()
                .toUpdateRequest();

        RegressionAssertions.assertNotFound(
                transactionService.attemptUpdate(INVALID_ID, updateRequest));
    }

    @Test(groups = {"api-regression", "regression", "api", "transactions"})
    @Story("Not found")
    @Severity(SeverityLevel.NORMAL)
    @Description("Deleting non-existent transaction returns 404")
    public void shouldRejectDeleteForInvalidId() {
        RegressionAssertions.assertNotFound(transactionService.attemptDeleteById(INVALID_ID));
    }

}
