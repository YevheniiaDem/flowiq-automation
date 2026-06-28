package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.ApiEndpoints;
import io.qameta.allure.Step;

public class SettingsService extends BaseApiService {

    @Step("Get notification preferences")
    public ApiCallResult<Void> fetchNotificationPreferences() {
        return fetch(ApiEndpoints.SETTINGS_NOTIFICATIONS, Void.class);
    }

    @Step("Fetch notification preferences without authentication")
    public ApiCallResult<Void> fetchNotificationPreferencesUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.SETTINGS_NOTIFICATIONS, Void.class);
    }
}
