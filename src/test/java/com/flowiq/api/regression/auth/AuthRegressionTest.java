package com.flowiq.api.regression.auth;

import com.flowiq.api.regression.support.RegressionAssertions;
import com.flowiq.api.regression.support.RegressionDataProviders;
import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseApiTest;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.request.LoginRequest;
import com.flowiq.models.request.RegisterRequest;
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
public class AuthRegressionTest extends BaseApiTest {

    @BeforeMethod(alwaysRun = true)
    public void clearSession() {
        TokenManager.clear();
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Login flow")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valid credentials return JWT and persist authenticated session")
    public void shouldLoginWithValidCredentials() {
        AuthResponse authResponse = authService.login(TestDataFactory.defaultLoginRequest());

        assertThat(authResponse.getToken()).isNotBlank();
        assertThat(TokenManager.isAuthenticated()).isTrue();
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Login flow")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Login response includes user email matching credentials")
    public void shouldReturnUserProfileOnLogin() {
        AuthResponse authResponse = authService.login(TestDataFactory.defaultLoginRequest());

        assertThat(authResponse.getUser()).isNotNull();
        assertThat(authResponse.getUser().getEmail())
                .isEqualToIgnoringCase(config.testUserEmail());
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Login flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Authenticated user can access /auth/me")
    public void shouldAccessMeAfterLogin() {
        authService.login(TestDataFactory.defaultLoginRequest());

        UserResponse me = authService.getCurrentUser();

        assertThat(me.getEmail()).isEqualToIgnoringCase(config.testUserEmail());
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Registration flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("New user can register and receive authentication token")
    public void shouldRegisterNewUser() {
        AuthResponse authResponse = authService.register(TestDataFactory.randomRegisterRequest());

        assertThat(authResponse.getToken()).isNotBlank();
        assertThat(authResponse.getUser().getEmail()).isNotBlank();
        authService.logout();
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Registration flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Registration persists session allowing immediate /auth/me access")
    public void shouldAccessMeAfterRegistration() {
        RegisterRequest request = TestDataFactory.randomRegisterRequest();
        authService.register(request);

        UserResponse me = authService.getCurrentUser();

        assertThat(me.getEmail()).isEqualToIgnoringCase(request.getEmail());
        authService.logout();
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Registration validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Duplicate email registration is rejected")
    public void shouldRejectDuplicateRegistration() {
        ApiCallResult<AuthResponse> result = authService.attemptRegister(
                TestDataFactory.registerRequestWithEmail(config.testUserEmail()));

        RegressionAssertions.assertConflict(result);
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Logout flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Logout clears session and blocks subsequent protected calls")
    public void shouldLogoutSuccessfully() {
        authService.login(TestDataFactory.defaultLoginRequest());
        authService.logout();

        assertThat(TokenManager.isAuthenticated()).isFalse();
        RegressionAssertions.assertUnauthorized(authService.fetchCurrentUserUnauthorized());
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Authorization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated /auth/me request is rejected")
    public void shouldRejectUnauthorizedMe() {
        RegressionAssertions.assertUnauthorized(authService.fetchCurrentUserUnauthorized());
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Login validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Login with empty credentials returns validation error")
    public void shouldRejectInvalidLoginPayload() {
        RegressionAssertions.assertValidationError(
                authService.attemptLogin(TestDataFactory.invalidLoginRequest()));
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Login validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Login with wrong password is rejected")
    public void shouldRejectLoginWithWrongPassword() {
        LoginRequest request = TestDataFactory.loginRequest(config.testUserEmail(), "wrong-password-xyz");
        ApiCallResult<AuthResponse> result = authService.attemptLogin(request);

        assertThat(result.getStatusCode()).isIn(400, 401, 403, 422);
        assertThat(result.isSuccessful()).isFalse();
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Login validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Login with unknown email is rejected")
    public void shouldRejectLoginWithUnknownEmail() {
        LoginRequest request = TestDataFactory.loginRequest("unknown@example.com", "SomePassword123!");
        ApiCallResult<AuthResponse> result = authService.attemptLogin(request);

        assertThat(result.getStatusCode()).isIn(400, 401, 404, 422);
        assertThat(result.isSuccessful()).isFalse();
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"},
            dataProvider = "invalidEmails", dataProviderClass = RegressionDataProviders.class)
    @Story("Registration validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Registration rejects invalid email formats")
    public void shouldRejectRegistrationWithInvalidEmail(String invalidEmail) {
        RegisterRequest request = TestDataFactory.randomRegisterRequest();
        request.setEmail(invalidEmail);

        RegressionAssertions.assertValidationError(authService.attemptRegister(request));
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"},
            dataProvider = "invalidPasswords", dataProviderClass = RegressionDataProviders.class)
    @Story("Registration validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Registration rejects invalid passwords")
    public void shouldRejectRegistrationWithInvalidPassword(String invalidPassword) {
        RegisterRequest request = TestDataFactory.randomRegisterRequest();
        request.setPassword(invalidPassword);

        RegressionAssertions.assertValidationError(authService.attemptRegister(request));
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"},
            dataProvider = "invalidEmails", dataProviderClass = RegressionDataProviders.class)
    @Story("Login validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Login rejects invalid email formats")
    public void shouldRejectLoginWithInvalidEmail(String invalidEmail) {
        LoginRequest request = TestDataFactory.loginRequest(invalidEmail, "SomePassword123!");
        ApiCallResult<AuthResponse> result = authService.attemptLogin(request);

        assertThat(result.getStatusCode()).isIn(400, 401, 422);
        assertThat(result.isSuccessful()).isFalse();
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Registration validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Registration without name is rejected")
    public void shouldRejectRegistrationWithoutName() {
        RegisterRequest request = TestDataFactory.randomRegisterRequest();
        request.setName("");

        RegressionAssertions.assertValidationError(authService.attemptRegister(request));
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Logout flow")
    @Severity(SeverityLevel.NORMAL)
    @Description("Logout without prior login is safe and leaves session cleared")
    public void shouldHandleLogoutWithoutActiveSession() {
        authService.logout();

        assertThat(TokenManager.isAuthenticated()).isFalse();
        RegressionAssertions.assertUnauthorized(authService.fetchCurrentUserUnauthorized());
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Login flow")
    @Severity(SeverityLevel.NORMAL)
    @Description("Re-login after logout restores authenticated session")
    public void shouldLoginAgainAfterLogout() {
        authService.login(TestDataFactory.defaultLoginRequest());
        authService.logout();

        AuthResponse secondLogin = authService.login(TestDataFactory.defaultLoginRequest());

        assertThat(secondLogin.getToken()).isNotBlank();
        assertThat(authService.getCurrentUser().getEmail())
                .isEqualToIgnoringCase(config.testUserEmail());
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"},
            dataProvider = "invalidPasswords", dataProviderClass = RegressionDataProviders.class)
    @Story("Login validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Login rejects invalid passwords")
    public void shouldRejectLoginWithInvalidPassword(String invalidPassword) {
        LoginRequest request = TestDataFactory.loginRequest(config.testUserEmail(), invalidPassword);
        ApiCallResult<AuthResponse> result = authService.attemptLogin(request);

        assertThat(result.getStatusCode()).isIn(400, 401, 403, 422);
        assertThat(result.isSuccessful()).isFalse();
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Login flow")
    @Severity(SeverityLevel.NORMAL)
    @Description("Authenticated session persists across multiple /auth/me calls")
    public void shouldPersistSessionAcrossMultipleMeCalls() {
        authService.login(TestDataFactory.defaultLoginRequest());

        UserResponse first = authService.getCurrentUser();
        UserResponse second = authService.getCurrentUser();

        assertThat(first.getEmail()).isEqualToIgnoringCase(second.getEmail());
        assertThat(TokenManager.isAuthenticated()).isTrue();
    }

    @Test(groups = {"api-regression", "regression", "api", "auth"})
    @Story("Registration flow")
    @Severity(SeverityLevel.NORMAL)
    @Description("Registered user can login with same credentials")
    public void shouldLoginAfterRegistration() {
        RegisterRequest request = TestDataFactory.randomRegisterRequest();
        authService.register(request);
        authService.logout();

        AuthResponse loginResponse = authService.login(
                TestDataFactory.loginRequest(request.getEmail(), request.getPassword()));

        assertThat(loginResponse.getToken()).isNotBlank();
        assertThat(authService.getCurrentUser().getEmail())
                .isEqualToIgnoringCase(request.getEmail());
        authService.logout();
    }
}
