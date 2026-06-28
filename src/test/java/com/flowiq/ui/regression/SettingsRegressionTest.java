package com.flowiq.ui.regression;

import com.flowiq.base.UiAssertions;
import com.flowiq.ui.smoke.base.BaseUiSmokeTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("UI Regression")
@Feature("Settings")
public class SettingsRegressionTest extends BaseUiSmokeTest {

    @Test(groups = {"ui-regression", "regression", "ui", "settings", "profile"})
    @Story("Profile persistence")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Profile tab remains accessible after reload")
    public void shouldKeepProfileTabAccessibleAfterReload() {
        pages.settings().open().openTabByIndex(1);
        page.reload();
        UiAssertions.waitForPageLoad(page);

        assertThat(pages.settings().pageRoot().isVisible()).isTrue();
        assertThat(page.locator("input").first().isVisible()).isTrue();
    }

    @Test(groups = {"ui-regression", "regression", "ui", "settings", "help-center"})
    @Story("Help center")
    @Severity(SeverityLevel.NORMAL)
    @Description("Help center lists activation checklist guide")
    public void shouldListActivationChecklistGuide() {
        pages.settings().open();

        assertThat(pages.settings().helpCenterItem("checklist").isVisible()).isTrue();
    }
}
