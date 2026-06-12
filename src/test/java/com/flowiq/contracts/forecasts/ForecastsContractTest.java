package com.flowiq.contracts.forecasts;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.contracts.ContractAssertions;
import com.flowiq.contracts.ContractSchemas;
import com.flowiq.contracts.base.BaseContractTest;
import com.flowiq.models.forecasts.ForecastSummaryResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("Contract Testing")
@Feature("Forecasts")
public class ForecastsContractTest extends BaseContractTest {

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    @Test(groups = {"contract", "forecasts"})
    @Story("GET /api/forecasts/summary")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Forecast summary response matches contract schema")
    public void forecastSummaryShouldMatchContract() {
        ApiCallResult<ForecastSummaryResponse> result = forecastService.fetchSummary();

        ContractAssertions.assertAllRequired(result, 200, ContractSchemas.FORECASTS_SUMMARY,
                "expectedRevenue", "expectedExpenses", "expectedProfit");
    }
}
