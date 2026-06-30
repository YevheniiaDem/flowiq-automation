package com.flowiq.api.regression.analytics;

import com.flowiq.api.regression.base.BaseRegressionApiTest;
import com.flowiq.api.regression.support.RegressionAssertions;
import com.flowiq.auth.TokenManager;
import com.flowiq.models.response.FopInsightsResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Regression")
@Feature("Analytics")
public class AnalyticsRegressionTest extends BaseRegressionApiTest {

    @Test(groups = {"api-regression", "regression", "api", "analytics"})
    @Story("Analytics overview")
    @Severity(SeverityLevel.BLOCKER)
    @Description("GET /analytics/overview returns revenue, expenses and profit")
    public void shouldReturnAnalyticsOverview() {
        var overview = analyticsService.getOverview();

        assertThat(overview.getRevenue()).isNotNull();
        assertThat(overview.getExpenses()).isNotNull();
        assertThat(overview.getProfit()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "analytics"})
    @Story("Revenue trend")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /analytics/revenue-trend returns monthly revenue data")
    public void shouldReturnRevenueTrend() {
        assertThat(analyticsService.getRevenueTrend()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "analytics"})
    @Story("Expense breakdown")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /analytics/expense-breakdown returns category amounts")
    public void shouldReturnExpenseBreakdown() {
        assertThat(analyticsService.getExpenseBreakdown()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "analytics"})
    @Story("Profit trend")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /analytics/profit-trend returns monthly profit data")
    public void shouldReturnProfitTrend() {
        assertThat(analyticsService.getProfitTrend()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "analytics"})
    @Story("FOP insights")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /analytics/fop-insights returns FOP tax insights")
    public void shouldReturnFopInsights() {
        FopInsightsResponse insights = analyticsService.getFopInsights();

        assertThat(insights.getCurrentFopGroup()).isNotBlank();
        assertThat(insights.getFopGroupNumber()).isGreaterThan(0);
        assertThat(insights.getAnnualIncome()).isNotNull();
        assertThat(insights.getIncomeLimit()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "analytics"})
    @Story("Income vs expenses")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /analytics/income-vs-expenses returns monthly comparison")
    public void shouldReturnIncomeVsExpenses() {
        assertThat(analyticsService.getIncomeVsExpenses()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "analytics"})
    @Story("Analytics overview")
    @Severity(SeverityLevel.NORMAL)
    @Description("Analytics overview supports period preset filter")
    public void shouldReturnOverviewWithPeriodPreset() {
        var overview = analyticsService.getOverview(Map.of("periodPreset", "THIS_MONTH"));

        assertThat(overview.getRevenue()).isNotNull();
        assertThat(overview.getExpenses()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "analytics"})
    @Story("FOP insights business rule")
    @Severity(SeverityLevel.CRITICAL)
    @Description("FOP income limit usage percent is within valid range")
    public void shouldEnforceFopIncomeLimitUsageBusinessRule() {
        FopInsightsResponse insights = analyticsService.getFopInsights();

        assertThat(insights.getIncomeLimitUsagePercent()).isBetween(0.0, 100.0);
        assertThat(insights.getIncomeLimitProgress()).isBetween(0.0, 100.0);
        assertThat(insights.getAnnualIncome()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(insights.getIncomeLimit()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test(groups = {"api-regression", "regression", "api", "analytics"})
    @Story("FOP insights business rule")
    @Severity(SeverityLevel.NORMAL)
    @Description("FOP insights include tax forecast and payment schedule metadata")
    public void shouldReturnFopTaxMetadata() {
        FopInsightsResponse insights = analyticsService.getFopInsights();

        assertThat(insights.getEstimatedTaxLoad()).isNotNull();
        assertThat(insights.getDaysUntilNextTaxPayment()).isGreaterThanOrEqualTo(0);
        assertThat(insights.getNextTaxPaymentLabel()).isNotBlank();
    }

    @Test(groups = {"api-regression", "regression", "api", "analytics"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated analytics overview request is rejected")
    public void shouldRejectUnauthorizedOverviewAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(analyticsService.fetchOverviewUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "analytics"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated FOP insights request is rejected")
    public void shouldRejectUnauthorizedFopInsightsAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(analyticsService.fetchFopInsightsUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "analytics"})
    @Story("Analytics overview")
    @Severity(SeverityLevel.NORMAL)
    @Description("Analytics overview profit equals revenue minus expenses")
    public void shouldKeepOverviewProfitConsistent() {
        var overview = analyticsService.getOverview();

        if (overview.getRevenue() != null && overview.getExpenses() != null && overview.getProfit() != null) {
            assertThat(overview.getProfit())
                    .isEqualByComparingTo(overview.getRevenue().subtract(overview.getExpenses()));
        }
    }

    @Test(groups = {"api-regression", "regression", "api", "analytics"})
    @Story("FOP insights")
    @Severity(SeverityLevel.NORMAL)
    @Description("FOP insights include top expense categories")
    public void shouldReturnTopExpenseCategoriesInFopInsights() {
        FopInsightsResponse insights = analyticsService.getFopInsights();

        assertThat(insights.getTopExpenseCategories()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "analytics"})
    @Story("Analytics overview")
    @Severity(SeverityLevel.NORMAL)
    @Description("Analytics overview fetch returns successful response")
    public void shouldFetchOverviewSuccessfully() {
        RegressionAssertions.assertOk(analyticsService.fetchOverview());
    }

    @Test(groups = {"api-regression", "regression", "api", "analytics"})
    @Story("Revenue trend")
    @Severity(SeverityLevel.NORMAL)
    @Description("Revenue trend data points contain month and amount")
    public void shouldReturnRevenueTrendWithDataPoints() {
        var trend = analyticsService.getRevenueTrend();

        if (!trend.isEmpty()) {
            assertThat(trend.get(0).getMonth()).isNotBlank();
            assertThat(trend.get(0).getAmount()).isNotNull();
        }
    }
}
