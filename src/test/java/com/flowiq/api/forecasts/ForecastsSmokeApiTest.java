package com.flowiq.api.forecasts;

import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseSmokeApiTest;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.SmokeSchemas;
import com.flowiq.models.forecasts.ForecastSummaryResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Smoke")
@Feature("Forecasts")
public class ForecastsSmokeApiTest extends BaseSmokeApiTest {

    @Test(groups = {"smoke", "api", "forecasts"})
    @Story("Happy path")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Authenticated user can retrieve forecast summary")
    public void shouldGetForecastSummary() {
        ApiCallResult<ForecastSummaryResponse> result = forecastService.fetchSummary();

        assertHappyPath(result);
        assertThat(result.getBody().getExpectedRevenue()).isNotNull();
    }

    @Test(groups = {"smoke", "api", "forecasts"})
    @Story("Authorization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Forecasts endpoint requires JWT")
    public void shouldRejectUnauthenticatedAccess() {
        TokenManager.clear();
        assertUnauthorized(forecastService.fetchSummaryUnauthorized());
    }

    @Test(groups = {"smoke", "api", "forecasts"})
    @Story("Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Unknown forecast sub-resource does not return success")
    public void shouldReturnErrorForUnknownEndpoint() {
        assertRejectedWithClientOrServerError(forecastService.fetchUnknownResource());
    }

    @Test(groups = {"smoke", "api", "forecasts"})
    @Story("Schema")
    @Severity(SeverityLevel.NORMAL)
    @Description("Forecast summary response matches JSON schema")
    public void shouldMatchForecastSummarySchema() {
        ApiCallResult<ForecastSummaryResponse> result = forecastService.fetchSummary();

        assertHappyPath(result);
        assertMatchesSchema(result, SmokeSchemas.FORECAST_SUMMARY);
    }
}
