package com.flowiq.api.dashboard;

import com.flowiq.base.BaseRegressionApiTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Regression")
@Feature("Dashboard")
public class DashboardRegressionApiTest extends BaseRegressionApiTest {

    @Test(groups = {"regression", "api", "dashboard"})
    @Story("Dashboard widgets")
    @Severity(SeverityLevel.CRITICAL)
    @Description("All dashboard endpoints return data for authenticated user")
    public void shouldLoadAllDashboardEndpoints() {
        var soft = softAssert();

        soft.assertThat(dashboardService.getStats()).isNotNull();
        soft.assertThat(dashboardService.getInsights()).isNotNull();
        assertThat(dashboardService.getHealth().getScore()).isGreaterThanOrEqualTo(0);
        soft.assertThat(dashboardService.getSummary().getText()).isNotBlank();
        soft.assertThat(dashboardService.getRevenueTrend()).isNotNull();
        soft.assertThat(dashboardService.getExpenseBreakdown()).isNotNull();
        soft.assertThat(dashboardService.getForecastSnapshot()).isNotNull();
        soft.assertThat(dashboardService.getTasksSnapshot()).isNotNull();
        soft.assertThat(dashboardService.getBusinessGuideSnapshot()).isNotNull();

        soft.assertAll();
    }

    @Test(groups = {"regression", "api", "dashboard"})
    @Story("Dashboard stats")
    @Severity(SeverityLevel.NORMAL)
    @Description("Dashboard stats contain stat cards")
    public void shouldReturnDashboardStatCards() {
        assertThat(dashboardService.getStats()).isNotEmpty();
    }
}
