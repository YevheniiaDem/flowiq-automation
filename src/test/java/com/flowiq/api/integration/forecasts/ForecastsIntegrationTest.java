package com.flowiq.api.integration.forecasts;

import com.flowiq.api.integration.base.BaseApiIntegrationTest;
import com.flowiq.api.integration.support.IntegrationAssertions;
import com.flowiq.auth.TokenManager;
import com.flowiq.models.forecasts.ForecastSummaryResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Integration")
@Feature("Forecasts")
public class ForecastsIntegrationTest extends BaseApiIntegrationTest {

    @Test(groups = {"api-integration", "api", "forecasts"})
    @Story("Revenue forecast")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /forecasts/revenue returns projected revenue metrics")
    public void shouldReturnRevenueForecast() {
        var revenue = forecastService.getRevenue();

        assertThat(revenue.getHistorical()).isNotNull();
        assertThat(revenue.getProjected()).isNotNull();
        assertThat(revenue.getHorizons()).isNotNull();
    }

    @Test(groups = {"api-integration", "api", "forecasts"})
    @Story("Expense forecast")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /forecasts/expenses returns projected expense metrics")
    public void shouldReturnExpenseForecast() {
        var expenses = forecastService.getExpenses();

        assertThat(expenses.getHistorical()).isNotNull();
        assertThat(expenses.getProjected()).isNotNull();
    }

    @Test(groups = {"api-integration", "api", "forecasts"})
    @Story("Profit forecast")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /forecasts/profit returns projected profit metrics")
    public void shouldReturnProfitForecast() {
        var profit = forecastService.getProfit();

        assertThat(profit.getHistorical()).isNotNull();
        assertThat(profit.getProjected()).isNotNull();
    }

    @Test(groups = {"api-integration", "api", "forecasts"})
    @Story("FOP limit forecast")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /forecasts/fop-limit returns FOP income limit forecast")
    public void shouldReturnFopLimitForecast() {
        var fopLimit = forecastService.getFopLimit();

        assertThat(fopLimit.getIncomeLimit()).isNotNull();
        assertThat(fopLimit.getCurrentAnnualIncome()).isNotNull();
        assertThat(fopLimit.getFopGroup()).isGreaterThan(0);
    }

    @Test(groups = {"api-integration", "api", "forecasts"})
    @Story("Tax forecast")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /forecasts/taxes returns tax burden forecast")
    public void shouldReturnTaxForecast() {
        var taxes = forecastService.getTaxes();

        assertThat(taxes.getCurrentTaxBurden()).isNotNull();
        assertThat(taxes.getAnnualTaxForecast()).isNotNull();
        assertThat(taxes.getCards()).isNotNull();
    }

    @Test(groups = {"api-integration", "api", "forecasts"})
    @Story("Data consistency")
    @Severity(SeverityLevel.NORMAL)
    @Description("Forecast summary aligns with individual metric endpoints")
    public void shouldKeepSummaryConsistentWithMetrics() {
        ForecastSummaryResponse summary = forecastService.getSummary();

        assertThat(summary.getExpectedRevenue()).isNotNull();
        assertThat(summary.getExpectedExpenses()).isNotNull();
        assertThat(summary.getExpectedProfit()).isNotNull();
        assertThat(summary.getWarnings()).isNotNull();

        assertThat(forecastService.fetchRevenue().isSuccessful()).isTrue();
        assertThat(forecastService.fetchExpenses().isSuccessful()).isTrue();
        assertThat(forecastService.fetchProfit().isSuccessful()).isTrue();
        assertThat(forecastService.fetchTaxes().isSuccessful()).isTrue();
        assertThat(forecastService.fetchFopLimit().isSuccessful()).isTrue();
    }

    @Test(groups = {"api-integration", "api", "forecasts"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated forecast requests are rejected")
    public void shouldRejectUnauthorizedAccess() {
        TokenManager.clear();
        IntegrationAssertions.assertUnauthorized(forecastService.fetchSummaryUnauthorized());
        loginAsDefaultUser();
    }
}
