package com.flowiq.integration.imports;

import com.flowiq.base.BaseDbTest;
import com.flowiq.db.seeder.TestDataSeeder;
import com.flowiq.utils.RandomDataGenerator;
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
public class ImportsDbIT extends BaseDbTest {

    @Test(groups = {"integration", "db", "imports"})
    @Story("Import job persistence")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Import job metrics are stored for isolated user")
    public void shouldPersistImportJobForIsolatedUser() {
        String fileName = "import-" + RandomDataGenerator.alphanumeric(6) + ".csv";

        long importId = importDb.insert(
                seededUser().getId(),
                fileName,
                2048L,
                "COMPLETED",
                10,
                9,
                1,
                "UNIVERSAL"
        );

        var row = importDb.findById(importId).orElseThrow();
        assertThat(row.userId()).isEqualTo(seededUser().getId());
        assertThat(row.fileName()).isEqualTo(fileName);
        assertThat(row.rowsImported()).isEqualTo(9);
        assertThat(row.status()).isEqualTo("COMPLETED");
    }

    @Test(groups = {"integration", "db", "imports"})
    @Story("User isolation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Import jobs are isolated between users")
    public void shouldIsolateImportJobsByUser() {
        var otherUser = TestDataSeeder.seedUser(dataSource);

        importDb.insert(seededUser().getId(), "a.csv", 100L, "COMPLETED", 5, 5, 0, "UNIVERSAL");
        importDb.insert(otherUser.getId(), "b.csv", 200L, "PARTIAL", 8, 6, 2, "MONOBANK");

        assertThat(importDb.countByUserId(seededUser().getId())).isEqualTo(1);
        assertThat(importDb.countByUserId(otherUser.getId())).isEqualTo(1);
    }
}
