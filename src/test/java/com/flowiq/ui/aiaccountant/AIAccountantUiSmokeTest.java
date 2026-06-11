package com.flowiq.ui.aiaccountant;

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
@Feature("AI Accountant")
public class AIAccountantUiSmokeTest extends AuthenticatedUiTest {

    @Test(groups = {"smoke", "ui", "ai-accountant"})
    @Story("Page load")
    @Severity(SeverityLevel.CRITICAL)
    @Description("AI Accountant page displays chat section")
    public void shouldDisplayAiAccountantPage() {
        pages.aiAccountant().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.aiAccountant().isLoaded()).isTrue();
        UiAssertions.assertElementVisible(pages.aiAccountant().chatSection());
        UiAssertions.assertElementVisible(pages.aiAccountant().chatInput());
    }
}
