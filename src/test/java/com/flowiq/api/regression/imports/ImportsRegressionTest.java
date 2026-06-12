package com.flowiq.api.regression.imports;

import com.flowiq.api.integration.support.ImportIntegrationSupport;
import com.flowiq.api.regression.base.BaseRegressionApiTest;
import com.flowiq.api.regression.support.RegressionAssertions;
import com.flowiq.auth.TokenManager;
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

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Regression")
@Feature("Imports")
public class ImportsRegressionTest extends BaseRegressionApiTest {

    @Test(groups = {"api-regression", "regression", "api", "imports"})
    @Story("Upload CSV")
    @Severity(SeverityLevel.BLOCKER)
    @Description("POST /imports/upload accepts valid CSV and creates import job")
    public void shouldUploadCsvFile() {
        ImportJobResponse job = importService.upload(TestDataFactory.sampleImportCsv());

        assertThat(job.getId()).isNotNull();
        assertThat(job.getFileName()).endsWith(".csv");
        assertThat(job.getStatus()).isIn("PENDING", "PROCESSING", "COMPLETED", "PARTIAL");
    }

    @Test(groups = {"api-regression", "regression", "api", "imports"})
    @Story("Upload CSV")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Uploaded import job completes successfully")
    public void shouldCompleteImportJob() {
        ImportJobResponse job = importService.upload(TestDataFactory.sampleImportCsv());

        ImportJobResponse completed = ImportIntegrationSupport.awaitCompletion(importService, job.getId());

        assertThat(completed.getStatus()).isIn("COMPLETED", "PARTIAL");
    }

    @Test(groups = {"api-regression", "regression", "api", "imports"})
    @Story("Import history")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /imports returns import job history")
    public void shouldListImportJobs() {
        ImportListResponse history = importService.list();

        assertThat(history.getJobs()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "imports"})
    @Story("Get by id")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /imports/{id} returns import job details")
    public void shouldGetImportJobById() {
        ImportJobResponse job = importService.upload(TestDataFactory.sampleImportCsv());
        ImportJobResponse fetched = importService.getById(job.getId());

        assertThat(fetched.getId()).isEqualTo(job.getId());
        assertThat(fetched.getFileName()).isEqualTo(job.getFileName());
    }

    @Test(groups = {"api-regression", "regression", "api", "imports"})
    @Story("Import history")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Import history includes recently uploaded job")
    public void shouldIncludeUploadedJobInHistory() {
        ImportJobResponse job = importService.upload(TestDataFactory.sampleImportCsv());
        ImportIntegrationSupport.awaitCompletion(importService, job.getId());

        ImportListResponse history = importService.list();

        assertThat(history.getJobs())
                .anyMatch(item -> job.getId().equals(item.getId()));
    }

    @Test(groups = {"api-regression", "regression", "api", "imports"})
    @Story("Invalid file")
    @Severity(SeverityLevel.NORMAL)
    @Description("Non-CSV files are rejected")
    public void shouldRejectInvalidImportFile() {
        RegressionAssertions.assertValidationError(
                importService.attemptUpload(TestDataFactory.invalidImportFile()));
    }

    @Test(groups = {"api-regression", "regression", "api", "imports"})
    @Story("Empty file")
    @Severity(SeverityLevel.NORMAL)
    @Description("Empty CSV files are rejected")
    public void shouldRejectEmptyImportFile() {
        RegressionAssertions.assertValidationError(
                importService.attemptUpload(TestDataFactory.emptyImportCsv()));
    }

    @Test(groups = {"api-regression", "regression", "api", "imports"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated import list request is rejected")
    public void shouldRejectUnauthorizedListAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(importService.fetchListUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "imports"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated import upload request is rejected")
    public void shouldRejectUnauthorizedUploadAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(
                importService.attemptUpload(TestDataFactory.sampleImportCsv()));
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "imports"})
    @Story("Not found")
    @Severity(SeverityLevel.NORMAL)
    @Description("Non-existent import job id returns 404")
    public void shouldReturnNotFoundForInvalidId() {
        RegressionAssertions.assertNotFound(importService.fetchById(INVALID_ID));
    }

    @Test(groups = {"api-regression", "regression", "api", "imports"})
    @Story("Duplicate import")
    @Severity(SeverityLevel.NORMAL)
    @Description("Uploading the same CSV twice creates separate import jobs")
    public void shouldAllowDuplicateImportUpload() {
        File csv = TestDataFactory.sampleImportCsv();

        ImportJobResponse first = importService.upload(csv);
        ImportJobResponse second = importService.upload(csv);

        assertThat(first.getId()).isNotEqualTo(second.getId());
    }

    @Test(groups = {"api-regression", "regression", "api", "imports"})
    @Story("Upload CSV")
    @Severity(SeverityLevel.NORMAL)
    @Description("Import job response includes row counters")
    public void shouldReturnImportJobWithRowCounts() {
        ImportJobResponse job = importService.upload(TestDataFactory.sampleImportCsv());
        ImportJobResponse completed = ImportIntegrationSupport.awaitCompletion(importService, job.getId());

        assertThat(completed.getRowsProcessed()).isGreaterThanOrEqualTo(0);
        assertThat(completed.getRowsImported()).isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {"api-regression", "regression", "api", "imports"})
    @Story("Get by id")
    @Severity(SeverityLevel.NORMAL)
    @Description("Import job by id returns consistent status after completion")
    public void shouldReflectCompletedStatusOnGetById() {
        ImportJobResponse job = importService.upload(TestDataFactory.sampleImportCsv());
        ImportIntegrationSupport.awaitCompletion(importService, job.getId());

        ImportJobResponse fetched = importService.getById(job.getId());

        assertThat(fetched.getStatus()).isIn("COMPLETED", "PARTIAL", "FAILED");
    }

    @Test(groups = {"api-regression", "regression", "api", "imports"})
    @Story("Import history")
    @Severity(SeverityLevel.NORMAL)
    @Description("Import list fetch returns successful response")
    public void shouldFetchImportListSuccessfully() {
        RegressionAssertions.assertOk(importService.fetchList());
    }
}
