package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.ApiEndpoints;
import com.flowiq.models.response.AnalyticsOverviewResponse;
import com.flowiq.models.response.CategoryAmountResponse;
import com.flowiq.models.response.FopInsightsResponse;
import com.flowiq.models.response.MonthlyAmountResponse;
import com.flowiq.models.response.MonthlyComparisonResponse;
import io.qameta.allure.Step;

import java.util.List;
import java.util.Map;

public class AnalyticsService extends BaseApiService {

    @Step("Get analytics overview")
    public AnalyticsOverviewResponse getOverview() {
        return getOk(ApiEndpoints.ANALYTICS_OVERVIEW, AnalyticsOverviewResponse.class);
    }

    @Step("Get revenue trend")
    public List<MonthlyAmountResponse> getRevenueTrend() {
        return get(ApiEndpoints.ANALYTICS_REVENUE_TREND).getRaw().jsonPath().getList("", MonthlyAmountResponse.class);
    }

    @Step("Get expense breakdown")
    public List<CategoryAmountResponse> getExpenseBreakdown() {
        return get(ApiEndpoints.ANALYTICS_EXPENSE_BREAKDOWN).getRaw().jsonPath().getList("", CategoryAmountResponse.class);
    }

    @Step("Get profit trend")
    public List<MonthlyAmountResponse> getProfitTrend() {
        return get(ApiEndpoints.ANALYTICS_PROFIT_TREND).getRaw().jsonPath().getList("", MonthlyAmountResponse.class);
    }

    @Step("Get FOP tax insights")
    public FopInsightsResponse getFopInsights() {
        return getOk(ApiEndpoints.ANALYTICS_FOP_INSIGHTS, FopInsightsResponse.class);
    }

    @Step("Get income vs expenses comparison")
    public List<MonthlyComparisonResponse> getIncomeVsExpenses() {
        return get(ApiEndpoints.ANALYTICS_INCOME_VS_EXPENSES).getRaw().jsonPath().getList("", MonthlyComparisonResponse.class);
    }

    @Step("Get analytics overview with filters")
    public AnalyticsOverviewResponse getOverview(Map<String, ?> queryParams) {
        return getOk(ApiEndpoints.ANALYTICS_OVERVIEW, queryParams, AnalyticsOverviewResponse.class);
    }

    @Step("Fetch analytics overview (unchecked)")
    public ApiCallResult<AnalyticsOverviewResponse> fetchOverview() {
        return fetch(ApiEndpoints.ANALYTICS_OVERVIEW, AnalyticsOverviewResponse.class);
    }

    @Step("Fetch analytics overview without authentication")
    public ApiCallResult<AnalyticsOverviewResponse> fetchOverviewUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.ANALYTICS_OVERVIEW, AnalyticsOverviewResponse.class);
    }

    @Step("Fetch analytics overview with params (unchecked)")
    public ApiCallResult<AnalyticsOverviewResponse> fetchOverview(Map<String, ?> queryParams) {
        return fetch(ApiEndpoints.ANALYTICS_OVERVIEW, queryParams, AnalyticsOverviewResponse.class);
    }

    @Step("Fetch FOP insights (unchecked)")
    public ApiCallResult<FopInsightsResponse> fetchFopInsights() {
        return fetch(ApiEndpoints.ANALYTICS_FOP_INSIGHTS, FopInsightsResponse.class);
    }
}
