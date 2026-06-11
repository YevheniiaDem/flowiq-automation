package com.flowiq.ui.login;

import com.flowiq.base.BaseUiTest;
import com.flowiq.base.UiAssertions;
import com.flowiq.constants.UiPaths;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.request.LoginRequest;
import com.flowiq.pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("UI Smoke")
@Feature("Login")
public class LoginUiTest extends BaseUiTest {

    @Test(groups = {"smoke", "ui", "login"})
    @Severity(SeverityLevel.BLOCKER)
    @Description("Login page displays email and password fields")
    public void shouldDisplayLoginPage() {
        LoginPage loginPage = new LoginPage(page).open();

        UiAssertions.waitForPageLoad(page);
        UiAssertions.assertPageUrlContains(page, UiPaths.LOGIN);
        assertThat(loginPage.isDisplayed()).isTrue();
    }

    @Test(groups = {"smoke", "ui", "login"})
    @Severity(SeverityLevel.CRITICAL)
    @Description("Demo user can login and reach dashboard")
    public void shouldLoginWithDemoUserAndReachDashboard() {
        LoginRequest credentials = TestDataFactory.defaultLoginRequest();

        new LoginPage(page).login(credentials.getEmail(), credentials.getPassword());

        UiAssertions.waitForPageLoad(page);
        page.waitForURL(url -> !url.contains(UiPaths.LOGIN), new com.microsoft.playwright.Page.WaitForURLOptions().setTimeout(config.uiTimeout()));
        assertThat(page.url())
                .as("URL after login")
                .doesNotContain(UiPaths.LOGIN);
    }
}
