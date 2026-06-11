package com.flowiq.integration.transactions;

import com.flowiq.base.BaseApiDbIT;
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

@Epic("Database Integration")
@Feature("Transactions")
public class TransactionsApiDbIT extends BaseApiDbIT {

    @Test(groups = {"integration", "api-db", "transactions"})
    @Story("API to database sync")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Transaction created via API is persisted in Testcontainer database")
    public void shouldPersistApiCreatedTransactionInDatabase() {
        var request = TransactionRequestBuilder.expense()
                .amount(new BigDecimal("333.33"))
                .uniqueDescription()
                .today()
                .build();

        TransactionResponse created = transactionService.create(request);

        var row = transactionDb.findById(created.getId()).orElseThrow();
        assertThat(row.userId()).isEqualTo(seededUser().getId());
        assertThat(row.amount()).isEqualByComparingTo("333.33");
        assertThat(row.description()).isEqualTo(request.getDescription());
    }
}
