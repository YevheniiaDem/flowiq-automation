package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.clients.BaseResponseSpecification;
import com.flowiq.constants.ApiEndpoints;
import com.flowiq.models.notifications.MarkNotificationReadRequest;
import com.flowiq.models.notifications.NotificationPageResponse;
import com.flowiq.models.notifications.NotificationSummaryResponse;
import io.qameta.allure.Step;

import java.util.Map;

public class NotificationService extends BaseApiService {

    @Step("List notifications")
    public NotificationPageResponse list(Map<String, ?> queryParams) {
        return getOk(ApiEndpoints.NOTIFICATIONS, queryParams, NotificationPageResponse.class);
    }

    @Step("List notifications (default pagination)")
    public NotificationPageResponse list() {
        return list(Map.of("page", 0, "size", 20));
    }

    @Step("Get unread notifications count")
    public int getUnreadCount() {
        return get(ApiEndpoints.NOTIFICATIONS_UNREAD_COUNT).getRaw().jsonPath().getInt("count");
    }

    @Step("Get notifications summary")
    public NotificationSummaryResponse getSummary() {
        return getOk(ApiEndpoints.NOTIFICATIONS_SUMMARY, NotificationSummaryResponse.class);
    }

    @Step("Mark notification {id} as read")
    public void markAsRead(long id) {
        BaseResponseSpecification.validate(
                put(ApiEndpoints.NOTIFICATION_READ.replace("{id}", String.valueOf(id)),
                        new MarkNotificationReadRequest()),
                200);
    }

    @Step("Mark all notifications as read")
    public int markAllAsRead() {
        return put(ApiEndpoints.NOTIFICATIONS_READ_ALL).getRaw().jsonPath().getInt("updated");
    }

    @Step("Delete notification {id}")
    public void deleteById(long id) {
        deleteNoContent(ApiEndpoints.NOTIFICATION_BY_ID.replace("{id}", String.valueOf(id)));
    }

    @Step("Fetch notifications (unchecked)")
    public ApiCallResult<NotificationPageResponse> fetchList(Map<String, ?> queryParams) {
        return fetch(ApiEndpoints.NOTIFICATIONS, queryParams, NotificationPageResponse.class);
    }

    @Step("Fetch notifications (unchecked, default pagination)")
    public ApiCallResult<NotificationPageResponse> fetchList() {
        return fetchList(Map.of("page", 0, "size", 20));
    }

    @Step("Fetch notifications without authentication")
    public ApiCallResult<NotificationPageResponse> fetchListUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.NOTIFICATIONS, Map.of("page", 0, "size", 20), NotificationPageResponse.class);
    }
}
