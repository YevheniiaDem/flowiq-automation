package com.flowiq.contracts.aiaccountant;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.contracts.ContractAssertions;
import com.flowiq.contracts.ContractSchemas;
import com.flowiq.contracts.base.BaseContractTest;
import com.flowiq.models.response.AIAccountantHealthResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("Contract Testing")
@Feature("AI Accountant")
public class AIAccountantContractTest extends BaseContractTest {

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    @Test(groups = {"contract", "ai-accountant"})
    @Story("GET /api/ai-accountant/health")
    @Severity(SeverityLevel.CRITICAL)
    @Description("AI Accountant health response matches contract schema")
    public void healthShouldMatchContract() {
        ApiCallResult<AIAccountantHealthResponse> result = aiAccountantService.fetchHealth();

        ContractAssertions.assertAllRequired(result, 200, ContractSchemas.AI_ACCOUNTANT_HEALTH,
                "score", "status");
    }
}
