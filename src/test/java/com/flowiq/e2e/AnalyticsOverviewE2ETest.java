package com.flowiq.e2e;

import com.flowiq.base.BaseE2ETest;
import com.flowiq.base.UiAssertions;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("E2E")
@Feature("Analytics")
public class AnalyticsOverviewE2ETest extends BaseE2ETest {

    @Test(groups = {"e2e", "analytics"})
    @Story("Overview")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Authenticated user opens analytics and sees overview or empty state")
    public void shouldOpenAnalyticsOverview() {
        pages.analytics().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.analytics().hasContentOrEmptyState()).isTrue();
    }
}
