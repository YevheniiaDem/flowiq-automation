package com.flowiq.api.notifications;

import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseSmokeApiTest;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.SmokeSchemas;
import com.flowiq.models.notifications.NotificationPageResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Smoke")
@Feature("Notifications")
public class NotificationsSmokeApiTest extends BaseSmokeApiTest {

    @Test(groups = {"smoke", "api", "notifications"})
    @Story("Happy path")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Authenticated user can list notifications")
    public void shouldListNotifications() {
        ApiCallResult<NotificationPageResponse> result = notificationService.fetchList();

        assertHappyPath(result);
        assertThat(result.getBody().getContent()).isNotNull();
    }

    @Test(groups = {"smoke", "api", "notifications"})
    @Story("Authorization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Notifications endpoint requires JWT")
    public void shouldRejectUnauthenticatedAccess() {
        TokenManager.clear();
        assertUnauthorized(notificationService.fetchListUnauthorized());
    }

    @Test(groups = {"smoke", "api", "notifications"})
    @Story("Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Notifications list with invalid page size is rejected")
    public void shouldRejectInvalidPagination() {
        ApiCallResult<NotificationPageResponse> result =
                notificationService.fetchList(java.util.Map.of("page", -1, "size", 0));

        assertValidationError(result);
    }

    @Test(groups = {"smoke", "api", "notifications"})
    @Story("Schema")
    @Severity(SeverityLevel.NORMAL)
    @Description("Notification page response matches JSON schema")
    public void shouldMatchNotificationPageSchema() {
        ApiCallResult<NotificationPageResponse> result = notificationService.fetchList();

        assertHappyPath(result);
        assertMatchesSchema(result, SmokeSchemas.NOTIFICATION_PAGE);
    }
}
