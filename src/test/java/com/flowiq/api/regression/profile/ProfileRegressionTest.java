package com.flowiq.api.regression.profile;

import com.flowiq.api.regression.base.BaseRegressionApiTest;
import com.flowiq.factories.TestDataFactory;
import com.flowiq.models.request.UpdateProfileRequest;
import com.flowiq.models.response.ProfileResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Regression")
@Feature("Profile")
public class ProfileRegressionTest extends BaseRegressionApiTest {

    @Test(groups = {"api-regression", "regression", "api", "profile", "settings"})
    @Story("Personal profile")
    @Severity(SeverityLevel.CRITICAL)
    @Description("GET /profile returns authenticated user profile")
    public void shouldReturnPersonalProfile() {
        ProfileResponse profile = profileService.getProfile();

        assertThat(profile.getEmail()).isEqualToIgnoringCase(config.testUserEmail());
    }

    @Test(groups = {"api-regression", "regression", "api", "profile", "settings"})
    @Story("Personal profile")
    @Severity(SeverityLevel.CRITICAL)
    @Description("PUT /profile updates personal profile fields")
    public void shouldUpdatePersonalProfile() {
        ProfileResponse updated = profileService.updateProfile(UpdateProfileRequest.builder()
                .firstName("Flow")
                .lastName("IQ")
                .company("FlowIQ QA")
                .build());

        assertThat(updated.getFirstName()).isEqualTo("Flow");
        assertThat(updated.getLastName()).isEqualTo("IQ");
    }

    @Test(groups = {"api-regression", "regression", "api", "profile", "settings", "security"})
    @Story("Sessions")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /profile/sessions returns active sessions")
    public void shouldListActiveSessions() {
        assertThat(profileService.getSessions()).isNotEmpty();
    }

    @Test(groups = {"api-regression", "regression", "api", "settings"})
    @Story("Notification settings")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /settings/notifications returns preferences")
    public void shouldReturnNotificationPreferences() {
        assertThat(settingsService.fetchNotificationPreferences().getStatusCode()).isEqualTo(200);
    }

    @Test(groups = {"api-regression", "regression", "api", "auth", "registration"})
    @Story("Registration flow")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Registered user profile is accessible via /auth/me and /profile")
    public void shouldAccessProfileAfterRegistration() {
        authService.register(TestDataFactory.randomRegisterRequest());

        assertThat(authService.getCurrentUser().getEmail()).isNotBlank();
        assertThat(profileService.getProfile().getEmail()).isNotBlank();
        authService.logout();
    }
}
