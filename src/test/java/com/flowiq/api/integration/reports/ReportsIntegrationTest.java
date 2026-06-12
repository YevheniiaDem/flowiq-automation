package com.flowiq.api.integration.reports;

import com.flowiq.api.integration.base.BaseApiIntegrationTest;
import com.flowiq.api.integration.support.IntegrationAssertions;
import com.flowiq.auth.TokenManager;
import com.flowiq.clients.ApiResponse;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.factories.builders.ReportRequestBuilder;
import com.flowiq.models.request.GenerateReportRequest;
import com.flowiq.models.response.ReportJobResponse;
import com.flowiq.models.response.ReportListResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Integration")
@Feature("Reports")
public class ReportsIntegrationTest extends BaseApiIntegrationTest {

    @Test(groups = {"api-integration", "api", "reports"})
    @Story("Generate CSV")
    @Severity(SeverityLevel.BLOCKER)
    @Description("POST /reports/generate creates a CSV report")
    public void shouldGenerateCsvReport() {
        ReportJobResponse job = generateAndAssert(
                ReportRequestBuilder.profitAndLossPdf()
                        .format(GenerateReportRequest.Format.CSV)
                        .build());
        assertThat(job.getFormat()).isEqualTo("CSV");
    }

    @Test(groups = {"api-integration", "api", "reports"})
    @Story("Generate PDF")
    @Severity(SeverityLevel.BLOCKER)
    @Description("POST /reports/generate creates a PDF report")
    public void shouldGeneratePdfReport() {
        ReportJobResponse job = generateAndAssert(ReportRequestBuilder.profitAndLossPdf().build());
        assertThat(job.getFormat()).isEqualTo("PDF");
    }

    @Test(groups = {"api-integration", "api", "reports"})
    @Story("Generate Excel")
    @Severity(SeverityLevel.BLOCKER)
    @Description("POST /reports/generate creates an Excel report")
    public void shouldGenerateExcelReport() {
        ReportJobResponse job = generateAndAssert(
                ReportRequestBuilder.profitAndLossPdf()
                        .format(GenerateReportRequest.Format.EXCEL)
                        .build());
        assertThat(job.getFormat()).isEqualTo("EXCEL");
    }

    @Test(groups = {"api-integration", "api", "reports"})
    @Story("Download generated report")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /reports/{id}/download returns non-empty file bytes")
    public void shouldDownloadGeneratedReport() {
        ReportJobResponse job = generateAndAssert(ReportRequestBuilder.profitAndLossPdf().build());

        ApiResponse download = reportService.download(job.getId());

        assertThat(download.getStatusCode()).isEqualTo(200);
        assertThat(download.getRaw().asByteArray()).isNotEmpty();
    }

    @Test(groups = {"api-integration", "api", "reports"})
    @Story("Report history")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /reports returns generated report history")
    public void shouldReturnReportHistory() {
        ReportJobResponse job = generateAndAssert(ReportRequestBuilder.profitAndLossPdf().build());

        ReportListResponse history = reportService.list();

        assertThat(history.getReports()).isNotEmpty();
        assertThat(history.getReports())
                .anyMatch(item -> job.getId().equals(item.getId()));
    }

    @Test(groups = {"api-integration", "api", "reports"})
    @Story("Invalid period validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Invalid report generation payload returns 400/422")
    public void shouldRejectInvalidReportGenerationRequest() {
        IntegrationAssertions.assertValidationError(
                reportService.attemptGenerate(TestDataFactory.invalidReportRequest()));
    }

    @Test(groups = {"api-integration", "api", "reports"})
    @Story("Invalid period validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Report preview with inverted date range is handled safely")
    public void shouldHandleInvalidPreviewDateRange() {
        var result = reportService.fetchPreview(Map.of(
                "dateFrom", "2099-12-31",
                "dateTo", "2000-01-01"
        ));
        assertThat(result.getStatusCode()).isIn(200, 400, 422);
    }

    @Test(groups = {"api-integration", "api", "reports"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated report requests are rejected")
    public void shouldRejectUnauthorizedAccess() {
        TokenManager.clear();
        IntegrationAssertions.assertUnauthorized(reportService.fetchListUnauthorized());
        IntegrationAssertions.assertUnauthorized(
                reportService.fetchPreviewUnauthorized(Map.of("periodPreset", "THIS_MONTH")));
        loginAsDefaultUser();
    }

    private ReportJobResponse generateAndAssert(GenerateReportRequest request) {
        ReportJobResponse job = reportService.generate(request);
        assertThat(job.getId()).isNotNull();
        assertThat(job.getStatus()).isEqualTo("COMPLETED");
        return job;
    }
}
