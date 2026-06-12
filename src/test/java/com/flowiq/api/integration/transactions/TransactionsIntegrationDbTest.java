package com.flowiq.api.integration.transactions;

import com.flowiq.api.integration.base.BaseApiIntegrationDbTest;
import com.flowiq.factories.builders.TransactionRequestBuilder;
import com.flowiq.models.response.TransactionResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Integration")
@Feature("Transactions DB Consistency")
public class TransactionsIntegrationDbTest extends BaseApiIntegrationDbTest {

    @Test(groups = {"api-integration", "api-integration-db", "transactions"})
    @Story("Data consistency")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Transaction created via API is persisted in Testcontainer database")
    public void shouldPersistCreatedTransactionInDatabase() {
        var request = TransactionRequestBuilder.expense()
                .amount(new BigDecimal("444.44"))
                .uniqueDescription()
                .today()
                .build();

        TransactionResponse created = transactionService.create(request);

        var row = transactionDb.findById(created.getId()).orElseThrow();
        assertThat(row.userId()).isEqualTo(seededUser().getId());
        assertThat(row.amount()).isEqualByComparingTo("444.44");
        assertThat(row.description()).isEqualTo(request.getDescription());
    }
}
