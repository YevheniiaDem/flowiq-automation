package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.clients.BaseResponseSpecification;
import com.flowiq.constants.ApiEndpoints;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.request.AIAccountantChatRequest;
import com.flowiq.models.response.AIAccountantChatResponse;
import com.flowiq.models.response.AIAccountantHealthResponse;
import com.flowiq.models.response.AIRecommendationResponse;
import com.flowiq.models.response.ForecastsResponse;
import com.flowiq.models.response.TaxAdvisorResponse;
import io.qameta.allure.Step;

import java.util.List;

public class AIAccountantService extends BaseApiService {

    @Step("Get AI Accountant health")
    public AIAccountantHealthResponse getHealth() {
        return getOk(ApiEndpoints.AI_ACCOUNTANT_HEALTH, AIAccountantHealthResponse.class);
    }

    @Step("Get AI financial recommendations")
    public List<AIRecommendationResponse> getRecommendations() {
        return getList(ApiEndpoints.AI_ACCOUNTANT_RECOMMENDATIONS, AIRecommendationResponse.class);
    }

    @Step("Get AI tax advisor advice")
    public TaxAdvisorResponse getTaxAdvisor() {
        return getOk(ApiEndpoints.AI_ACCOUNTANT_TAX_ADVISOR, TaxAdvisorResponse.class);
    }

    @Step("Get AI Accountant forecasts")
    public ForecastsResponse getForecasts() {
        return getOk(ApiEndpoints.AI_ACCOUNTANT_FORECASTS, ForecastsResponse.class);
    }

    @Step("Chat with AI Accountant")
    public AIAccountantChatResponse chat(AIAccountantChatRequest request) {
        return BaseResponseSpecification.extractOk(post(ApiEndpoints.AI_ACCOUNTANT_CHAT, request), AIAccountantChatResponse.class);
    }

    @Step("Chat with AI Accountant: {message}")
    public AIAccountantChatResponse chat(String message) {
        return chat(TestDataFactory.chatRequest(message));
    }

    @Step("Fetch AI Accountant health (unchecked)")
    public ApiCallResult<AIAccountantHealthResponse> fetchHealth() {
        return fetch(ApiEndpoints.AI_ACCOUNTANT_HEALTH, AIAccountantHealthResponse.class);
    }

    @Step("Fetch AI Accountant health without authentication")
    public ApiCallResult<AIAccountantHealthResponse> fetchHealthUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.AI_ACCOUNTANT_HEALTH, AIAccountantHealthResponse.class);
    }

    @Step("Attempt chat with AI Accountant")
    public ApiCallResult<AIAccountantChatResponse> attemptChat(AIAccountantChatRequest request) {
        return attemptPost(ApiEndpoints.AI_ACCOUNTANT_CHAT, request, AIAccountantChatResponse.class);
    }

    @Step("Fetch AI recommendations (unchecked)")
    public ApiCallResult<Void> fetchRecommendations() {
        return fetch(ApiEndpoints.AI_ACCOUNTANT_RECOMMENDATIONS, Void.class);
    }

    @Step("Fetch AI recommendations without authentication")
    public ApiCallResult<Void> fetchRecommendationsUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.AI_ACCOUNTANT_RECOMMENDATIONS, Void.class);
    }

    @Step("Fetch AI tax advisor without authentication")
    public ApiCallResult<TaxAdvisorResponse> fetchTaxAdvisorUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.AI_ACCOUNTANT_TAX_ADVISOR, TaxAdvisorResponse.class);
    }

    @Step("Attempt chat without authentication")
    public ApiCallResult<AIAccountantChatResponse> attemptChatUnauthorized(AIAccountantChatRequest request) {
        return ApiCallResult.from(postUnauthenticated(ApiEndpoints.AI_ACCOUNTANT_CHAT, request),
                AIAccountantChatResponse.class);
    }
}
