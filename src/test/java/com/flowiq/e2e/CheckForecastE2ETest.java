package com.flowiq.e2e;

import com.flowiq.base.BaseE2ETest;
import com.flowiq.base.UiAssertions;
import com.flowiq.models.forecasts.ForecastSummaryResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("E2E")
@Feature("Check Forecast")
public class CheckForecastE2ETest extends BaseE2ETest {

    @Test(groups = {"e2e", "forecasts"})
    @Story("Forecast UI and API")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Forecasts page loads and API summary is available")
    public void shouldDisplayForecastsAndReturnApiSummary() {
        pages.forecasts().open();
        pages.forecasts().waitForSummaryLoaded();

        ForecastSummaryResponse summary = forecastService.getSummary();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.forecasts().isLoaded()).isTrue();
        assertThat(summary.getExpectedRevenue()).isNotNull();
        assertThat(pages.forecasts().getSummaryCardCount()).isGreaterThan(0);
    }
}
