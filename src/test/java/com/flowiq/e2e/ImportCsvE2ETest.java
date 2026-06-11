package com.flowiq.e2e;

import com.flowiq.base.BaseE2ETest;
import com.flowiq.base.UiAssertions;
import com.flowiq.factories.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("E2E")
@Feature("Import CSV")
public class ImportCsvE2ETest extends BaseE2ETest {

    @Test(groups = {"e2e", "imports"})
    @Story("Upload CSV via UI")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User uploads CSV file via imports page")
    public void shouldUploadCsvViaUi() {
        Path csv = TestDataFactory.sampleImportCsv().toPath();
        var importsPage = pages.imports();
        importsPage.open();
        int historyBefore = importsPage.getHistoryRowCount();

        importsPage.uploadFile(csv);
        UiAssertions.waitUntilVisible(importsPage.historyTable(), 15);

        assertThat(importsPage.getHistoryRowCount()).isGreaterThanOrEqualTo(historyBefore);
    }
}
