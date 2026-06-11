package com.flowiq.api.transactions;

import com.flowiq.assertions.ApiAssertions;
import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseSmokeApiTest;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.SmokeSchemas;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.response.TransactionPageResponse;
import com.flowiq.models.response.TransactionResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Smoke")
@Feature("Transactions")
public class TransactionsSmokeApiTest extends BaseSmokeApiTest {

    @Test(groups = {"smoke", "api", "transactions"})
    @Story("Happy path")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Authenticated user can list transactions")
    public void shouldListTransactions() {
        ApiCallResult<TransactionPageResponse> result = transactionService.fetchList();

        assertHappyPath(result);
        assertThat(result.getBody().getContent()).isNotNull();
    }

    @Test(groups = {"smoke", "api", "transactions"})
    @Story("Authorization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Transactions endpoint requires JWT")
    public void shouldRejectUnauthenticatedAccess() {
        TokenManager.clear();
        assertUnauthorized(transactionService.fetchListUnauthorized());
    }

    @Test(groups = {"smoke", "api", "transactions"})
    @Story("Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Create transaction with missing required fields is rejected")
    public void shouldRejectInvalidCreatePayload() {
        ApiCallResult<TransactionResponse> result =
                transactionService.attemptCreate(TestDataFactory.invalidTransactionRequest());

        assertValidationError(result);
    }

    @Test(groups = {"smoke", "api", "transactions"})
    @Story("Schema")
    @Severity(SeverityLevel.NORMAL)
    @Description("Transaction list response matches JSON schema")
    public void shouldMatchListResponseSchema() {
        ApiCallResult<TransactionPageResponse> result = transactionService.fetchList();

        assertHappyPath(result);
        assertMatchesSchema(result, SmokeSchemas.TRANSACTION_PAGE);
    }
}
