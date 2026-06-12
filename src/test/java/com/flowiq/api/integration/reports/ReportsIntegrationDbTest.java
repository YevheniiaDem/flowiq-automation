package com.flowiq.api.integration.reports;

import com.flowiq.api.integration.base.BaseApiIntegrationDbTest;
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

@Epic("API Integration")
@Feature("Reports DB Consistency")
public class ReportsIntegrationDbTest extends BaseApiIntegrationDbTest {

    @Test(groups = {"api-integration", "api-integration-db", "reports"})
    @Story("Data consistency")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Report generated via API is stored in Testcontainer database")
    public void shouldPersistGeneratedReportInDatabase() {
        ReportJobResponse job = reportService.generate(ReportRequestBuilder.profitAndLossPdf().build());

        var row = reportDb.findById(job.getId()).orElseThrow();
        assertThat(row.userId()).isEqualTo(seededUser().getId());
        assertThat(row.status()).isEqualTo("COMPLETED");
        assertThat(row.reportType()).isEqualTo("PROFIT_AND_LOSS");
    }
}
