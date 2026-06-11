package com.flowiq.api.imports;

import com.flowiq.assertions.ApiAssertions;
import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseSmokeApiTest;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.SmokeSchemas;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.response.ImportJobResponse;
import com.flowiq.models.response.ImportListResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Smoke")
@Feature("Imports")
public class ImportsSmokeApiTest extends BaseSmokeApiTest {

    @Test(groups = {"smoke", "api", "imports"})
    @Story("Happy path")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Authenticated user can upload universal CSV and list import jobs")
    public void shouldUploadCsvAndListImports() {
        ApiCallResult<ImportJobResponse> uploadResult =
                importService.attemptUpload(TestDataFactory.sampleImportCsv());

        ApiAssertions.assertStatusCode(uploadResult, 201);
        assertThat(uploadResult.getBody().getId()).isNotNull();

        ApiCallResult<ImportListResponse> listResult = importService.fetchList();
        assertHappyPath(listResult);
        assertThat(listResult.getBody().getJobs()).isNotNull();
    }

    @Test(groups = {"smoke", "api", "imports"})
    @Story("Authorization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Imports endpoint requires JWT")
    public void shouldRejectUnauthenticatedAccess() {
        TokenManager.clear();
        assertUnauthorized(importService.fetchListUnauthorized());
    }

    @Test(groups = {"smoke", "api", "imports"})
    @Story("Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Non-CSV file upload is rejected")
    public void shouldRejectInvalidFileUpload() {
        ApiCallResult<ImportJobResponse> result =
                importService.attemptUpload(TestDataFactory.invalidImportFile());

        assertValidationError(result);
    }

    @Test(groups = {"smoke", "api", "imports"})
    @Story("Schema")
    @Severity(SeverityLevel.NORMAL)
    @Description("Import list response matches JSON schema")
    public void shouldMatchImportListSchema() {
        ApiCallResult<ImportListResponse> result = importService.fetchList();

        assertHappyPath(result);
        assertMatchesSchema(result, SmokeSchemas.IMPORT_LIST);
    }
}
