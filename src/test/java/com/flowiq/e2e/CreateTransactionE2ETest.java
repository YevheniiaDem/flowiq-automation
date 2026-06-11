package com.flowiq.e2e;

import com.flowiq.base.BaseE2ETest;
import com.flowiq.base.UiAssertions;
import com.flowiq.utils.RandomDataGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("E2E")
@Feature("Create Transaction")
public class CreateTransactionE2ETest extends BaseE2ETest {

    @Test(groups = {"e2e", "transactions"})
    @Story("Create via UI")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User creates a transaction in UI and sees it in the table")
    public void shouldCreateTransactionViaUi() {
        String description = "E2E tx " + RandomDataGenerator.alphanumeric(8);

        var transactionsPage = pages.transactions();
        transactionsPage.open();
        transactionsPage.createTransaction("99.99", description);
        transactionsPage.search(description);

        UiAssertions.waitUntilVisible(pages.transactions().table().getByText(description), 15);
        assertThat(pages.transactions().containsTransactionText(description)).isTrue();
    }
}
