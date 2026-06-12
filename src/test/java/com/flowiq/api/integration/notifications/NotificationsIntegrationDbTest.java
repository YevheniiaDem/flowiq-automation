package com.flowiq.api.integration.notifications;

import com.flowiq.api.integration.base.BaseApiIntegrationDbTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Integration")
@Feature("Notifications DB Consistency")
public class NotificationsIntegrationDbTest extends BaseApiIntegrationDbTest {

    @Test(groups = {"api-integration", "api-integration-db", "notifications"})
    @Story("Data consistency")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Mark all read via API updates notification rows in Testcontainer database")
    public void shouldSyncMarkAllReadWithDatabase() {
        notificationDb.insert(
                seededUser().getId(),
                "Integration unread",
                "DB consistency check",
                "SYSTEM",
                "INFO",
                "/notifications",
                "api-integration-" + seededUser().getId(),
                false
        );

        assertThat(notificationDb.countUnreadByUserId(seededUser().getId())).isEqualTo(1);

        notificationService.markAllAsRead();

        assertThat(notificationDb.countUnreadByUserId(seededUser().getId())).isZero();
    }
}
