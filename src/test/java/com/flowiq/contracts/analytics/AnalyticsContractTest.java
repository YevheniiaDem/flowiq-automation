package com.flowiq.contracts.analytics;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.contracts.ContractAssertions;
import com.flowiq.contracts.ContractSchemas;
import com.flowiq.contracts.base.BaseContractTest;
import com.flowiq.models.response.AnalyticsOverviewResponse;
import com.flowiq.models.response.FopInsightsResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("Contract Testing")
@Feature("Analytics")
public class AnalyticsContractTest extends BaseContractTest {

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    @Test(groups = {"contract", "analytics"})
    @Story("GET /api/analytics/overview")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Analytics overview response matches contract schema")
    public void analyticsOverviewShouldMatchContract() {
        ApiCallResult<AnalyticsOverviewResponse> result = analyticsService.fetchOverview();

        ContractAssertions.assertAllRequired(result, 200, ContractSchemas.ANALYTICS_OVERVIEW,
                "revenue", "expenses", "profit");
    }

    @Test(groups = {"contract", "analytics"})
    @Story("GET /api/analytics/fop-insights")
    @Severity(SeverityLevel.CRITICAL)
    @Description("FOP insights response matches contract schema")
    public void fopInsightsShouldMatchContract() {
        ApiCallResult<FopInsightsResponse> result = analyticsService.fetchFopInsights();

        ContractAssertions.assertAllRequired(result, 200, ContractSchemas.ANALYTICS_FOP_INSIGHTS,
                "currentFopGroup", "fopGroupNumber", "annualIncome", "incomeLimit");
    }
}
