package com.flowiq.api.aiaccountant;

import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseSmokeApiTest;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.SmokeSchemas;
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

@Epic("API Smoke")
@Feature("AI Accountant")
public class AIAccountantSmokeApiTest extends BaseSmokeApiTest {

    @Test(groups = {"smoke", "api", "ai-accountant"})
    @Story("Happy path")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Authenticated user can retrieve AI Accountant health")
    public void shouldGetHealthStatus() {
        ApiCallResult<AIAccountantHealthResponse> result = aiAccountantService.fetchHealth();

        assertHappyPath(result);
        assertThat(result.getBody().getStatus()).isNotBlank();
    }

    @Test(groups = {"smoke", "api", "ai-accountant"})
    @Story("Authorization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("AI Accountant endpoint requires JWT")
    public void shouldRejectUnauthenticatedAccess() {
        TokenManager.clear();
        assertUnauthorized(aiAccountantService.fetchHealthUnauthorized());
    }

    @Test(groups = {"smoke", "api", "ai-accountant"})
    @Story("Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Chat with empty message is rejected")
    public void shouldRejectEmptyChatMessage() {
        ApiCallResult<AIAccountantChatResponse> result =
                aiAccountantService.attemptChat(TestDataFactory.invalidChatRequest());

        assertValidationError(result);
    }

    @Test(groups = {"smoke", "api", "ai-accountant"})
    @Story("Schema")
    @Severity(SeverityLevel.NORMAL)
    @Description("AI Accountant health response matches JSON schema")
    public void shouldMatchHealthSchema() {
        ApiCallResult<AIAccountantHealthResponse> result = aiAccountantService.fetchHealth();

        assertHappyPath(result);
        assertMatchesSchema(result, SmokeSchemas.AI_ACCOUNTANT_HEALTH);
    }
}
