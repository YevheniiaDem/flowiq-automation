package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.constants.ApiEndpoints;
import com.flowiq.models.request.UpdateProfileRequest;
import com.flowiq.models.response.FopProfileResponse;
import com.flowiq.models.response.ProfileResponse;
import com.flowiq.models.response.SessionResponse;
import io.qameta.allure.Step;

import java.util.List;

public class ProfileService extends BaseApiService {

    @Step("Get personal profile")
    public ProfileResponse getProfile() {
        return getOk(ApiEndpoints.PROFILE, ProfileResponse.class);
    }

    @Step("Update personal profile")
    public ProfileResponse updateProfile(UpdateProfileRequest request) {
        return putOk(ApiEndpoints.PROFILE, request, ProfileResponse.class);
    }

    @Step("Get FOP profile")
    public FopProfileResponse getFopProfile() {
        return getOk(ApiEndpoints.PROFILE_FOP, FopProfileResponse.class);
    }

    @Step("Get active sessions")
    public List<SessionResponse> getSessions() {
        return get(ApiEndpoints.PROFILE_SESSIONS).getRaw().jsonPath().getList("", SessionResponse.class);
    }

    @Step("Fetch profile without authentication")
    public ApiCallResult<ProfileResponse> fetchProfileUnauthorized() {
        return fetchUnauthenticated(ApiEndpoints.PROFILE, ProfileResponse.class);
    }

    @Step("Fetch profile (unchecked)")
    public ApiCallResult<ProfileResponse> fetchProfile() {
        return fetch(ApiEndpoints.PROFILE, ProfileResponse.class);
    }

    @Step("Fetch FOP profile (unchecked)")
    public ApiCallResult<FopProfileResponse> fetchFopProfile() {
        return fetch(ApiEndpoints.PROFILE_FOP, FopProfileResponse.class);
    }

    @Step("Fetch sessions (unchecked)")
    public ApiCallResult<Void> fetchSessions() {
        return fetch(ApiEndpoints.PROFILE_SESSIONS, Void.class);
    }
}
