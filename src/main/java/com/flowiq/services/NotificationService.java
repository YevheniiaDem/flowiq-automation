package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.clients.BaseResponseSpecification;
import com.flowiq.constants.ApiEndpoints;
import com.flowiq.constants.TestConstants;
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
        return list(TestConstants.pagination(TestConstants.DEFAULT_PAGE_SIZE));
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
                put(ApiEndpoints.withPathParam(ApiEndpoints.NOTIFICATION_READ, "id", id),
                        new MarkNotificationReadRequest()),
                200);
    }

    @Step("Mark all notifications as read")
    public int markAllAsRead() {
        return put(ApiEndpoints.NOTIFICATIONS_READ_ALL).getRaw().jsonPath().getInt("updated");
    }

    @Step("Attempt mark all notifications as read (unchecked)")
    public ApiCallResult<Void> attemptMarkAllAsRead() {
        return attemptPut(ApiEndpoints.NOTIFICATIONS_READ_ALL, Void.class);
    }

    @Step("Delete notification {id}")
    public void deleteById(long id) {
        deleteNoContent(ApiEndpoints.withPathParam(ApiEndpoints.NOTIFICATION_BY_ID, "id", id));
    }

    @Step("Fetch notifications (unchecked)")
    public ApiCallResult<NotificationPageResponse> fetchList(Map<String, ?> queryParams) {
        return fetch(ApiEndpoints.NOTIFICATIONS, queryParams, NotificationPageResponse.class);
    }

    @Step("Fetch notifications (unchecked, default pagination)")
    public ApiCallResult<NotificationPageResponse> fetchList() {
        return fetchList(TestConstants.pagination(TestConstants.DEFAULT_PAGE_SIZE));
    }

    @Step("Fetch notifications without authentication")
    public ApiCallResult<NotificationPageResponse> fetchListUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.NOTIFICATIONS,
                TestConstants.pagination(TestConstants.DEFAULT_PAGE_SIZE),
                NotificationPageResponse.class);
    }

    @Step("Fetch notifications summary (unchecked)")
    public ApiCallResult<NotificationSummaryResponse> fetchSummary() {
        return fetch(ApiEndpoints.NOTIFICATIONS_SUMMARY, NotificationSummaryResponse.class);
    }

    @Step("Fetch notifications summary without authentication")
    public ApiCallResult<NotificationSummaryResponse> fetchSummaryUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.NOTIFICATIONS_SUMMARY, NotificationSummaryResponse.class);
    }

    @Step("Fetch unread count without authentication")
    public ApiCallResult<Void> fetchUnreadCountUnauthorized() {
        return ApiCallResult.from(getUnauthenticated(ApiEndpoints.NOTIFICATIONS_UNREAD_COUNT));
    }

    @Step("Attempt mark notification {id} as read")
    public ApiCallResult<Void> attemptMarkAsRead(long id) {
        return attemptPut(ApiEndpoints.withPathParam(ApiEndpoints.NOTIFICATION_READ, "id", id),
                new MarkNotificationReadRequest(), Void.class);
    }

    @Step("Attempt delete notification {id}")
    public ApiCallResult<Void> attemptDeleteById(long id) {
        return super.attemptDelete(ApiEndpoints.withPathParam(ApiEndpoints.NOTIFICATION_BY_ID, "id", id));
    }
}
