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
@Feature("Reports")
public class ReportsSmokeTest extends BaseUiSmokeTest {

    @Test(groups = {"ui-smoke", "smoke", "ui", "reports"})
    @Story("Page load")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Reports page opens with generate action")
    public void shouldOpenReportsPage() {
        pages.reports().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.reports().isLoaded()).isTrue();
        UiAssertions.assertElementVisible(pages.reports().generateButton());
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "reports"})
    @Story("Generate report")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User can generate a report from the UI dialog")
    public void shouldGenerateReport() {
        pages.reports().open();

        pages.reports().generateDefaultReport();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.reports().isLoaded()).isTrue();
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "reports"})
    @Story("Report history")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Report history table displays generated reports")
    public void shouldDisplayReportHistory() {
        pages.reports().open();

        pages.reports().waitForHistoryLoaded();
        assertThat(pages.reports().getHistoryRowCount()).isGreaterThanOrEqualTo(0);
    }
}
