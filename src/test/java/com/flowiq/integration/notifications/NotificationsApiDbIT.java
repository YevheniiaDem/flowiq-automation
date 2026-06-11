package com.flowiq.integration.notifications;

import com.flowiq.base.BaseApiDbIT;
import com.flowiq.models.notifications.NotificationPageResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Database Integration")
@Feature("Notifications")
public class NotificationsApiDbIT extends BaseApiDbIT {

    @Test(groups = {"integration", "api-db", "notifications"})
    @Story("API to database sync")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Mark all read via API updates notification rows in Testcontainer database")
    public void shouldMarkAllNotificationsReadInDatabase() {
        notificationDb.insert(
                seededUser().getId(),
                "Unread",
                "Pending action",
                "SYSTEM",
                "INFO",
                "/notifications",
                "api-db-" + seededUser().getId(),
                false
        );

        assertThat(notificationDb.countUnreadByUserId(seededUser().getId())).isEqualTo(1);

        notificationService.markAllAsRead();
        NotificationPageResponse page = notificationService.list();

        assertThat(notificationDb.countUnreadByUserId(seededUser().getId())).isZero();
        assertThat(page.getContent()).isNotNull();
    }
}
