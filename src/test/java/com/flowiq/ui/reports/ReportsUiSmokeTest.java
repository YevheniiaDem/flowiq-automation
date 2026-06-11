package com.flowiq.ui.reports;

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
@Feature("Reports")
public class ReportsUiSmokeTest extends AuthenticatedUiTest {

    @Test(groups = {"smoke", "ui", "reports"})
    @Story("Page load")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Reports page displays generate report action")
    public void shouldDisplayReportsPage() {
        pages.reports().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.reports().isLoaded()).isTrue();
        UiAssertions.assertElementVisible(pages.reports().generateButton());
    }
}
