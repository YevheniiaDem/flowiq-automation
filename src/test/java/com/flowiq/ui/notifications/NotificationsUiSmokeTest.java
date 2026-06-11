package com.flowiq.ui.notifications;

import com.flowiq.base.AuthenticatedUiTest;
import com.flowiq.base.UiAssertions;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("UI Smoke")
@Feature("Notifications")
public class NotificationsUiSmokeTest extends AuthenticatedUiTest {

    @Test(groups = {"smoke", "ui", "notifications"})
    @Story("Page load")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Notifications page displays filters and notification list")
    public void shouldDisplayNotificationsPage() {
        pages.notifications().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.notifications().isLoaded()).isTrue();
        UiAssertions.assertElementVisible(pages.notifications().filters());
        UiAssertions.assertElementVisible(pages.notifications().list());
    }
}
