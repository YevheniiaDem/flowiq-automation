package com.flowiq.api.regression.forecasts;

import com.flowiq.api.regression.base.BaseRegressionApiTest;
import com.flowiq.api.regression.support.RegressionAssertions;
import com.flowiq.auth.TokenManager;
import com.flowiq.models.forecasts.ForecastSummaryResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Regression")
@Feature("Forecasts")
public class ForecastsRegressionTest extends BaseRegressionApiTest {

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Revenue forecast")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /forecasts/revenue returns projected revenue metrics")
    public void shouldReturnRevenueForecast() {
        var revenue = forecastService.getRevenue();

        assertThat(revenue.getHistorical()).isNotNull();
        assertThat(revenue.getProjected()).isNotNull();
        assertThat(revenue.getHorizons()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Expense forecast")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /forecasts/expenses returns projected expense metrics")
    public void shouldReturnExpenseForecast() {
        var expenses = forecastService.getExpenses();

        assertThat(expenses.getHistorical()).isNotNull();
        assertThat(expenses.getProjected()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Profit forecast")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /forecasts/profit returns projected profit metrics")
    public void shouldReturnProfitForecast() {
        var profit = forecastService.getProfit();

        assertThat(profit.getHistorical()).isNotNull();
        assertThat(profit.getProjected()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Tax forecast")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /forecasts/taxes returns tax burden forecast")
    public void shouldReturnTaxForecast() {
        var taxes = forecastService.getTaxes();

        assertThat(taxes.getCurrentTaxBurden()).isNotNull();
        assertThat(taxes.getAnnualTaxForecast()).isNotNull();
        assertThat(taxes.getCards()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("FOP limit forecast")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /forecasts/fop-limit returns FOP income limit forecast")
    public void shouldReturnFopLimitForecast() {
        var fopLimit = forecastService.getFopLimit();

        assertThat(fopLimit.getIncomeLimit()).isNotNull();
        assertThat(fopLimit.getCurrentAnnualIncome()).isNotNull();
        assertThat(fopLimit.getFopGroup()).isGreaterThan(0);
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Forecast summary")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /forecasts/summary returns full forecast summary")
    public void shouldReturnForecastSummary() {
        ForecastSummaryResponse summary = forecastService.getSummary();

        assertThat(summary.getExpectedRevenue()).isNotNull();
        assertThat(summary.getExpectedExpenses()).isNotNull();
        assertThat(summary.getExpectedProfit()).isNotNull();
        assertThat(summary.getWarnings()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("FOP limit business rule")
    @Severity(SeverityLevel.CRITICAL)
    @Description("FOP limit usage percent stays within valid range")
    public void shouldEnforceFopLimitUsageBusinessRule() {
        var fopLimit = forecastService.getFopLimit();

        assertThat(fopLimit.getCurrentUsagePercent()).isBetween(0.0, 100.0);
        assertThat(fopLimit.getIncomeLimit()).isGreaterThan(BigDecimal.ZERO);
        assertThat(fopLimit.getCurrentAnnualIncome()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("FOP limit business rule")
    @Severity(SeverityLevel.NORMAL)
    @Description("FOP limit forecast includes group label and horizon projections")
    public void shouldReturnFopLimitWithHorizons() {
        var fopLimit = forecastService.getFopLimit();

        assertThat(fopLimit.getFopGroupLabel()).isNotBlank();
        assertThat(fopLimit.getHorizons()).isNotNull();
        assertThat(fopLimit.getMonthsUntilLimitExceeded()).isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Profit calculation consistency")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Forecast summary profit aligns with revenue minus expenses")
    public void shouldKeepProfitCalculationConsistent() {
        ForecastSummaryResponse summary = forecastService.getSummary();

        if (summary.getExpectedRevenue() != null
                && summary.getExpectedExpenses() != null
                && summary.getExpectedProfit() != null) {
            assertThat(summary.getExpectedProfit())
                    .isEqualByComparingTo(
                            summary.getExpectedRevenue().subtract(summary.getExpectedExpenses()));
        }
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Profit calculation consistency")
    @Severity(SeverityLevel.NORMAL)
    @Description("Forecast summary FOP limit usage percent is within valid range")
    public void shouldKeepSummaryFopUsageConsistent() {
        ForecastSummaryResponse summary = forecastService.getSummary();

        assertThat(summary.getFopLimitUsagePercent()).isBetween(0.0, 100.0);
        assertThat(summary.getMonthsUntilFopLimit()).isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated forecast summary request is rejected")
    public void shouldRejectUnauthorizedSummaryAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(forecastService.fetchSummaryUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated revenue forecast request is rejected")
    public void shouldRejectUnauthorizedRevenueAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(forecastService.fetchRevenueUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated expense forecast request is rejected")
    public void shouldRejectUnauthorizedExpensesAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(forecastService.fetchExpensesUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated profit forecast request is rejected")
    public void shouldRejectUnauthorizedProfitAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(forecastService.fetchProfitUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated tax forecast request is rejected")
    public void shouldRejectUnauthorizedTaxesAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(forecastService.fetchTaxesUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated FOP limit forecast request is rejected")
    public void shouldRejectUnauthorizedFopLimitAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(forecastService.fetchFopLimitUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Data consistency")
    @Severity(SeverityLevel.NORMAL)
    @Description("All forecast metric endpoints return successful responses")
    public void shouldFetchAllForecastMetricsSuccessfully() {
        RegressionAssertions.assertOk(forecastService.fetchRevenue());
        RegressionAssertions.assertOk(forecastService.fetchExpenses());
        RegressionAssertions.assertOk(forecastService.fetchProfit());
        RegressionAssertions.assertOk(forecastService.fetchTaxes());
        RegressionAssertions.assertOk(forecastService.fetchFopLimit());
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Forecast summary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Forecast summary includes insights and horizon projections")
    public void shouldReturnSummaryWithInsightsAndHorizons() {
        ForecastSummaryResponse summary = forecastService.getSummary();

        assertThat(summary.getInsights()).isNotNull();
        assertThat(summary.getRevenueHorizons()).isNotNull();
        assertThat(summary.getProfitHorizons()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "forecasts"})
    @Story("Revenue forecast")
    @Severity(SeverityLevel.NORMAL)
    @Description("Revenue forecast includes trend percent indicator")
    public void shouldReturnRevenueWithTrendPercent() {
        var revenue = forecastService.getRevenue();

        assertThat(Double.isFinite(revenue.getTrendPercent())).isTrue();
    }
}
