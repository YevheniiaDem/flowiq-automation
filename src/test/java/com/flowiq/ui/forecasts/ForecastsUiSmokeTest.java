package com.flowiq.ui.forecasts;

import com.flowiq.base.AuthenticatedUiTest;
import com.flowiq.base.UiAssertions;
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
public class ForecastsUiSmokeTest extends AuthenticatedUiTest {

    @Test(groups = {"smoke", "ui", "forecasts"})
    @Story("Page load")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Forecasts page displays summary cards")
    public void shouldDisplayForecastsPage() {
        pages.forecasts().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.forecasts().isLoaded()).isTrue();
        pages.forecasts().waitForSummaryLoaded();
        assertThat(pages.forecasts().getSummaryCardCount()).isGreaterThan(0);
    }
}
