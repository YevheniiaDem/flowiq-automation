package com.flowiq.ui.transactions;

import com.flowiq.base.AuthenticatedUiTest;
import com.flowiq.base.UiAssertions;
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
public class TransactionsUiSmokeTest extends AuthenticatedUiTest {

    @Test(groups = {"smoke", "ui", "transactions"})
    @Story("Page load")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Transactions page displays toolbar, filters and table")
    public void shouldDisplayTransactionsPage() {
        pages.transactions().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.transactions().isLoaded()).isTrue();
        UiAssertions.assertElementVisible(pages.transactions().addButton());
        UiAssertions.assertElementVisible(pages.transactions().table());
    }
}
