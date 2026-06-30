package com.flowiq.api.regression.reports;

import com.flowiq.api.regression.base.BaseRegressionApiTest;
import com.flowiq.api.regression.support.RegressionAssertions;
import com.flowiq.api.regression.support.RegressionDataProviders;
import com.flowiq.auth.TokenManager;
import com.flowiq.clients.ApiResponse;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.factories.builders.ReportRequestBuilder;
import com.flowiq.models.request.GenerateReportRequest;
import com.flowiq.models.response.ReportJobResponse;
import com.flowiq.models.response.ReportListResponse;
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
public class ReportsRegressionTest extends BaseRegressionApiTest {

    @Test(groups = {"api-regression", "regression", "api", "reports"})
    @Story("Report list")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /reports returns generated report history")
    public void shouldListReports() {
        ReportListResponse list = reportService.list();

        assertThat(list.getReports()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "reports"},
            dataProvider = "reportPeriodPresets", dataProviderClass = RegressionDataProviders.class)
    @Story("Report preview")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Report preview returns aggregated metrics for selected period preset")
    public void shouldPreviewReportForPeriodPreset(String periodPreset) {
        ReportPreviewResponse preview = reportService.preview(Map.of("periodPreset", periodPreset));

        assertThat(preview.getRevenue()).isNotNull();
        assertThat(preview.getExpenses()).isNotNull();
        assertThat(preview.getProfit()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "reports"},
            dataProvider = "reportTypeFormatCombinations", dataProviderClass = RegressionDataProviders.class)
    @Story("Report generation")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User can generate reports for all type and format combinations")
    public void shouldGenerateReportForTypeAndFormat(
            GenerateReportRequest.ReportType reportType,
            GenerateReportRequest.Format format) {
        GenerateReportRequest request = ReportRequestBuilder.profitAndLossPdf()
                .type(reportType)
                .format(format)
                .periodPreset("THIS_MONTH")
                .build();

        ReportJobResponse job = reportService.generate(request);

        assertThat(job.getId()).isNotNull();
        assertThat(job.getStatus()).isEqualTo("COMPLETED");
        assertThat(job.getReportType()).isEqualTo(reportType.name());
    }

    @Test(groups = {"api-regression", "regression", "api", "reports"})
    @Story("Report download")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /reports/{id}/download returns non-empty file bytes")
    public void shouldDownloadGeneratedReport() {
        ReportJobResponse job = generateDefaultReport();

        ApiResponse download = reportService.download(job.getId());

        assertThat(download.getStatusCode()).isEqualTo(200);
        assertThat(download.getRaw().asByteArray()).isNotEmpty();
    }

    @Test(groups = {"api-regression", "regression", "api", "reports"})
    @Story("Get by id")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /reports/{id} returns report job details")
    public void shouldGetReportById() {
        ReportJobResponse job = generateDefaultReport();

        ReportJobResponse fetched = reportService.getById(job.getId());

        assertThat(fetched.getId()).isEqualTo(job.getId());
        assertThat(fetched.getStatus()).isEqualTo("COMPLETED");
    }

    @Test(groups = {"api-regression", "regression", "api", "reports"})
    @Story("Validation errors")
    @Severity(SeverityLevel.NORMAL)
    @Description("Invalid report generation payload returns 400/422")
    public void shouldRejectInvalidReportGenerationRequest() {
        RegressionAssertions.assertValidationError(
                reportService.attemptGenerate(TestDataFactory.invalidReportRequest()));
    }

    @Test(groups = {"api-regression", "regression", "api", "reports"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated report list request is rejected")
    public void shouldRejectUnauthorizedListAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(reportService.fetchListUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "reports"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated report preview request is rejected")
    public void shouldRejectUnauthorizedPreviewAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(
                reportService.fetchPreviewUnauthorized(Map.of("periodPreset", "THIS_MONTH")));
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "reports"})
    @Story("Not found")
    @Severity(SeverityLevel.NORMAL)
    @Description("Non-existent report id returns 404")
    public void shouldReturnNotFoundForInvalidId() {
        RegressionAssertions.assertNotFound(reportService.fetchById(INVALID_ID));
    }

    @Test(groups = {"api-regression", "regression", "api", "reports"})
    @Story("Report preview")
    @Severity(SeverityLevel.NORMAL)
    @Description("Report preview profit equals revenue minus expenses")
    public void shouldKeepPreviewProfitConsistent() {
        ReportPreviewResponse preview = reportService.preview(Map.of("periodPreset", "THIS_MONTH"));

        if (preview.getRevenue() != null && preview.getExpenses() != null && preview.getProfit() != null) {
            assertThat(preview.getProfit())
                    .isEqualByComparingTo(preview.getRevenue().subtract(preview.getExpenses()));
        }
    }

    @Test(groups = {"api-regression", "regression", "api", "reports"})
    @Story("Report list")
    @Severity(SeverityLevel.NORMAL)
    @Description("Generated report appears in report history")
    public void shouldIncludeGeneratedReportInHistory() {
        ReportJobResponse job = generateDefaultReport();

        ReportListResponse history = reportService.list();

        assertThat(history.getReports())
                .anyMatch(item -> job.getId().equals(item.getId()));
    }

    @Test(groups = {"api-regression", "regression", "api", "reports"})
    @Story("Not found")
    @Severity(SeverityLevel.NORMAL)
    @Description("Downloading non-existent report returns 404")
    public void shouldRejectDownloadForInvalidId() {
        RegressionAssertions.assertNotFound(reportService.attemptDownload(INVALID_ID));
    }

    @Test(groups = {"api-regression", "regression", "api", "reports"})
    @Story("Report preview")
    @Severity(SeverityLevel.NORMAL)
    @Description("Report preview with inverted date range is handled safely")
    public void shouldHandleInvalidPreviewDateRange() {
        var result = reportService.fetchPreview(Map.of(
                "dateFrom", "2099-12-31",
                "dateTo", "2000-01-01"
        ));
        assertThat(result.getStatusCode()).isIn(200, 400, 422);
    }

    @Test(groups = {"api-regression", "regression", "api", "reports"})
    @Story("Report generation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Generated report includes file metadata")
    public void shouldReturnReportWithFileMetadata() {
        ReportJobResponse job = generateDefaultReport();

        assertThat(job.getFileName()).isNotBlank();
        assertThat(job.getFormat()).isNotBlank();
        assertThat(job.getCreatedAt()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "reports"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated report generation is rejected")
    public void shouldRejectUnauthorizedGenerateAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(
                reportService.attemptGenerateUnauthorized(TestDataFactory.validReportRequest()));
        loginAsDefaultUser();
    }

    private ReportJobResponse generateDefaultReport() {
        ReportJobResponse job = reportService.generate(ReportRequestBuilder.profitAndLossPdf().build());
        assertThat(job.getId()).isNotNull();
        assertThat(job.getStatus()).isEqualTo("COMPLETED");
        return job;
    }
}
