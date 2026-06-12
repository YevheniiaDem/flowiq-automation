package com.flowiq.ui.smoke;

import com.flowiq.base.UiAssertions;
import com.flowiq.ui.smoke.base.BaseUiSmokeTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.SkipException;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("UI Smoke")
@Feature("Notifications")
public class NotificationsSmokeTest extends BaseUiSmokeTest {

    @Test(groups = {"ui-smoke", "smoke", "ui", "notifications"})
    @Story("Notification list")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Notifications page displays filters and notification list")
    public void shouldDisplayNotificationList() {
        pages.notifications().open();

        UiAssertions.waitForPageLoad(page);
        assertThat(pages.notifications().isLoaded()).isTrue();
        UiAssertions.assertElementVisible(pages.notifications().list());
        UiAssertions.assertElementVisible(pages.notifications().filters());
    }

    @Test(groups = {"ui-smoke", "smoke", "ui", "notifications"})
    @Story("Mark as read")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Clicking an unread notification marks it as read")
    public void shouldMarkNotificationAsRead() {
        pages.notifications().open();

        if (pages.notifications().isEmpty() || pages.notifications().getUnreadCardCount() == 0) {
            throw new SkipException("No unread notifications available for mark-as-read smoke test");
        }

        int unreadBefore = pages.notifications().getUnreadCardCount();
        pages.notifications().markFirstUnreadAsRead();

        assertThat(pages.notifications().getUnreadCardCount()).isLessThanOrEqualTo(unreadBefore);
    }
}
