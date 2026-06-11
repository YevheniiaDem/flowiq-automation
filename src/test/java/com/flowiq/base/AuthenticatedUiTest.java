package com.flowiq.base;

import com.flowiq.auth.TokenManager;
import com.flowiq.clients.ApiClientFactory;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.response.AuthResponse;
import com.flowiq.services.AuthService;
import com.flowiq.support.TestCleanupManager;
import com.flowiq.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

@Slf4j
public abstract class AuthenticatedUiTest extends BaseUiTest {

    protected AuthService authService;

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "setUpUiTest")
    public void authenticateUiSession() {
        authService = new AuthService();
        ApiClientFactory.baseSpec();
        AuthResponse authResponse = authService.login(TestDataFactory.defaultLoginRequest());
        injectAuthIntoBrowser(authResponse);
    }

    @AfterMethod(alwaysRun = true, dependsOnMethods = "tearDownUiTest")
    public void logoutApiSession() {
        try {
            TestCleanupManager.runAll();
            if (TokenManager.isAuthenticated()) {
                authService.logout();
            }
        } catch (Exception e) {
            log.warn("API logout during UI teardown failed", e);
        } finally {
            ApiClientFactory.reset();
            TokenManager.clear();
        }
    }

    protected void injectAuthIntoBrowser(AuthResponse authResponse) {
        page.navigate("/");
        String userJson = JsonUtils.toJson(authResponse.getUser());
        page.evaluate(
                "([token, refreshToken, userJson]) => {"
                        + "localStorage.setItem('token', token);"
                        + "localStorage.setItem('refreshToken', refreshToken);"
                        + "localStorage.setItem('user', userJson);"
                        + "}",
                new Object[]{authResponse.getToken(), authResponse.getRefreshToken(), userJson}
        );
        page.reload();
        UiAssertions.waitForPageLoad(page);
    }
}
