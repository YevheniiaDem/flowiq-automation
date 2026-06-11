package com.flowiq.api.imports;

import com.flowiq.assertions.ApiAssertions;
import com.flowiq.base.BaseRegressionApiTest;
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

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Epic("API Regression")
@Feature("Imports")
public class ImportsRegressionApiTest extends BaseRegressionApiTest {

    @Test(groups = {"regression", "api", "imports"})
    @Story("CSV import")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User can upload CSV and track import job status")
    public void shouldUploadCsvAndCompleteImportJob() {
        ImportJobResponse job = importService.upload(TestDataFactory.sampleImportCsv());

        assertThat(job.getId()).isNotNull();
        assertThat(job.getFileName()).endsWith(".csv");

        ImportJobResponse completed = awaitImportCompletion(job.getId());
        assertThat(completed.getStatus()).isIn("COMPLETED", "PARTIAL");

        ImportListResponse list = importService.list();
        assertThat(list.getJobs()).anyMatch(item -> job.getId().equals(item.getId()));
    }

    @Test(groups = {"regression", "api", "imports"})
    @Story("Import validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Non-CSV files are rejected")
    public void shouldRejectInvalidImportFile() {
        var result = importService.attemptUpload(TestDataFactory.invalidImportFile());
        ApiAssertions.assertStatusCodeOneOf(result, 400, 415);
    }

    private ImportJobResponse awaitImportCompletion(long jobId) {
        return await()
                .atMost(60, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> importService.getById(jobId),
                        job -> !"PENDING".equals(job.getStatus()) && !"PROCESSING".equals(job.getStatus()));
    }
}
