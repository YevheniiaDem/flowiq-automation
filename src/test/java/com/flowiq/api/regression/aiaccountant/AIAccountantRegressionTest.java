package com.flowiq.api.regression.aiaccountant;

import com.flowiq.api.regression.base.BaseRegressionApiTest;
import com.flowiq.api.regression.support.RegressionAssertions;
import com.flowiq.api.regression.support.RegressionDataProviders;
import com.flowiq.auth.TokenManager;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.request.AIAccountantChatRequest;
import com.flowiq.models.response.AIAccountantChatResponse;
import com.flowiq.models.response.AIAccountantHealthResponse;
import com.flowiq.models.response.AIRecommendationResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Regression")
@Feature("AI Accountant")
public class AIAccountantRegressionTest extends BaseRegressionApiTest {

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("Health")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /ai-accountant/health returns health score and status")
    public void shouldReturnHealth() {
        AIAccountantHealthResponse health = aiAccountantService.getHealth();

        assertThat(health.getScore()).isBetween(0, 100);
        assertThat(health.getStatus()).isNotBlank();
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("Recommendations")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /ai-accountant/recommendations returns financial recommendations")
    public void shouldReturnRecommendations() {
        List<AIRecommendationResponse> recommendations = aiAccountantService.getRecommendations();

        assertThat(recommendations).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("Tax advisor")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /ai-accountant/tax-advisor returns tax advice")
    public void shouldReturnTaxAdvisor() {
        var taxAdvisor = aiAccountantService.getTaxAdvisor();

        assertThat(taxAdvisor.getCurrentFopGroup()).isNotBlank();
        assertThat(taxAdvisor.getNextTaxPaymentLabel()).isNotBlank();
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("Forecasts")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /ai-accountant/forecasts returns AI forecast summary")
    public void shouldReturnForecasts() {
        var forecasts = aiAccountantService.getForecasts();

        assertThat(forecasts.getHorizons()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("AI chat")
    @Severity(SeverityLevel.BLOCKER)
    @Description("POST /ai-accountant/chat accepts a valid message and returns reply")
    public void shouldSendChatMessage() {
        AIAccountantChatResponse response = aiAccountantService.chat(TestDataFactory.validChatRequest());

        assertThat(response.getReply()).isNotBlank();
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"},
            dataProvider = "invalidChatMessages", dataProviderClass = RegressionDataProviders.class)
    @Story("AI chat validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Empty or blank chat messages are rejected")
    public void shouldRejectInvalidChatMessages(String invalidMessage) {
        AIAccountantChatRequest request = new AIAccountantChatRequest();
        request.setMessage(invalidMessage);

        RegressionAssertions.assertValidationError(aiAccountantService.attemptChat(request));
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated health request is rejected")
    public void shouldRejectUnauthorizedHealthAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(aiAccountantService.fetchHealthUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated recommendations request is rejected")
    public void shouldRejectUnauthorizedRecommendationsAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(aiAccountantService.fetchRecommendationsUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated tax advisor request is rejected")
    public void shouldRejectUnauthorizedTaxAdvisorAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(aiAccountantService.fetchTaxAdvisorUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated chat request is rejected")
    public void shouldRejectUnauthorizedChatAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(
                aiAccountantService.attemptChatUnauthorized(TestDataFactory.validChatRequest()));
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("AI recommendations business rule")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Each recommendation includes id, type, title and description")
    public void shouldEnforceRecommendationsBusinessRule() {
        List<AIRecommendationResponse> recommendations = aiAccountantService.getRecommendations();

        if (!recommendations.isEmpty()) {
            AIRecommendationResponse first = recommendations.get(0);
            assertThat(first.getId()).isNotBlank();
            assertThat(first.getType()).isNotBlank();
            assertThat(first.getTitle()).isNotBlank();
            assertThat(first.getDescription()).isNotBlank();
        }
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("Health")
    @Severity(SeverityLevel.NORMAL)
    @Description("Health fetch returns successful response")
    public void shouldFetchHealthSuccessfully() {
        RegressionAssertions.assertOk(aiAccountantService.fetchHealth());
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("AI chat")
    @Severity(SeverityLevel.NORMAL)
    @Description("Chat with string message shorthand returns non-empty reply")
    public void shouldSendChatMessageViaStringShorthand() {
        AIAccountantChatResponse response = aiAccountantService.chat("Які податки мене чекають?");

        assertThat(response.getReply()).isNotBlank();
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("Recommendations")
    @Severity(SeverityLevel.NORMAL)
    @Description("Recommendations fetch returns successful response")
    public void shouldFetchRecommendationsSuccessfully() {
        RegressionAssertions.assertOk(aiAccountantService.fetchRecommendations());
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("Forecasts")
    @Severity(SeverityLevel.NORMAL)
    @Description("AI forecasts include horizon projections")
    public void shouldReturnForecastsWithHorizons() {
        var forecasts = aiAccountantService.getForecasts();

        assertThat(forecasts.getHorizons()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("Tax advisor")
    @Severity(SeverityLevel.NORMAL)
    @Description("Tax advisor response includes income limit metadata")
    public void shouldReturnTaxAdvisorWithIncomeLimit() {
        var taxAdvisor = aiAccountantService.getTaxAdvisor();

        assertThat(taxAdvisor.getIncomeLimit()).isNotNull();
        assertThat(taxAdvisor.getFopGroupNumber()).isGreaterThan(0);
    }

    @Test(groups = {"api-regression", "regression", "api", "ai-accountant"})
    @Story("Health")
    @Severity(SeverityLevel.NORMAL)
    @Description("Health response includes summary text")
    public void shouldReturnHealthWithSummary() {
        AIAccountantHealthResponse health = aiAccountantService.getHealth();

        assertThat(health.getSummary()).isNotBlank();
    }
}
