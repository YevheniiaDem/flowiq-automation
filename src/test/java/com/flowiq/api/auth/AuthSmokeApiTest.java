package com.flowiq.api.auth;

import com.flowiq.assertions.ApiAssertions;
import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseApiTest;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.SmokeSchemas;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.response.AuthResponse;
import com.flowiq.models.response.UserResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Smoke")
@Feature("Auth")
public class AuthSmokeApiTest extends BaseApiTest {

    @BeforeMethod(alwaysRun = true)
    public void clearSession() {
        TokenManager.clear();
    }

    @Test(groups = {"smoke", "api", "auth"})
    @Story("Happy path")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Login with demo user returns JWT and user profile")
    public void shouldLoginSuccessfully() {
        ApiCallResult<AuthResponse> result = authService.fetchLogin(TestDataFactory.defaultLoginRequest());

        ApiAssertions.assertStatusCode(result, 200);
        assertThat(result.getBody().getToken()).isNotBlank();
        assertThat(result.getBody().getUser().getEmail()).isNotBlank();
    }

    @Test(groups = {"smoke", "api", "auth"})
    @Story("Authorization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Protected /auth/me endpoint rejects unauthenticated requests")
    public void shouldRejectUnauthenticatedAccess() {
        ApiCallResult<UserResponse> result = authService.fetchCurrentUserUnauthorized();

        ApiAssertions.assertStatusCodeOneOf(result, 401, 403);
    }

    @Test(groups = {"smoke", "api", "auth"})
    @Story("Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Login with empty credentials returns validation error")
    public void shouldRejectInvalidLoginPayload() {
        ApiCallResult<AuthResponse> result = authService.attemptLogin(TestDataFactory.invalidLoginRequest());

        ApiAssertions.assertStatusCodeOneOf(result, 400, 401, 422);
    }

    @Test(groups = {"smoke", "api", "auth"})
    @Story("Schema")
    @Severity(SeverityLevel.NORMAL)
    @Description("Login response matches auth JSON schema")
    public void shouldMatchLoginResponseSchema() {
        ApiCallResult<AuthResponse> result = authService.fetchLogin(TestDataFactory.defaultLoginRequest());

        ApiAssertions.assertStatusCode(result, 200);
        ApiAssertions.assertMatchesSchema(result, SmokeSchemas.AUTH_LOGIN);
    }

    @Test(groups = {"smoke", "api", "auth", "registration"})
    @Story("Registration flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("New user registration returns JWT")
    public void shouldRegisterNewUser() {
        ApiCallResult<AuthResponse> result = authService.attemptRegister(TestDataFactory.randomRegisterRequest());

        ApiAssertions.assertStatusCode(result, 201);
        assertThat(result.getBody().getToken()).isNotBlank();
        authService.logout();
    }
}
