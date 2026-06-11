package com.flowiq.api.reports;

import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseSmokeApiTest;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.SmokeSchemas;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.response.ReportJobResponse;
import com.flowiq.models.response.ReportListResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Smoke")
@Feature("Reports")
public class ReportsSmokeApiTest extends BaseSmokeApiTest {

    @Test(groups = {"smoke", "api", "reports"})
    @Story("Happy path")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Authenticated user can list generated reports")
    public void shouldListReports() {
        ApiCallResult<ReportListResponse> result = reportService.fetchList();

        assertHappyPath(result);
        assertThat(result.getBody().getReports()).isNotNull();
    }

    @Test(groups = {"smoke", "api", "reports"})
    @Story("Authorization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Reports endpoint requires JWT")
    public void shouldRejectUnauthenticatedAccess() {
        TokenManager.clear();
        assertUnauthorized(reportService.fetchListUnauthorized());
    }

    @Test(groups = {"smoke", "api", "reports"})
    @Story("Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Generate report without required fields is rejected")
    public void shouldRejectInvalidGeneratePayload() {
        ApiCallResult<ReportJobResponse> result =
                reportService.attemptGenerate(TestDataFactory.invalidReportRequest());

        assertValidationError(result);
    }

    @Test(groups = {"smoke", "api", "reports"})
    @Story("Schema")
    @Severity(SeverityLevel.NORMAL)
    @Description("Report list response matches JSON schema")
    public void shouldMatchReportListSchema() {
        ApiCallResult<ReportListResponse> result = reportService.fetchList();

        assertHappyPath(result);
        assertMatchesSchema(result, SmokeSchemas.REPORT_LIST);
    }
}
