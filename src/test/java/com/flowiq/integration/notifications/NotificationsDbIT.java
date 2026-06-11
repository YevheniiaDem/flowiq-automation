package com.flowiq.integration.notifications;

import com.flowiq.base.BaseDbTest;
import com.flowiq.db.seeder.TestDataSeeder;
import com.flowiq.utils.RandomDataGenerator;
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
public class NotificationsDbIT extends BaseDbTest {

    @Test(groups = {"integration", "db", "notifications"})
    @Story("Notification read state")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Notification is persisted and marked as read for isolated user")
    public void shouldPersistAndMarkNotificationAsRead() {
        String dedupKey = "notif-" + RandomDataGenerator.uuid();

        long notificationId = notificationDb.insert(
                seededUser().getId(),
                "Tax reminder",
                "Submit quarterly declaration",
                "TAX",
                "WARNING",
                "/tasks",
                dedupKey,
                false
        );

        assertThat(notificationDb.countUnreadByUserId(seededUser().getId())).isEqualTo(1);

        notificationDb.markAsRead(notificationId);
        var row = notificationDb.findById(notificationId).orElseThrow();

        assertThat(row.userId()).isEqualTo(seededUser().getId());
        assertThat(row.read()).isTrue();
        assertThat(notificationDb.countUnreadByUserId(seededUser().getId())).isZero();
    }

    @Test(groups = {"integration", "db", "notifications"})
    @Story("User isolation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Notifications are isolated between users")
    public void shouldIsolateNotificationsByUser() {
        var otherUser = TestDataSeeder.seedUser(dataSource);

        notificationDb.insert(seededUser().getId(), "A", "msg-a", "SYSTEM", "INFO", null, "a-" + RandomDataGenerator.uuid(), false);
        notificationDb.insert(otherUser.getId(), "B", "msg-b", "FINANCIAL", "SUCCESS", null, "b-" + RandomDataGenerator.uuid(), true);

        assertThat(notificationDb.countUnreadByUserId(seededUser().getId())).isEqualTo(1);
        assertThat(notificationDb.countUnreadByUserId(otherUser.getId())).isZero();
    }
}
