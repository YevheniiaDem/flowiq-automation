package com.flowiq.ui.smoke;

import com.flowiq.base.BaseUiTest;
import com.flowiq.base.UiAssertions;
import com.flowiq.constants.UiPaths;
import com.flowiq.factories.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("UI Smoke")
@Feature("Registration")
public class RegisterSmokeTest extends BaseUiTest {

    @Test(groups = {"ui-smoke", "smoke", "ui", "registration"})
    @Story("Page load")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Registration page opens with required fields")
    public void shouldOpenRegisterPage() {
        pages.register().open();

        UiAssertions.assertPageUrlContains(page, UiPaths.REGISTER);
        assertThat(pages.register().isDisplayed()).isTrue();
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "registration"})
    @Story("Successful registration")
    @Severity(SeverityLevel.BLOCKER)
    @Description("New user can register and leave login/register routes")
    public void shouldRegisterNewUser() {
        var request = TestDataFactory.randomRegisterRequest();

        pages.register().open()
                .enterName(request.getName())
                .enterEmail(request.getEmail())
                .enterPassword(request.getPassword())
                .submit();

        UiAssertions.waitForPageLoad(page);
        page.waitForURL(url -> !url.contains(UiPaths.REGISTER) && !url.contains(UiPaths.LOGIN),
                new com.microsoft.playwright.Page.WaitForURLOptions().setTimeout(config.uiTimeout()));
        assertThat(page.url()).doesNotContain(UiPaths.REGISTER);
    }
}
