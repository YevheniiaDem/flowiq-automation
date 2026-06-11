package com.flowiq.api.reports;

import com.flowiq.base.BaseRegressionApiTest;
import com.flowiq.clients.ApiResponse;
import com.flowiq.factories.builders.ReportRequestBuilder;
import com.flowiq.models.request.GenerateReportRequest;
import com.flowiq.models.response.ReportJobResponse;
import com.flowiq.models.response.ReportPreviewResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Regression")
@Feature("Reports")
public class ReportsRegressionApiTest extends BaseRegressionApiTest {

    @Test(groups = {"regression", "api", "reports"})
    @Story("Report preview")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Report preview returns aggregated metrics for selected period")
    public void shouldPreviewReportForCurrentMonth() {
        ReportPreviewResponse preview = reportService.preview(Map.of("periodPreset", "THIS_MONTH"));

        assertThat(preview.getRevenue()).isNotNull();
        assertThat(preview.getExpenses()).isNotNull();
        assertThat(preview.getProfit()).isNotNull();
    }

    @Test(groups = {"regression", "api", "reports"})
    @Story("Report generation")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User can generate and download a PDF report")
    public void shouldGenerateAndDownloadReport() {
        GenerateReportRequest request = ReportRequestBuilder.profitAndLossPdf().build();
        ReportJobResponse job = reportService.generate(request);

        assertThat(job.getId()).isNotNull();
        assertThat(job.getStatus()).isEqualTo("COMPLETED");

        ApiResponse download = reportService.download(job.getId());
        assertThat(download.getStatusCode()).isEqualTo(200);
        assertThat(download.getRaw().asByteArray()).isNotEmpty();
    }
}
