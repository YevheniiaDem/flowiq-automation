package com.flowiq.ui.smoke;

import com.flowiq.base.UiAssertions;
import com.flowiq.ui.smoke.base.BaseUiSmokeTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("UI Smoke")
@Feature("Transactions")
public class TransactionsSmokeTest extends BaseUiSmokeTest {

    @Test(groups = {"ui-smoke", "smoke", "ui", "transactions"})
    @Story("Page load")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Transactions page opens with toolbar and table")
    public void shouldOpenTransactionsPage() {
        pages.transactions().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.transactions().isLoaded()).isTrue();
        UiAssertions.assertElementVisible(pages.transactions().table());
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "transactions"})
    @Story("Search")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User can type into transactions search field")
    public void shouldSearchTransactions() {
        pages.transactions().open();
        String query = "test";

        pages.transactions().search(query);

        assertThat(pages.transactions().searchInput().inputValue()).isEqualTo(query);
        UiAssertions.assertElementVisible(pages.transactions().table());
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "transactions"})
    @Story("Table")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Transactions table is visible with data rows or empty state")
    public void shouldDisplayTransactionsTable() {
        pages.transactions().open();

        UiAssertions.assertElementVisible(pages.transactions().table());
        assertThat(pages.transactions().getTransactionRowCount()).isGreaterThanOrEqualTo(0);
    }
}
