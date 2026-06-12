package com.flowiq.ui.smoke;

import com.flowiq.base.UiAssertions;
import com.flowiq.ui.smoke.base.BaseUiSmokeTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("UI Smoke")
@Feature("Forecasts")
public class ForecastsSmokeTest extends BaseUiSmokeTest {

    @Test(groups = {"ui-smoke", "smoke", "ui", "forecasts"})
    @Story("Charts")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Forecast charts are rendered on the page")
    public void shouldDisplayForecastCharts() {
        pages.forecasts().open();

        UiAssertions.waitForPageLoad(page);
        pages.forecasts().waitForSummaryLoaded();
        pages.forecasts().waitForChartsLoaded();
        assertThat(pages.forecasts().getChartCount()).isGreaterThanOrEqualTo(1);
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "forecasts"})
    @Story("Warnings")
    @Severity(SeverityLevel.NORMAL)
    @Description("Forecast warnings section is shown when backend returns warnings")
    public void shouldDisplayForecastWarningsWhenPresent() {
        pages.forecasts().open();

        UiAssertions.waitForPageLoad(page);
        pages.forecasts().waitForSummaryLoaded();

        if (pages.forecasts().hasWarnings()) {
            assertThat(pages.forecasts().warningBanners().count()).isGreaterThan(0);
        } else {
            assertThat(pages.forecasts().getSummaryCardCount()).isGreaterThan(0);
        }
    }
}
