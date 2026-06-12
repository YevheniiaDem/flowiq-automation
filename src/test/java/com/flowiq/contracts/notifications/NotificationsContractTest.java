package com.flowiq.contracts.notifications;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.contracts.ContractAssertions;
import com.flowiq.contracts.ContractSchemas;
import com.flowiq.contracts.base.BaseContractTest;
import com.flowiq.models.notifications.NotificationPageResponse;
import com.flowiq.models.notifications.NotificationSeverity;
import com.flowiq.models.notifications.NotificationSummaryResponse;
import com.flowiq.models.notifications.NotificationType;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("Contract Testing")
@Feature("Notifications")
public class NotificationsContractTest extends BaseContractTest {

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    @Test(groups = {"contract", "notifications"})
    @Story("GET /api/notifications")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Notifications page response matches contract schema")
    public void notificationsListShouldMatchContract() {
        ApiCallResult<NotificationPageResponse> result = notificationService.fetchList();

        ContractAssertions.assertAllRequired(result, 200, ContractSchemas.NOTIFICATIONS_PAGE,
                "content", "page", "size", "totalElements", "totalPages");

        if (result.getResponse() != null) {
            ContractAssertions.assertEnumValuesInNestedArray(
                    result.getResponse(), "content", "type", NotificationType.class);
            ContractAssertions.assertEnumValuesInNestedArray(
                    result.getResponse(), "content", "severity", NotificationSeverity.class);
        }
    }

    @Test(groups = {"contract", "notifications"})
    @Story("GET /api/notifications/summary")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Notifications summary response matches contract schema")
    public void notificationsSummaryShouldMatchContract() {
        ApiCallResult<NotificationSummaryResponse> result = notificationService.fetchSummary();

        ContractAssertions.assertAllRequired(result, 200, ContractSchemas.NOTIFICATIONS_SUMMARY,
                "total", "unread", "critical", "warnings", "success", "thisMonth");
    }
}
