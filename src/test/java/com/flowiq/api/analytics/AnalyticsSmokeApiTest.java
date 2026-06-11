package com.flowiq.api.analytics;

import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseSmokeApiTest;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.SmokeSchemas;
import com.flowiq.models.response.AnalyticsOverviewResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Smoke")
@Feature("Analytics")
public class AnalyticsSmokeApiTest extends BaseSmokeApiTest {

    @Test(groups = {"smoke", "api", "analytics"})
    @Story("Happy path")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Authenticated user can retrieve analytics overview")
    public void shouldGetAnalyticsOverview() {
        ApiCallResult<AnalyticsOverviewResponse> result = analyticsService.fetchOverview();

        assertHappyPath(result);
        assertThat(result.getBody().getRevenue()).isNotNull();
    }

    @Test(groups = {"smoke", "api", "analytics"})
    @Story("Authorization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Analytics endpoint requires JWT")
    public void shouldRejectUnauthenticatedAccess() {
        TokenManager.clear();
        assertUnauthorized(analyticsService.fetchOverviewUnauthorized());
    }

    @Test(groups = {"smoke", "api", "analytics"})
    @Story("Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Analytics overview with invalid query parameter is handled")
    public void shouldHandleInvalidQueryParameter() {
        ApiCallResult<AnalyticsOverviewResponse> result =
                analyticsService.fetchOverview(java.util.Map.of("period", "INVALID_PERIOD"));

        assertHappyPath(result);
    }

    @Test(groups = {"smoke", "api", "analytics"})
    @Story("Schema")
    @Severity(SeverityLevel.NORMAL)
    @Description("Analytics overview response matches JSON schema")
    public void shouldMatchOverviewSchema() {
        ApiCallResult<AnalyticsOverviewResponse> result = analyticsService.fetchOverview();

        assertHappyPath(result);
        assertMatchesSchema(result, SmokeSchemas.ANALYTICS_OVERVIEW);
    }
}
