package com.flowiq.e2e;

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

@Epic("E2E")
@Feature("User Login")
public class UserLoginE2ETest extends BaseUiTest {

    @Test(groups = {"e2e", "login"})
    @Story("Login to dashboard")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User logs in via UI and reaches authenticated dashboard")
    public void shouldLoginViaUiAndReachDashboard() {
        var credentials = TestDataFactory.defaultLoginRequest();
        pages.login().login(credentials.getEmail(), credentials.getPassword());

        UiAssertions.waitForPageLoad(page);
        page.waitForURL(url -> !url.contains(UiPaths.LOGIN));
        pages.dashboard().waitForPageLoaded();

        assertThat(pages.dashboard().isLoaded()).isTrue();
    }
}
