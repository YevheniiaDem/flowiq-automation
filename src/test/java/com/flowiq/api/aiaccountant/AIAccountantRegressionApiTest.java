package com.flowiq.api.aiaccountant;

import com.flowiq.base.BaseRegressionApiTest;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.response.AIAccountantChatResponse;
import com.flowiq.models.response.AIAccountantHealthResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Regression")
@Feature("AI Accountant")
public class AIAccountantRegressionApiTest extends BaseRegressionApiTest {

    @Test(groups = {"regression", "api", "ai-accountant"})
    @Story("Health and recommendations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("AI Accountant health and recommendations are available")
    public void shouldReturnHealthAndRecommendations() {
        AIAccountantHealthResponse health = aiAccountantService.getHealth();

        assertThat(health.getScore()).isBetween(0, 100);
        assertThat(health.getStatus()).isNotBlank();
        assertThat(aiAccountantService.getRecommendations()).isNotNull();
    }

    @Test(groups = {"regression", "api", "ai-accountant"})
    @Story("AI chat")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User can send a message to AI Accountant chat")
    public void shouldSendChatMessage() {
        AIAccountantChatResponse response = aiAccountantService.chat(TestDataFactory.validChatRequest());

        assertThat(response.getReply()).isNotBlank();
    }
}
