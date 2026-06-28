package com.flowiq.ui.regression;

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

@Epic("UI Regression")
@Feature("Analytics")
public class AnalyticsRegressionTest extends BaseUiSmokeTest {

    @Test(groups = {"ui-regression", "regression", "ui", "analytics"})
    @Story("Navigation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Analytics page is reachable from sidebar navigation")
    public void shouldNavigateToAnalyticsFromSidebar() {
        pages.dashboard().open();
        page.getByTestId("nav-link-analytics").click();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.analytics().pageRoot().isVisible()).isTrue();
    }

    @Test(groups = {"ui-regression", "regression", "ui", "analytics", "empty-states"})
    @Story("Empty state CTA")
    @Severity(SeverityLevel.NORMAL)
    @Description("Analytics empty state exposes import CTA when no data")
    public void shouldExposeEmptyStateImportCtaWhenNoData() {
        pages.analytics().open();

        if (pages.analytics().emptyState().isVisible()) {
            assertThat(page.getByTestId("analytics-empty-state").locator("button, a").first().isVisible()).isTrue();
        }
    }
}
