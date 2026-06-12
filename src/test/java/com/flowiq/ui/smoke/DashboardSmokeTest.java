package com.flowiq.ui.smoke;

import com.flowiq.base.UiAssertions;
import com.flowiq.constants.TestIds;
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
@Feature("Dashboard")
public class DashboardSmokeTest extends BaseUiSmokeTest {

    @Test(groups = {"ui-smoke", "smoke", "ui", "dashboard"})
    @Story("KPI cards")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Dashboard KPI stat cards load for authenticated user")
    public void shouldDisplayKpiCards() {
        pages.dashboard().open();

        UiAssertions.waitForPageLoad(page);
        pages.dashboard().waitForStatsLoaded();
        assertThat(pages.dashboard().getStatCardCount()).isGreaterThan(0);
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "dashboard"})
    @Story("Charts")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Dashboard revenue and expense charts are rendered")
    public void shouldDisplayDashboardCharts() {
        pages.dashboard().open();

        UiAssertions.waitForPageLoad(page);
        pages.dashboard().waitForChartsLoaded();
        assertThat(pages.dashboard().getChartCount()).isGreaterThanOrEqualTo(1);
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "dashboard"})
    @Story("Sidebar")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Sidebar navigation is visible on dashboard")
    public void shouldDisplaySidebar() {
        pages.dashboard().open();

        UiAssertions.assertElementVisible(pages.dashboard().sidebar().sidebar());
        UiAssertions.assertElementVisible(page.getByTestId(TestIds.navLink("dashboard")));
        UiAssertions.assertElementVisible(page.getByTestId(TestIds.navLink("transactions")));
    }
}
