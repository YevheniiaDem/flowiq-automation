package com.flowiq.api.integration.imports;

import com.flowiq.api.integration.base.BaseApiIntegrationTest;
import com.flowiq.api.integration.support.ImportIntegrationSupport;
import com.flowiq.api.integration.support.IntegrationAssertions;
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

@Epic("API Integration")
@Feature("Imports")
public class ImportsIntegrationTest extends BaseApiIntegrationTest {

    @Test(groups = {"api-integration", "api", "imports"})
    @Story("Upload CSV")
    @Severity(SeverityLevel.BLOCKER)
    @Description("POST /imports/upload accepts valid CSV and completes import job")
    public void shouldUploadCsvFile() {
        ImportJobResponse job = importService.upload(TestDataFactory.sampleImportCsv());

        assertThat(job.getId()).isNotNull();
        assertThat(job.getFileName()).endsWith(".csv");

        ImportJobResponse completed = ImportIntegrationSupport.awaitCompletion(importService, job.getId());
        assertThat(completed.getStatus()).isIn("COMPLETED", "PARTIAL");
    }

    @Test(groups = {"api-integration", "api", "imports"})
    @Story("Duplicate import")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Uploading the same CSV twice creates separate import jobs")
    public void shouldAllowDuplicateImportUpload() {
        File csv = TestDataFactory.sampleImportCsv();

        ImportJobResponse first = importService.upload(csv);
        ImportJobResponse second = importService.upload(csv);

        assertThat(first.getId()).isNotEqualTo(second.getId());
        ImportIntegrationSupport.awaitCompletion(importService, first.getId());
        ImportJobResponse secondCompleted = ImportIntegrationSupport.awaitCompletion(importService, second.getId());
        assertThat(secondCompleted.getStatus()).isIn("COMPLETED", "PARTIAL", "FAILED");
    }

    @Test(groups = {"api-integration", "api", "imports"})
    @Story("Invalid file")
    @Severity(SeverityLevel.NORMAL)
    @Description("Non-CSV files are rejected")
    public void shouldRejectInvalidImportFile() {
        IntegrationAssertions.assertValidationError(
                importService.attemptUpload(TestDataFactory.invalidImportFile()));
    }

    @Test(groups = {"api-integration", "api", "imports"})
    @Story("Empty file")
    @Severity(SeverityLevel.NORMAL)
    @Description("Empty CSV files are rejected")
    public void shouldRejectEmptyImportFile() {
        IntegrationAssertions.assertValidationError(
                importService.attemptUpload(TestDataFactory.emptyImportCsv()));
    }

    @Test(groups = {"api-integration", "api", "imports"})
    @Story("Import history")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /imports returns import job history")
    public void shouldReturnImportHistory() {
        ImportJobResponse job = importService.upload(TestDataFactory.sampleImportCsv());
        ImportIntegrationSupport.awaitCompletion(importService, job.getId());

        ImportListResponse history = importService.list();

        assertThat(history.getJobs()).isNotEmpty();
        assertThat(history.getJobs()).anyMatch(item -> job.getId().equals(item.getId()));
    }

    @Test(groups = {"api-integration", "api", "imports"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated import requests are rejected")
    public void shouldRejectUnauthorizedAccess() {
        TokenManager.clear();
        IntegrationAssertions.assertUnauthorized(importService.fetchListUnauthorized());
        loginAsDefaultUser();
    }
}
