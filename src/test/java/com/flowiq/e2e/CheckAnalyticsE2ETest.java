package com.flowiq.e2e;

import com.flowiq.base.BaseE2ETest;
import com.flowiq.models.response.AnalyticsOverviewResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("E2E")
@Feature("Check Analytics")
public class CheckAnalyticsE2ETest extends BaseE2ETest {

    @Test(groups = {"e2e", "analytics"})
    @Story("Analytics after UI session")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Authenticated UI session can access analytics API metrics")
    public void shouldAccessAnalyticsInAuthenticatedSession() {
        pages.dashboard().open();

        AnalyticsOverviewResponse overview = analyticsService.getOverview();

        assertThat(overview.getRevenue()).isNotNull();
        assertThat(overview.getExpenses()).isNotNull();
        assertThat(overview.getProfit()).isNotNull();
        assertThat(pages.dashboard().isLoaded()).isTrue();
    }
}
