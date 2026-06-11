package com.flowiq.ui.dashboard;

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
@Feature("Dashboard")
public class DashboardUiSmokeTest extends AuthenticatedUiTest {

    @Test(groups = {"smoke", "ui", "dashboard"})
    @Story("Page load")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Dashboard page loads with stats grid for authenticated user")
    public void shouldDisplayDashboardPage() {
        pages.dashboard().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.dashboard().isLoaded()).isTrue();
        pages.dashboard().waitForStatsLoaded();
        assertThat(pages.dashboard().getStatCardCount()).isGreaterThan(0);
    }
}
