package com.flowiq.ui.smoke;

import com.flowiq.base.UiAssertions;
import com.flowiq.ui.smoke.base.BaseOnboardingUiSmokeTest;
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
@Feature("Onboarding")
public class OnboardingSmokeTest extends BaseOnboardingUiSmokeTest {

    @Test(groups = {"ui-smoke", "smoke", "ui", "onboarding", "activation-checklist"})
    @Story("Activation checklist")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Activation checklist is visible when not dismissed")
    public void shouldDisplayActivationChecklist() {
        OnboardingUiHelper.showActivationChecklist(page);
        pages.dashboard().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.onboarding().activationChecklist().isVisible()).isTrue();
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "onboarding", "help-center", "product-tour"})
    @Story("Help center")
    @Severity(SeverityLevel.NORMAL)
    @Description("Help & Learn center exposes product tour entry point")
    public void shouldOpenProductTourFromHelpCenter() {
        pages.settings().open();

        assertThat(pages.settings().helpCenterItem("checklist").isVisible()).isTrue();
        pages.settings().helpCenterItem("checklist").click();
        UiAssertions.waitForPageLoad(page);
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "onboarding", "demo-workspace"})
    @Story("Demo workspace")
    @Severity(SeverityLevel.NORMAL)
    @Description("Demo workspace banner appears when demo mode is enabled")
    public void shouldShowDemoWorkspaceBanner() {
        OnboardingUiHelper.enableDemoWorkspace(page);
        pages.dashboard().open();

        assertThat(pages.onboarding().demoWorkspaceBanner().isVisible()).isTrue();
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "onboarding", "whats-new"})
    @Story("What's New")
    @Severity(SeverityLevel.NORMAL)
    @Description("What's New modal can appear for fresh onboarding state")
    public void shouldAllowWhatsNewDismissal() {
        page.evaluate("() => localStorage.removeItem('onboarding_whats_new_version')");
        pages.dashboard().open();
        UiAssertions.waitForPageLoad(page);

        if (pages.onboarding().whatsNewModal().isVisible()) {
            page.keyboard().press("Escape");
            assertThat(pages.onboarding().whatsNewModal().isHidden()).isTrue();
        }
    }
}
