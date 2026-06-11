package com.flowiq.api.analytics;

import com.flowiq.base.BaseRegressionApiTest;
import com.flowiq.models.response.AnalyticsOverviewResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Regression")
@Feature("Analytics")
public class AnalyticsRegressionApiTest extends BaseRegressionApiTest {

    @Test(groups = {"regression", "api", "analytics"})
    @Story("Analytics overview")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Analytics overview returns revenue, expenses and profit metrics")
    public void shouldReturnAnalyticsOverview() {
        AnalyticsOverviewResponse overview = analyticsService.getOverview();

        assertThat(overview.getRevenue()).isNotNull();
        assertThat(overview.getExpenses()).isNotNull();
        assertThat(overview.getProfit()).isNotNull();
    }

    @Test(groups = {"regression", "api", "analytics"})
    @Story("Analytics trends")
    @Severity(SeverityLevel.NORMAL)
    @Description("All analytics trend endpoints return data")
    public void shouldReturnAllAnalyticsEndpoints() {
        var soft = softAssert();

        soft.assertThat(analyticsService.getRevenueTrend()).isNotNull();
        soft.assertThat(analyticsService.getExpenseBreakdown()).isNotNull();
        soft.assertThat(analyticsService.getProfitTrend()).isNotNull();
        soft.assertThat(analyticsService.getFopInsights().getFopGroupNumber()).isNotNull();
        soft.assertThat(analyticsService.getIncomeVsExpenses()).isNotNull();

        soft.assertAll();
    }
}
