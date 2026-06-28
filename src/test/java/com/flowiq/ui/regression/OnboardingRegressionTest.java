package com.flowiq.ui.regression;

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

@Epic("UI Regression")
@Feature("Onboarding")
public class OnboardingRegressionTest extends BaseOnboardingUiSmokeTest {

    @Test(groups = {"ui-regression", "regression", "ui", "onboarding", "activation-checklist"})
    @Story("Activation checklist")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Activation checklist can be dismissed and stays hidden")
    public void shouldDismissActivationChecklist() {
        OnboardingUiHelper.showActivationChecklist(page);
        pages.dashboard().open();

        assertThat(pages.onboarding().activationChecklist().isVisible()).isTrue();
        pages.onboarding().activationChecklist().locator("button").first().click();
        page.reload();
        UiAssertions.waitForPageLoad(page);
    }

    @Test(groups = {"ui-regression", "regression", "ui", "onboarding", "product-tour"})
    @Story("Product tour")
    @Severity(SeverityLevel.NORMAL)
    @Description("Product tour can be started from Help & Learn center")
    public void shouldStartProductTourFromSettings() {
        pages.settings().open();
        pages.settings().helpCenterItem("checklist").click();
        UiAssertions.waitForPageLoad(page);
    }
}
