package com.flowiq.e2e;

import com.flowiq.base.BaseUiTest;
import com.flowiq.base.UiAssertions;
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
@Feature("Registration")
public class RegisterOnboardingE2ETest extends BaseUiTest {

    @Test(groups = {"e2e", "registration", "onboarding"})
    @Story("Registration to dashboard")
    @Severity(SeverityLevel.BLOCKER)
    @Description("User registers via UI and lands on dashboard")
    public void shouldRegisterAndReachDashboard() {
        var request = TestDataFactory.randomRegisterRequest();

        pages.register().open()
                .enterName(request.getName())
                .enterEmail(request.getEmail())
                .enterPassword(request.getPassword())
                .submit();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.dashboard().isLoaded()).isTrue();
    }
}
