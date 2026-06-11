package com.flowiq.e2e;

import com.flowiq.base.BaseE2ETest;
import com.flowiq.base.UiAssertions;
import com.flowiq.models.notifications.NotificationPageResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("E2E")
@Feature("Read Notification")
public class ReadNotificationE2ETest extends BaseE2ETest {

    @Test(groups = {"e2e", "notifications"})
    @Story("Read notifications in UI")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User opens notifications page and can read notification list")
    public void shouldOpenNotificationsAndReadList() {
        NotificationPageResponse apiPage = notificationService.list();
        pages.notifications().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.notifications().isLoaded()).isTrue();
        assertThat(pages.notifications().list().isVisible()).isTrue();

        if (!apiPage.getContent().isEmpty()) {
            assertThat(pages.notifications().isEmpty()).isFalse();
        }
    }
}
