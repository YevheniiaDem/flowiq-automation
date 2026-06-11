package com.flowiq.api.notifications;

import com.flowiq.base.BaseRegressionApiTest;
import com.flowiq.models.notifications.NotificationPageResponse;
import com.flowiq.models.notifications.NotificationSummaryResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Regression")
@Feature("Notifications")
public class NotificationsRegressionApiTest extends BaseRegressionApiTest {

    @Test(groups = {"regression", "api", "notifications"})
    @Story("Notifications list")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Notifications list and summary are available for authenticated user")
    public void shouldListNotificationsAndSummary() {
        NotificationPageResponse page = notificationService.list();
        NotificationSummaryResponse summary = notificationService.getSummary();

        assertThat(page.getContent()).isNotNull();
        assertThat(summary.getTotal()).isGreaterThanOrEqualTo(0);
        assertThat(summary.getUnread()).isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {"regression", "api", "notifications"})
    @Story("Unread count")
    @Severity(SeverityLevel.NORMAL)
    @Description("Unread count is consistent with unread filter")
    public void shouldReturnUnreadCount() {
        int unreadCount = notificationService.getUnreadCount();
        NotificationPageResponse unreadOnly = notificationService.list(Map.of("unreadOnly", true, "page", 0, "size", 50));

        assertThat(unreadCount).isGreaterThanOrEqualTo(0);
        if (unreadCount > 0) {
            assertThat(unreadOnly.getContent()).isNotEmpty();
        }
    }

    @Test(groups = {"regression", "api", "notifications"})
    @Story("Mark as read")
    @Severity(SeverityLevel.CRITICAL)
    @Description("User can mark all notifications as read")
    public void shouldMarkAllNotificationsAsRead() {
        int updated = notificationService.markAllAsRead();
        assertThat(updated).isGreaterThanOrEqualTo(0);
        assertThat(notificationService.getUnreadCount()).isEqualTo(0);
    }
}
