package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.ApiEndpoints;
import com.flowiq.models.forecasts.FopLimitForecastResponse;
import com.flowiq.models.forecasts.ForecastMetricResponse;
import com.flowiq.models.forecasts.ForecastSummaryResponse;
import com.flowiq.models.forecasts.TaxForecastResponse;
import io.qameta.allure.Step;

public class ForecastService extends BaseApiService {

    @Step("Get revenue forecast")
    public ForecastMetricResponse getRevenue() {
        return getOk(ApiEndpoints.FORECASTS_REVENUE, ForecastMetricResponse.class);
    }

    @Step("Get expenses forecast")
    public ForecastMetricResponse getExpenses() {
        return getOk(ApiEndpoints.FORECASTS_EXPENSES, ForecastMetricResponse.class);
    }

    @Step("Get profit forecast")
    public ForecastMetricResponse getProfit() {
        return getOk(ApiEndpoints.FORECASTS_PROFIT, ForecastMetricResponse.class);
    }

    @Step("Get tax forecast")
    public TaxForecastResponse getTaxes() {
        return getOk(ApiEndpoints.FORECASTS_TAXES, TaxForecastResponse.class);
    }

    @Step("Get FOP limit forecast")
    public FopLimitForecastResponse getFopLimit() {
        return getOk(ApiEndpoints.FORECASTS_FOP_LIMIT, FopLimitForecastResponse.class);
    }

    @Step("Get full forecast summary")
    public ForecastSummaryResponse getSummary() {
        return getOk(ApiEndpoints.FORECASTS_SUMMARY, ForecastSummaryResponse.class);
    }

    @Step("Fetch forecast summary (unchecked)")
    public ApiCallResult<ForecastSummaryResponse> fetchSummary() {
        return fetch(ApiEndpoints.FORECASTS_SUMMARY, ForecastSummaryResponse.class);
    }

    @Step("Fetch forecast summary without authentication")
    public ApiCallResult<ForecastSummaryResponse> fetchSummaryUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.FORECASTS_SUMMARY, ForecastSummaryResponse.class);
    }

    @Step("Fetch unknown forecast resource")
    public ApiCallResult<Void> fetchUnknownResource() {
        return ApiCallResult.from(get("/forecasts/unknown-smoke-resource"), Void.class);
    }

    @Step("Fetch revenue forecast (unchecked)")
    public ApiCallResult<ForecastMetricResponse> fetchRevenue() {
        return fetch(ApiEndpoints.FORECASTS_REVENUE, ForecastMetricResponse.class);
    }

    @Step("Fetch expenses forecast (unchecked)")
    public ApiCallResult<ForecastMetricResponse> fetchExpenses() {
        return fetch(ApiEndpoints.FORECASTS_EXPENSES, ForecastMetricResponse.class);
    }

    @Step("Fetch profit forecast (unchecked)")
    public ApiCallResult<ForecastMetricResponse> fetchProfit() {
        return fetch(ApiEndpoints.FORECASTS_PROFIT, ForecastMetricResponse.class);
    }

    @Step("Fetch tax forecast (unchecked)")
    public ApiCallResult<TaxForecastResponse> fetchTaxes() {
        return fetch(ApiEndpoints.FORECASTS_TAXES, TaxForecastResponse.class);
    }

    @Step("Fetch FOP limit forecast (unchecked)")
    public ApiCallResult<FopLimitForecastResponse> fetchFopLimit() {
        return fetch(ApiEndpoints.FORECASTS_FOP_LIMIT, FopLimitForecastResponse.class);
    }

    @Step("Fetch revenue forecast without authentication")
    public ApiCallResult<ForecastMetricResponse> fetchRevenueUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.FORECASTS_REVENUE, ForecastMetricResponse.class);
    }

    @Step("Fetch expenses forecast without authentication")
    public ApiCallResult<ForecastMetricResponse> fetchExpensesUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.FORECASTS_EXPENSES, ForecastMetricResponse.class);
    }

    @Step("Fetch profit forecast without authentication")
    public ApiCallResult<ForecastMetricResponse> fetchProfitUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.FORECASTS_PROFIT, ForecastMetricResponse.class);
    }

    @Step("Fetch tax forecast without authentication")
    public ApiCallResult<TaxForecastResponse> fetchTaxesUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.FORECASTS_TAXES, TaxForecastResponse.class);
    }

    @Step("Fetch FOP limit forecast without authentication")
    public ApiCallResult<FopLimitForecastResponse> fetchFopLimitUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.FORECASTS_FOP_LIMIT, FopLimitForecastResponse.class);
    }
}
