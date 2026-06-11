package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.ApiEndpoints;
import com.flowiq.models.forecasts.ForecastSnapshotResponse;
import com.flowiq.models.knowledge.KnowledgeDashboardSnapshotDto;
import com.flowiq.models.response.AIInsightResponse;
import com.flowiq.models.response.AISummaryResponse;
import com.flowiq.models.response.BusinessHealthResponse;
import com.flowiq.models.response.CategoryAmountResponse;
import com.flowiq.models.response.MonthlyAmountResponse;
import com.flowiq.models.response.StatCardResponse;
import com.flowiq.models.tasks.TaskSnapshotResponse;
import io.qameta.allure.Step;

import java.util.List;

public class DashboardService extends BaseApiService {

    @Step("Get dashboard stats")
    public List<StatCardResponse> getStats() {
        return get(ApiEndpoints.DASHBOARD_STATS).getRaw().jsonPath().getList("", StatCardResponse.class);
    }

    @Step("Get dashboard AI insights")
    public List<AIInsightResponse> getInsights() {
        return get(ApiEndpoints.DASHBOARD_INSIGHTS).getRaw().jsonPath().getList("", AIInsightResponse.class);
    }

    @Step("Get business health score")
    public BusinessHealthResponse getHealth() {
        return getOk(ApiEndpoints.DASHBOARD_HEALTH, BusinessHealthResponse.class);
    }

    @Step("Get AI dashboard summary")
    public AISummaryResponse getSummary() {
        return getOk(ApiEndpoints.DASHBOARD_SUMMARY, AISummaryResponse.class);
    }

    @Step("Get revenue trend chart")
    public List<MonthlyAmountResponse> getRevenueTrend() {
        return get(ApiEndpoints.DASHBOARD_CHARTS_REVENUE_TREND).getRaw().jsonPath().getList("", MonthlyAmountResponse.class);
    }

    @Step("Get expense breakdown chart")
    public List<CategoryAmountResponse> getExpenseBreakdown() {
        return get(ApiEndpoints.DASHBOARD_CHARTS_EXPENSE_BREAKDOWN).getRaw().jsonPath().getList("", CategoryAmountResponse.class);
    }

    @Step("Get forecast snapshot widget")
    public ForecastSnapshotResponse getForecastSnapshot() {
        return getOk(ApiEndpoints.DASHBOARD_FORECAST_SNAPSHOT, ForecastSnapshotResponse.class);
    }

    @Step("Get tasks snapshot widget")
    public TaskSnapshotResponse getTasksSnapshot() {
        return getOk(ApiEndpoints.DASHBOARD_TASKS_SNAPSHOT, TaskSnapshotResponse.class);
    }

    @Step("Get business guide snapshot widget")
    public KnowledgeDashboardSnapshotDto getBusinessGuideSnapshot() {
        return getOk(ApiEndpoints.DASHBOARD_BUSINESS_GUIDE_SNAPSHOT, KnowledgeDashboardSnapshotDto.class);
    }

    @Step("Fetch dashboard stats (unchecked)")
    public ApiCallResult<Void> fetchStats() {
        return fetch(ApiEndpoints.DASHBOARD_STATS, Void.class);
    }
}
