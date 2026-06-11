package com.flowiq.integration.reports;

import com.flowiq.base.BaseApiDbIT;
import com.flowiq.factories.builders.ReportRequestBuilder;
import com.flowiq.models.response.ReportJobResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Database Integration")
@Feature("Reports")
public class ReportsApiDbIT extends BaseApiDbIT {

    @Test(groups = {"integration", "api-db", "reports"})
    @Story("API to database sync")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Report generated via API is stored in Testcontainer database")
    public void shouldPersistApiGeneratedReportInDatabase() {
        ReportJobResponse job = reportService.generate(ReportRequestBuilder.profitAndLossPdf().build());

        var row = reportDb.findById(job.getId()).orElseThrow();
        assertThat(row.userId()).isEqualTo(seededUser().getId());
        assertThat(row.status()).isEqualTo("COMPLETED");
        assertThat(row.reportType()).isEqualTo("PROFIT_AND_LOSS");
    }
}
