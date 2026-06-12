package com.flowiq.api.regression.dashboard;

import com.flowiq.api.regression.base.BaseRegressionApiTest;
import com.flowiq.api.regression.support.RegressionAssertions;
import com.flowiq.auth.TokenManager;
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
public class DashboardRegressionTest extends BaseRegressionApiTest {

    @Test(groups = {"api-regression", "regression", "api", "dashboard"})
    @Story("Dashboard stats")
    @Severity(SeverityLevel.BLOCKER)
    @Description("GET /dashboard/stats returns stat cards for authenticated user")
    public void shouldReturnDashboardStats() {
        assertThat(dashboardService.getStats()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "dashboard"})
    @Story("Dashboard insights")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /dashboard/insights returns AI insights")
    public void shouldReturnDashboardInsights() {
        assertThat(dashboardService.getInsights()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "dashboard"})
    @Story("Business health")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /dashboard/health returns business health score")
    public void shouldReturnBusinessHealth() {
        var health = dashboardService.getHealth();

        assertThat(health.getScore()).isBetween(0, 100);
        assertThat(health.getStatus()).isNotBlank();
    }

    @Test(groups = {"api-regression", "regression", "api", "dashboard"})
    @Story("AI summary")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /dashboard/summary returns AI dashboard summary")
    public void shouldReturnDashboardSummary() {
        var summary = dashboardService.getSummary();

        assertThat(summary.getText()).isNotBlank();
    }

    @Test(groups = {"api-regression", "regression", "api", "dashboard"})
    @Story("Revenue trend")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /dashboard/charts/revenue-trend returns monthly revenue data")
    public void shouldReturnRevenueTrend() {
        assertThat(dashboardService.getRevenueTrend()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "dashboard"})
    @Story("Expense breakdown")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /dashboard/charts/expense-breakdown returns category breakdown")
    public void shouldReturnExpenseBreakdown() {
        assertThat(dashboardService.getExpenseBreakdown()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "dashboard"})
    @Story("Forecast snapshot")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /dashboard/forecast-snapshot returns forecast widget data")
    public void shouldReturnForecastSnapshot() {
        var snapshot = dashboardService.getForecastSnapshot();

        assertThat(snapshot).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "dashboard"})
    @Story("Tasks snapshot")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /dashboard/tasks-snapshot returns tasks widget data")
    public void shouldReturnTasksSnapshot() {
        var snapshot = dashboardService.getTasksSnapshot();

        assertThat(snapshot).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "dashboard"})
    @Story("Business guide snapshot")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /dashboard/business-guide-snapshot returns knowledge widget data")
    public void shouldReturnBusinessGuideSnapshot() {
        var snapshot = dashboardService.getBusinessGuideSnapshot();

        assertThat(snapshot).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "dashboard"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated dashboard stats request is rejected")
    public void shouldRejectUnauthorizedStatsAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(dashboardService.fetchStatsUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "dashboard"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated dashboard health request is rejected")
    public void shouldRejectUnauthorizedHealthAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(dashboardService.fetchHealthUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "dashboard"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated dashboard summary request is rejected")
    public void shouldRejectUnauthorizedSummaryAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(dashboardService.fetchSummaryUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "dashboard"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated revenue trend request is rejected")
    public void shouldRejectUnauthorizedRevenueTrendAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(dashboardService.fetchRevenueTrendUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "dashboard"})
    @Story("Dashboard stats")
    @Severity(SeverityLevel.NORMAL)
    @Description("Dashboard stat cards contain title and value")
    public void shouldReturnStatCardsWithValues() {
        var stats = dashboardService.getStats();

        if (!stats.isEmpty()) {
            assertThat(stats.get(0).getLabelKey()).isNotBlank();
            assertThat(stats.get(0).getAmount()).isNotNull();
        }
    }

    @Test(groups = {"api-regression", "regression", "api", "dashboard"})
    @Story("Business health")
    @Severity(SeverityLevel.NORMAL)
    @Description("Business health response includes max score reference")
    public void shouldReturnHealthWithMaxScore() {
        var health = dashboardService.getHealth();

        assertThat(health.getMaxScore()).isGreaterThan(0);
        assertThat(health.getScore()).isLessThanOrEqualTo(health.getMaxScore());
    }
}
