package com.flowiq.integration.imports;

import com.flowiq.base.BaseApiDbIT;
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

@Epic("Database Integration")
@Feature("Imports")
public class ImportsApiDbIT extends BaseApiDbIT {

    @Test(groups = {"integration", "api-db", "imports"})
    @Story("API to database sync")
    @Severity(SeverityLevel.BLOCKER)
    @Description("CSV uploaded via API creates import job in Testcontainer database")
    public void shouldPersistApiUploadInDatabase() {
        ImportJobResponse job = importService.upload(TestDataFactory.sampleImportCsv());

        var row = importDb.findById(job.getId()).orElseThrow();
        assertThat(row.userId()).isEqualTo(seededUser().getId());
        assertThat(row.fileName()).endsWith(".csv");
        assertThat(row.status()).isIn("COMPLETED", "PARTIAL", "PROCESSING", "PENDING");
    }
}
