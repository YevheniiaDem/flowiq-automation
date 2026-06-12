package com.flowiq.contracts.reports;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.contracts.ContractAssertions;
import com.flowiq.contracts.ContractSchemas;
import com.flowiq.contracts.base.BaseContractTest;
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

@Epic("Contract Testing")
@Feature("Reports")
public class ReportsContractTest extends BaseContractTest {

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    @Test(groups = {"contract", "reports"})
    @Story("GET /api/reports")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Reports list response matches contract schema")
    public void reportsListShouldMatchContract() {
        ApiCallResult<ReportListResponse> result = reportService.fetchList();

        ContractAssertions.assertAllRequired(result, 200, ContractSchemas.REPORTS_LIST, "reports");
    }

    @Test(groups = {"contract", "reports"})
    @Story("GET /api/reports/preview")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Report preview response matches contract schema")
    public void reportPreviewShouldMatchContract() {
        ApiCallResult<ReportPreviewResponse> result =
                reportService.fetchPreview(Map.of("periodPreset", "THIS_MONTH"));

        ContractAssertions.assertAllRequired(result, 200, ContractSchemas.REPORTS_PREVIEW,
                "revenue", "expenses", "profit");
    }
}
