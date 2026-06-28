package com.flowiq.ui.smoke;

import com.flowiq.base.UiAssertions;
import com.flowiq.ui.smoke.base.BaseUiSmokeTest;
import com.flowiq.utils.OnboardingUiHelper;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("UI Smoke")
@Feature("Settings")
public class SettingsSmokeTest extends BaseUiSmokeTest {

    @Test(groups = {"ui-smoke", "smoke", "ui", "settings"})
    @Story("Page load")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Settings page loads with tabs")
    public void shouldOpenSettingsPage() {
        pages.settings().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.settings().pageRoot().isVisible()).isTrue();
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "settings", "profile"})
    @Story("Profile tab")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Profile tab opens personal settings form")
    public void shouldOpenProfileTab() {
        pages.settings().open().openTabByIndex(1);

        assertThat(page.locator("input").first().isVisible()).isTrue();
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "settings", "security"})
    @Story("Security tab")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Security tab opens password change form")
    public void shouldOpenSecurityTab() {
        pages.settings().open().openTabByIndex(2);

        assertThat(page.locator("input[type='password']").first().isVisible()).isTrue();
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "settings", "help-center"})
    @Story("Help & Learn")
    @Severity(SeverityLevel.NORMAL)
    @Description("Help & Learn center is visible on general settings")
    public void shouldDisplayHelpLearnCenter() {
        pages.settings().open();

        assertThat(pages.settings().helpLearnCenter().isVisible()).isTrue();
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "settings", "demo-workspace"})
    @Story("Demo workspace")
    @Severity(SeverityLevel.NORMAL)
    @Description("Demo workspace can be enabled from Help & Learn center")
    public void shouldEnableDemoWorkspaceFromHelpCenter() {
        pages.settings().open();
        OnboardingUiHelper.enableDemoWorkspace(page);
        page.reload();
        UiAssertions.waitForPageLoad(page);

        assertThat(pages.settings().demoWorkspaceBanner().isVisible()).isTrue();
    }
}
