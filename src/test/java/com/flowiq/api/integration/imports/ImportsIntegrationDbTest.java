package com.flowiq.api.integration.imports;

import com.flowiq.api.integration.base.BaseApiIntegrationDbTest;
import com.flowiq.api.integration.support.ImportIntegrationSupport;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.response.ImportJobResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Integration")
@Feature("Imports DB Consistency")
public class ImportsIntegrationDbTest extends BaseApiIntegrationDbTest {

    @Test(groups = {"api-integration", "api-integration-db", "imports"})
    @Story("Data consistency")
    @Severity(SeverityLevel.BLOCKER)
    @Description("CSV upload via API creates import job row in Testcontainer database")
    public void shouldPersistImportJobInDatabase() {
        ImportJobResponse job = importService.upload(TestDataFactory.sampleImportCsv());
        ImportJobResponse completed = ImportIntegrationSupport.awaitCompletion(importService, job.getId());

        var row = importDb.findById(completed.getId()).orElseThrow();
        assertThat(row.userId()).isEqualTo(seededUser().getId());
        assertThat(row.fileName()).endsWith(".csv");
        assertThat(row.status()).isIn("COMPLETED", "PARTIAL", "FAILED");
    }
}
