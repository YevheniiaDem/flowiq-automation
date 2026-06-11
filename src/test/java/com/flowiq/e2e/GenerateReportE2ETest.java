package com.flowiq.e2e;

import com.flowiq.base.BaseE2ETest;
import com.flowiq.base.UiAssertions;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("E2E")
@Feature("Generate Report")
public class GenerateReportE2ETest extends BaseE2ETest {

    @Test(groups = {"e2e", "reports"})
    @Story("Generate via UI")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User generates a report from reports page")
    public void shouldGenerateReportViaUi() {
        var reportsPage = pages.reports();
        reportsPage.open();
        reportsPage.generateDefaultReport();

        UiAssertions.waitForPageLoad(page);
        assertThat(reportService.list().getReports()).isNotEmpty();
    }
}
