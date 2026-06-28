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
@Feature("Analytics")
public class AnalyticsSmokeTest extends BaseUiSmokeTest {

    @Test(groups = {"ui-smoke", "smoke", "ui", "analytics"})
    @Story("Page load")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Analytics page loads for authenticated user")
    public void shouldOpenAnalyticsPage() {
        pages.analytics().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.analytics().pageRoot().isVisible()).isTrue();
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "analytics", "empty-states"})
    @Story("Empty state")
    @Severity(SeverityLevel.NORMAL)
    @Description("Analytics shows content or guided empty state")
    public void shouldShowAnalyticsContentOrEmptyState() {
        pages.analytics().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.analytics().hasContentOrEmptyState()).isTrue();
    }
}
