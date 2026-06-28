package com.flowiq.api.dashboard;

import com.flowiq.base.BaseSmokeApiTest;
import com.flowiq.clients.ApiCallResult;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Smoke")
@Feature("Dashboard")
public class DashboardSmokeApiTest extends BaseSmokeApiTest {

    @Test(groups = {"smoke", "api", "dashboard"})
    @Story("Happy path")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Dashboard stats load for authenticated user")
    public void shouldReturnDashboardStats() {
        assertThat(dashboardService.getStats()).isNotEmpty();
    }

    @Test(groups = {"smoke", "api", "dashboard"})
    @Story("Authorization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Dashboard stats require authentication")
    public void shouldRejectUnauthenticatedStats() {
        assertUnauthorized(dashboardService.fetchStatsUnauthorized());
    }

    @Test(groups = {"smoke", "api", "dashboard"})
    @Story("Business health")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Dashboard health score is available")
    public void shouldReturnBusinessHealth() {
        assertThat(dashboardService.getHealth().getScore()).isBetween(0, 100);
    }

    @Test(groups = {"smoke", "api", "dashboard"})
    @Story("Authorization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Dashboard health requires authentication")
    public void shouldRejectUnauthenticatedHealth() {
        ApiCallResult<?> result = dashboardService.fetchHealthUnauthorized();
        assertUnauthorized(result);
    }
}
