package com.flowiq.api.security;

import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseApiTest;
import com.flowiq.clients.ApiCallResult;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Security")
@Feature("Authorization")
public class SecuritySmokeApiTest extends BaseApiTest {

    @BeforeMethod(alwaysRun = true)
    public void clearSession() {
        TokenManager.clear();
    }

    @Test(groups = {"security", "smoke", "api"})
    @Story("401 enforcement")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Protected API endpoints reject unauthenticated requests")
    public void shouldRejectUnauthenticatedAccessAcrossModules() {
        assertUnauthorized("Dashboard stats", dashboardService.fetchStatsUnauthorized());
        assertUnauthorized("Transactions", transactionService.fetchListUnauthorized());
        assertUnauthorized("Imports", importService.fetchListUnauthorized());
        assertUnauthorized("Analytics", analyticsService.fetchOverviewUnauthorized());
        assertUnauthorized("Reports", reportService.fetchListUnauthorized());
        assertUnauthorized("Notifications", notificationService.fetchListUnauthorized());
        assertUnauthorized("Tasks", taskService.fetchListUnauthorized());
        assertUnauthorized("Forecasts summary", forecastService.fetchSummaryUnauthorized());
        assertUnauthorized("Business guide", businessGuideService.fetchArticlesUnauthorized());
        assertUnauthorized("AI health", aiAccountantService.fetchHealthUnauthorized());
        assertUnauthorized("Profile", profileService.fetchProfileUnauthorized());
        assertUnauthorized("Settings notifications", settingsService.fetchNotificationPreferencesUnauthorized());
    }

    private void assertUnauthorized(String label, ApiCallResult<?> result) {
        assertThat(result.getStatusCode())
                .as("Endpoint %s should require auth", label)
                .isIn(401, 403);
    }
}
