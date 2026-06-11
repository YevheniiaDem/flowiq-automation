package com.flowiq.integration.transactions;

import com.flowiq.base.BaseDbTest;
import com.flowiq.db.gateway.TransactionDbGateway;
import com.flowiq.db.seeder.TestDataSeeder;
import com.flowiq.utils.DateUtils;
import com.flowiq.utils.RandomDataGenerator;
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
public class TransactionsDbIT extends BaseDbTest {

    @Test(groups = {"integration", "db", "transactions"})
    @Story("Transaction persistence")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Isolated user transaction is persisted in PostgreSQL Testcontainer")
    public void shouldPersistTransactionForIsolatedUser() {
        String description = "db-it-" + RandomDataGenerator.alphanumeric(8);
        long transactionId = transactionDb.insert(
                seededUser().getId(),
                "EXPENSE",
                new BigDecimal("250.75"),
                "Office",
                description,
                DateUtils.parseDate(DateUtils.today())
        );

        TransactionDbGateway.TransactionRow row = transactionDb.findById(transactionId).orElseThrow();

        assertThat(row.userId()).isEqualTo(seededUser().getId());
        assertThat(row.type()).isEqualTo("EXPENSE");
        assertThat(row.amount()).isEqualByComparingTo("250.75");
        assertThat(row.description()).isEqualTo(description);
        assertThat(transactionDb.countByUserId(seededUser().getId())).isEqualTo(1);
    }

    @Test(groups = {"integration", "db", "transactions"})
    @Story("User isolation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Transactions are isolated per user in cleaned database")
    public void shouldIsolateTransactionsByUser() {
        var otherUser = TestDataSeeder.seedUser(dataSource);

        transactionDb.insert(
                seededUser().getId(), "INCOME", new BigDecimal("100.00"),
                "Services", "user-a", DateUtils.parseDate(DateUtils.today()));
        transactionDb.insert(
                otherUser.getId(), "EXPENSE", new BigDecimal("50.00"),
                "Office", "user-b", DateUtils.parseDate(DateUtils.today()));

        assertThat(transactionDb.countByUserId(seededUser().getId())).isEqualTo(1);
        assertThat(transactionDb.countByUserId(otherUser.getId())).isEqualTo(1);
    }

    @Test(groups = {"integration", "db", "transactions"})
    @Story("Transaction deletion")
    @Severity(SeverityLevel.NORMAL)
    @Description("Transaction can be deleted from isolated database state")
    public void shouldDeleteTransaction() {
        long transactionId = transactionDb.insert(
                seededUser().getId(),
                "EXPENSE",
                new BigDecimal("10.00"),
                "Other",
                "to-delete",
                DateUtils.parseDate(DateUtils.today())
        );

        transactionDb.deleteById(transactionId);

        assertThat(transactionDb.findById(transactionId)).isEmpty();
        assertThat(transactionDb.countByUserId(seededUser().getId())).isZero();
    }
}
