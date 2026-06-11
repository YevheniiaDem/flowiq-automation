package com.flowiq.api.auth;

import com.flowiq.assertions.ApiAssertions;
import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseApiTest;
import com.flowiq.clients.ApiCallResult;
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

@Epic("API Regression")
@Feature("Auth")
public class AuthRegressionApiTest extends BaseApiTest {

    @BeforeMethod(alwaysRun = true)
    public void clearSession() {
        TokenManager.clear();
    }

    @Test(groups = {"regression", "api", "auth"})
    @Story("Login flow")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valid credentials return JWT and persist authenticated session")
    public void shouldLoginAndAccessProtectedEndpoint() {
        AuthResponse authResponse = authService.login(TestDataFactory.defaultLoginRequest());

        assertThat(authResponse.getToken()).isNotBlank();
        UserResponse me = authService.getCurrentUser();
        assertThat(me.getEmail()).isEqualToIgnoringCase(config.testUserEmail());
    }

    @Test(groups = {"regression", "api", "auth"})
    @Story("Registration flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("New user can register and receive authentication token")
    public void shouldRegisterNewUser() {
        AuthResponse authResponse = authService.register(TestDataFactory.randomRegisterRequest());

        assertThat(authResponse.getToken()).isNotBlank();
        assertThat(authResponse.getUser().getEmail()).isNotBlank();
        authService.logout();
    }

    @Test(groups = {"regression", "api", "auth"})
    @Story("Registration validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Duplicate email registration is rejected")
    public void shouldRejectDuplicateRegistration() {
        ApiCallResult<AuthResponse> result = authService.attemptRegister(
                TestDataFactory.registerRequestWithEmail(config.testUserEmail()));

        ApiAssertions.assertStatusCodeOneOf(result, 400, 409);
    }

    @Test(groups = {"regression", "api", "auth"})
    @Story("Logout flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Logout clears session and blocks subsequent protected calls")
    public void shouldLogoutSuccessfully() {
        authService.login(TestDataFactory.defaultLoginRequest());
        authService.logout();

        assertThat(TokenManager.isAuthenticated()).isFalse();
        ApiCallResult<UserResponse> result = authService.fetchCurrentUserUnauthorized();
        ApiAssertions.assertStatusCodeOneOf(result, 401, 403);
    }
}
