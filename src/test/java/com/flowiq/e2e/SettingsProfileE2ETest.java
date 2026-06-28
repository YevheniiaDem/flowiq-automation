package com.flowiq.e2e;

import com.flowiq.base.BaseE2ETest;
import com.flowiq.base.UiAssertions;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("E2E")
@Feature("Settings")
public class SettingsProfileE2ETest extends BaseE2ETest {

    @Test(groups = {"e2e", "settings", "profile", "security"})
    @Story("Settings navigation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User navigates settings profile and security tabs")
    public void shouldNavigateProfileAndSecurityTabs() {
        pages.settings().open().openTabByIndex(1);
        assertThat(page.locator("input").first().isVisible()).isTrue();

        pages.settings().openTabByIndex(2);
        assertThat(page.locator("input[type='password']").first().isVisible()).isTrue();

        UiAssertions.waitForPageLoad(page);
    }
}
