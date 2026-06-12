package com.flowiq.api.regression.notifications;

import com.flowiq.api.regression.base.BaseRegressionApiTest;
import com.flowiq.api.regression.support.RegressionAssertions;
import com.flowiq.api.regression.support.RegressionDataProviders;
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

@Epic("API Regression")
@Feature("Notifications")
public class NotificationsRegressionTest extends BaseRegressionApiTest {

    @Test(groups = {"api-regression", "regression", "api", "notifications"})
    @Story("Notifications list")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /notifications returns paginated notification list")
    public void shouldListNotifications() {
        NotificationPageResponse page = notificationService.list();

        assertThat(page.getContent()).isNotNull();
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {"api-regression", "regression", "api", "notifications"},
            dataProvider = "paginationPages", dataProviderClass = RegressionDataProviders.class)
    @Story("Pagination")
    @Severity(SeverityLevel.NORMAL)
    @Description("Notifications list supports page parameter")
    public void shouldPaginateNotificationsByPage(int page) {
        NotificationPageResponse response = notificationService.list(Map.of("page", page, "size", 10));

        assertThat(response.getPage()).isEqualTo(page);
        assertThat(response.getContent()).isNotNull();
    }

    @Test(groups = {"api-regression", "regression", "api", "notifications"},
            dataProvider = "pageSizes", dataProviderClass = RegressionDataProviders.class)
    @Story("Pagination")
    @Severity(SeverityLevel.NORMAL)
    @Description("Notifications list supports size parameter")
    public void shouldPaginateNotificationsByPageSize(int size) {
        NotificationPageResponse response = notificationService.list(Map.of("page", 0, "size", size));

        assertThat(response.getSize()).isEqualTo(size);
    }

    @Test(groups = {"api-regression", "regression", "api", "notifications"},
            dataProvider = "notificationUnreadFilters", dataProviderClass = RegressionDataProviders.class)
    @Story("Unread filters")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Notifications list supports unreadOnly filter")
    public void shouldFilterNotificationsByUnreadFlag(boolean unreadOnly) {
        NotificationPageResponse page = notificationService.list(
                Map.of("unreadOnly", unreadOnly, "page", 0, "size", 50));

        assertThat(page.getContent()).isNotNull();
        if (unreadOnly && !page.getContent().isEmpty()) {
            assertThat(page.getContent()).allMatch(notification -> !notification.isRead());
        }
    }

    @Test(groups = {"api-regression", "regression", "api", "notifications"})
    @Story("Summary")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /notifications/summary returns total and unread counters")
    public void shouldReturnNotificationSummary() {
        NotificationSummaryResponse summary = notificationService.getSummary();

        assertThat(summary.getTotal()).isGreaterThanOrEqualTo(0);
        assertThat(summary.getUnread()).isGreaterThanOrEqualTo(0);
        assertThat(summary.getUnread()).isLessThanOrEqualTo(summary.getTotal());
    }

    @Test(groups = {"api-regression", "regression", "api", "notifications"})
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

    @Test(groups = {"api-regression", "regression", "api", "notifications"})
    @Story("Mark all read")
    @Severity(SeverityLevel.CRITICAL)
    @Description("PUT /notifications/read-all marks all notifications as read")
    public void shouldMarkAllNotificationsAsRead() {
        int updated = notificationService.markAllAsRead();

        assertThat(updated).isGreaterThanOrEqualTo(0);
        assertThat(notificationService.getUnreadCount()).isZero();
    }

    @Test(groups = {"api-regression", "regression", "api", "notifications"})
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

    @Test(groups = {"api-regression", "regression", "api", "notifications"})
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

        RegressionAssertions.assertNotFound(notificationService.attemptMarkAsRead(notificationId));
    }

    @Test(groups = {"api-regression", "regression", "api", "notifications"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated notification list request is rejected")
    public void shouldRejectUnauthorizedListAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(notificationService.fetchListUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "notifications"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated notification summary request is rejected")
    public void shouldRejectUnauthorizedSummaryAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(notificationService.fetchSummaryUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "notifications"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated unread count request is rejected")
    public void shouldRejectUnauthorizedUnreadCountAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(notificationService.fetchUnreadCountUnauthorized());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "notifications"})
    @Story("Unauthorized access")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Unauthenticated mark-all-read request is rejected")
    public void shouldRejectUnauthorizedMarkAllReadAccess() {
        TokenManager.clear();
        RegressionAssertions.assertUnauthorized(notificationService.attemptMarkAllAsRead());
        loginAsDefaultUser();
    }

    @Test(groups = {"api-regression", "regression", "api", "notifications"})
    @Story("Not found")
    @Severity(SeverityLevel.NORMAL)
    @Description("Mark read on non-existent notification returns 404")
    public void shouldReturnNotFoundForInvalidIdMarkRead() {
        RegressionAssertions.assertNotFound(notificationService.attemptMarkAsRead(INVALID_ID));
    }

    @Test(groups = {"api-regression", "regression", "api", "notifications"})
    @Story("Notification read state business rule")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Marked-as-read notification no longer appears in unread-only list")
    public void shouldEnforceNotificationReadStateBusinessRule() {
        NotificationPageResponse unreadPage = notificationService.list(
                Map.of("unreadOnly", true, "page", 0, "size", 1));
        if (unreadPage.getContent().isEmpty()) {
            throw new SkipException("No unread notifications available for read-state test");
        }

        long notificationId = unreadPage.getContent().get(0).getId();
        assertThat(unreadPage.getContent().get(0).isRead()).isFalse();

        notificationService.markAsRead(notificationId);

        NotificationPageResponse afterMark = notificationService.list(
                Map.of("unreadOnly", true, "page", 0, "size", 50));
        assertThat(afterMark.getContent()).noneMatch(n -> notificationId == n.getId());
    }

    @Test(groups = {"api-regression", "regression", "api", "notifications"})
    @Story("Summary validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Notification summary total matches list totalElements")
    public void shouldKeepSummaryConsistentWithList() {
        NotificationSummaryResponse summary = notificationService.getSummary();
        NotificationPageResponse page = notificationService.list();

        assertThat(page.getTotalElements()).isEqualTo(summary.getTotal());
    }

    @Test(groups = {"api-regression", "regression", "api", "notifications"})
    @Story("Not found")
    @Severity(SeverityLevel.NORMAL)
    @Description("Deleting non-existent notification returns 404")
    public void shouldRejectDeleteForInvalidId() {
        RegressionAssertions.assertNotFound(notificationService.attemptDeleteById(INVALID_ID));
    }

    @Test(groups = {"api-regression", "regression", "api", "notifications"})
    @Story("Notifications list")
    @Severity(SeverityLevel.NORMAL)
    @Description("Notification list fetch returns successful response")
    public void shouldFetchNotificationsSuccessfully() {
        RegressionAssertions.assertOk(notificationService.fetchList());
    }
}
