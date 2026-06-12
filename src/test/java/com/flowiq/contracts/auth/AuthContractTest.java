package com.flowiq.contracts.auth;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.contracts.ContractAssertions;
import com.flowiq.contracts.ContractSchemas;
import com.flowiq.contracts.base.BaseContractTest;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.response.AuthResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("Contract Testing")
@Feature("Auth")
public class AuthContractTest extends BaseContractTest {

    @Override
    protected boolean requiresAuthentication() {
        return false;
    }

    @Test(groups = {"contract", "auth"})
    @Story("POST /api/auth/login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Login response matches auth contract schema")
    public void loginResponseShouldMatchContract() {
        ApiCallResult<AuthResponse> result = authService.fetchLogin(TestDataFactory.defaultLoginRequest());

        ContractAssertions.assertAllRequired(result, 200, ContractSchemas.AUTH_LOGIN,
                "token", "user", "user.email");
    }

    @Test(groups = {"contract", "auth"})
    @Story("POST /api/auth/register")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Register response matches auth contract schema")
    public void registerResponseShouldMatchContract() {
        ApiCallResult<AuthResponse> result = authService.attemptRegister(TestDataFactory.randomRegisterRequest());

        ContractAssertions.assertAllRequired(result, 201, ContractSchemas.AUTH_REGISTER,
                "token", "user", "user.email");
    }
}
