package com.flowiq.ui.smoke;

import com.flowiq.base.BaseUiTest;
import com.flowiq.base.UiAssertions;
import com.flowiq.constants.UiPaths;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.request.LoginRequest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("UI Smoke")
@Feature("Login")
public class LoginSmokeTest extends BaseUiTest {

    @Test(groups = {"ui-smoke", "smoke", "ui", "login"})
    @Story("Page load")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Login page opens and displays root container")
    public void shouldOpenLoginPage() {
        pages.login().open();

        UiAssertions.waitForPageLoad(page);
        UiAssertions.assertPageUrlContains(page, UiPaths.LOGIN);
        assertThat(pages.login().pageRoot().isVisible()).isTrue();
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "login"})
    @Story("Form fields")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Email and password fields are visible on login page")
    public void shouldDisplayLoginFormFields() {
        pages.login().open();

        assertThat(pages.login().isDisplayed()).isTrue();
        UiAssertions.assertElementEnabled(pages.login().pageRoot());
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "login"})
    @Story("Successful login")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Valid credentials redirect user away from login page")
    public void shouldLoginSuccessfully() {
        LoginRequest credentials = TestDataFactory.defaultLoginRequest();

        pages.login().login(credentials.getEmail(), credentials.getPassword());

        UiAssertions.waitForPageLoad(page);
        page.waitForURL(
                url -> !url.contains(UiPaths.LOGIN),
                new com.microsoft.playwright.Page.WaitForURLOptions().setTimeout(config.uiTimeout()));
        assertThat(page.url()).doesNotContain(UiPaths.LOGIN);
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "login"})
    @Story("Invalid login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Invalid credentials show login error and keep user on login page")
    public void shouldRejectInvalidCredentials() {
        pages.login().open();
        pages.login().enterEmail("invalid@flowiq.test");
        pages.login().enterPassword("wrong-password");
        pages.login().submit();

        UiAssertions.waitUntilVisible(pages.login().errorMessage(), 10);
        assertThat(pages.login().hasError()).isTrue();
        assertThat(page.url()).contains(UiPaths.LOGIN);
    }
}
