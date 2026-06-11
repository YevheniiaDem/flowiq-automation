package com.flowiq.ui.imports;

import com.flowiq.base.AuthenticatedUiTest;
import com.flowiq.base.UiAssertions;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("UI Smoke")
@Feature("Imports")
public class ImportsUiSmokeTest extends AuthenticatedUiTest {

    @Test(groups = {"smoke", "ui", "imports"})
    @Story("Page load")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Imports page displays upload zone and history table")
    public void shouldDisplayImportsPage() {
        pages.imports().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.imports().isLoaded()).isTrue();
        UiAssertions.assertElementVisible(pages.imports().uploadZone());
        UiAssertions.assertElementVisible(pages.imports().historyTable());
    }
}
