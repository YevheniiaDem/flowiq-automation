package com.flowiq.api.integration.notifications;

import com.flowiq.api.integration.base.BaseApiIntegrationTest;
import com.flowiq.api.integration.support.IntegrationAssertions;
import com.flowiq.auth.TokenManager;
import com.flowiq.models.notifications.NotificationPageResponse;
import com.flowiq.models.notifications.NotificationSummaryResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Integration")
@Feature("Notifications")
public class NotificationsIntegrationTest extends BaseApiIntegrationTest {

    @Test(groups = {"api-integration", "api", "notifications"})
    @Story("Mark read")
    @Severity(SeverityLevel.CRITICAL)
    @Description("PUT /notifications/{id}/read marks a single notification as read")
    public void shouldMarkSingleNotificationAsRead() {
        NotificationPageResponse unreadPage = notificationService.list(
                Map.of("unreadOnly", true, "page", 0, "size", 1));
        if (unreadPage.getContent().isEmpty()) {
            throw new SkipException("No unread notifications available for mark-read test");
        }

        long notificationId = unreadPage.getContent().get(0).getId();
        int unreadBefore = notificationService.getUnreadCount();

        notificationService.markAsRead(notificationId);

        assertThat(notificationService.getUnreadCount()).isLessThanOrEqualTo(unreadBefore);
        NotificationPageResponse after = notificationService.list(
                Map.of("unreadOnly", true, "page", 0, "size", 50));
        assertThat(after.getContent()).noneMatch(n -> notificationId == n.getId());
    }

    @Test(groups = {"api-integration", "api", "notifications"})
    @Story("Mark all read")
    @Severity(SeverityLevel.CRITICAL)
    @Description("PUT /notifications/read-all marks all notifications as read")
    public void shouldMarkAllNotificationsAsRead() {
        int updated = notificationService.markAllAsRead();

        assertThat(updated).isGreaterThanOrEqualTo(0);
        assertThat(notificationService.getUnreadCount()).isZero();
    }

    @Test(groups = {"api-integration", "api", "notifications"})
    @Story("Delete notification")
    @Severity(SeverityLevel.CRITICAL)
    @Description("DELETE /notifications/{id} removes a notification")
    public void shouldDeleteNotification() {
        NotificationPageResponse page = notificationService.list(Map.of("page", 0, "size", 1));
        if (page.getContent().isEmpty()) {
            throw new SkipException("No notifications available for delete test");
        }

        long notificationId = page.getContent().get(0).getId();

        notificationService.deleteById(notificationId);

        IntegrationAssertions.assertNotFound(
                notificationService.attemptMarkAsRead(notificationId));
    }

    @Test(groups = {"api-integration", "api", "notifications"})
    @Story("Unread count")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /notifications/unread-count returns consistent unread count")
    public void shouldReturnUnreadCount() {
        int unreadCount = notificationService.getUnreadCount();
        NotificationPageResponse unreadOnly = notificationService.list(
                Map.of("unreadOnly", true, "page", 0, "size", 50));

        assertThat(unreadCount).isGreaterThanOrEqualTo(0);
        if (unreadCount > 0) {
            assertThat(unreadOnly.getContent()).isNotEmpty();
        }
    }

    @Test(groups = {"api-integration", "api", "notifications"})
    @Story("Summary validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /notifications/summary returns total and unread counters")
    public void shouldReturnValidNotificationSummary() {
        NotificationSummaryResponse summary = notificationService.getSummary();
        NotificationPageResponse page = notificationService.list();

        assertThat(summary.getTotal()).isGreaterThanOrEqualTo(0);
        assertThat(summary.getUnread()).isGreaterThanOrEqualTo(0);
        assertThat(summary.getUnread()).isLessThanOrEqualTo(summary.getTotal());
        assertThat(page.getTotalElements()).isEqualTo(summary.getTotal());
    }

    @Test(groups = {"api-integration", "api", "notifications"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated notification requests are rejected")
    public void shouldRejectUnauthorizedAccess() {
        TokenManager.clear();
        IntegrationAssertions.assertUnauthorized(notificationService.fetchListUnauthorized());
        IntegrationAssertions.assertUnauthorized(notificationService.fetchSummaryUnauthorized());
        IntegrationAssertions.assertUnauthorized(notificationService.fetchUnreadCountUnauthorized());
        loginAsDefaultUser();
    }
}
