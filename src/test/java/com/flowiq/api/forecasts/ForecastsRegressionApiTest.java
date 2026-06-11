package com.flowiq.api.forecasts;

import com.flowiq.base.BaseRegressionApiTest;
import com.flowiq.models.forecasts.ForecastSummaryResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Regression")
@Feature("Forecasts")
public class ForecastsRegressionApiTest extends BaseRegressionApiTest {

    @Test(groups = {"regression", "api", "forecasts"})
    @Story("Forecast summary")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Forecast summary returns projected revenue, expenses and warnings")
    public void shouldReturnForecastSummary() {
        ForecastSummaryResponse summary = forecastService.getSummary();

        assertThat(summary.getExpectedRevenue()).isNotNull();
        assertThat(summary.getExpectedExpenses()).isNotNull();
        assertThat(summary.getExpectedProfit()).isNotNull();
        assertThat(summary.getWarnings()).isNotNull();
    }

    @Test(groups = {"regression", "api", "forecasts"})
    @Story("Forecast metrics")
    @Severity(SeverityLevel.NORMAL)
    @Description("All forecast metric endpoints return data")
    public void shouldReturnAllForecastMetrics() {
        var soft = softAssert();

        soft.assertThat(forecastService.getRevenue()).isNotNull();
        soft.assertThat(forecastService.getExpenses()).isNotNull();
        soft.assertThat(forecastService.getProfit()).isNotNull();
        soft.assertThat(forecastService.getTaxes()).isNotNull();
        soft.assertThat(forecastService.getFopLimit()).isNotNull();

        soft.assertAll();
    }
}
