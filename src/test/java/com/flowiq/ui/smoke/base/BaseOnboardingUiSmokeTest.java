package com.flowiq.ui.smoke.base;

import com.flowiq.auth.TokenManager;
import com.flowiq.base.AuthenticatedUiTest;
import com.flowiq.base.UiAssertions;
import com.flowiq.clients.ApiClientFactory;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.response.AuthResponse;
import com.flowiq.utils.OnboardingUiHelper;
import org.testng.annotations.BeforeMethod;

/**
 * UI smoke base that preserves onboarding overlays for activation / tour tests.
 */
public abstract class BaseOnboardingUiSmokeTest extends AuthenticatedUiTest {

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "setUpUiTest")
    @Override
    public void authenticateUiSession() {
        authService = new com.flowiq.services.AuthService();
        ApiClientFactory.baseSpec();
        AuthResponse authResponse = authService.login(TestDataFactory.defaultLoginRequest());
        injectAuthWithoutOnboardingDismissal(authResponse);
        OnboardingUiHelper.resetForOnboardingFlow(page);
        page.reload();
        UiAssertions.waitForPageLoad(page);
    }

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "authenticateUiSession")
    public void clearApiTokenForIsolation() {
        TokenManager.clear();
    }
}
