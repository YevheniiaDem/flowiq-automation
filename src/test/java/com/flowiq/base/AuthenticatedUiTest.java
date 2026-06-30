package com.flowiq.base;

import com.flowiq.assertions.ApiAssertions;
import com.flowiq.auth.TokenManager;
import com.flowiq.clients.ApiClientFactory;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.response.AuthResponse;
import com.flowiq.services.AuthService;
import com.flowiq.support.TestCleanupManager;
import com.flowiq.utils.JsonUtils;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
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
        storeAuthInBrowser(authResponse, true);
        UiAssertions.waitForPageLoad(page);
    }

    protected void injectAuthWithoutOnboardingDismissal(AuthResponse authResponse) {
        storeAuthInBrowser(authResponse, false);
        UiAssertions.waitForPageLoad(page);
    }

    private void storeAuthInBrowser(AuthResponse authResponse, boolean dismissOnboarding) {
        page.navigate("/", new Page.NavigateOptions().setWaitUntil(WaitUntilState.COMMIT));
        String userJson = JsonUtils.toJson(authResponse.getUser());
        page.evaluate(
                "([token, refreshToken, userJson, dismissOnboarding]) => {"
                        + "localStorage.setItem('token', token);"
                        + "localStorage.setItem('refreshToken', refreshToken);"
                        + "localStorage.setItem('user', userJson);"
                        + "if (dismissOnboarding) {"
                        + "localStorage.setItem('onboarding_completed', 'true');"
                        + "localStorage.setItem('onboarding_skipped', 'true');"
                        + "localStorage.removeItem('onboarding_pending');"
                        + "localStorage.setItem('onboarding_whats_new_version', '999');"
                        + "localStorage.setItem('onboarding_checklist_dismissed', 'true');"
                        + "localStorage.setItem('onboarding_demo_workspace', 'false');"
                        + "sessionStorage.removeItem('onboarding_tour_step');"
                        + "}"
                        + "}",
                new Object[]{
                        authResponse.getToken(),
                        authResponse.getRefreshToken() != null ? authResponse.getRefreshToken() : "",
                        userJson,
                        dismissOnboarding
                }
        );
        page.navigate("/", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }
}
