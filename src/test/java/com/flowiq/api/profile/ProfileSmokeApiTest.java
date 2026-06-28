package com.flowiq.api.profile;

import com.flowiq.auth.TokenManager;
import com.flowiq.base.BaseSmokeApiTest;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.models.response.ProfileResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Smoke")
@Feature("Profile")
public class ProfileSmokeApiTest extends BaseSmokeApiTest {

    @Test(groups = {"smoke", "api", "profile", "settings"})
    @Story("Happy path")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Authenticated user can read personal profile")
    public void shouldGetPersonalProfile() {
        ProfileResponse profile = profileService.getProfile();

        assertThat(profile.getEmail()).isEqualToIgnoringCase(config.testUserEmail());
    }

    @Test(groups = {"smoke", "api", "profile", "settings"})
    @Story("FOP profile")
    @Severity(SeverityLevel.NORMAL)
    @Description("Authenticated user can read FOP profile")
    public void shouldGetFopProfile() {
        assertThat(profileService.fetchFopProfile().getStatusCode()).isIn(200, 404);
    }

    @Test(groups = {"smoke", "api", "profile", "settings", "security"})
    @Story("Authorization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Profile endpoint rejects unauthenticated access")
    public void shouldRejectUnauthenticatedProfileAccess() {
        TokenManager.clear();
        ApiCallResult<ProfileResponse> result = profileService.fetchProfileUnauthorized();
        assertUnauthorized(result);
    }

    @Test(groups = {"smoke", "api", "settings"})
    @Story("Notification settings")
    @Severity(SeverityLevel.NORMAL)
    @Description("Notification preferences are readable")
    public void shouldGetNotificationPreferences() {
        assertThat(settingsService.fetchNotificationPreferences().getStatusCode()).isEqualTo(200);
    }
}
