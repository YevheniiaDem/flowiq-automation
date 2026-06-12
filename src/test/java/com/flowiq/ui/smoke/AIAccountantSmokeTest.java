package com.flowiq.ui.smoke;

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

@Epic("UI Smoke")
@Feature("AI Accountant")
public class AIAccountantSmokeTest extends BaseUiSmokeTest {

    @Test(groups = {"ui-smoke", "smoke", "ui", "ai-accountant"})
    @Story("Chat UI")
    @Severity(SeverityLevel.CRITICAL)
    @Description("AI Accountant chat section and input are visible")
    public void shouldDisplayChatSection() {
        pages.aiAccountant().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.aiAccountant().isLoaded()).isTrue();
        UiAssertions.assertElementVisible(pages.aiAccountant().chatSection());
        UiAssertions.assertElementVisible(pages.aiAccountant().chatInput());
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "ai-accountant"})
    @Story("Send message")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User can send a message in AI Accountant chat")
    public void shouldSendChatMessage() {
        pages.aiAccountant().open();

        int messagesBefore = pages.aiAccountant().getMessageCount();
        pages.aiAccountant().sendMessage("Які мої основні витрати?");

        pages.aiAccountant().waitForResponse();
        assertThat(pages.aiAccountant().getMessageCount()).isGreaterThan(messagesBefore);
    }
}
