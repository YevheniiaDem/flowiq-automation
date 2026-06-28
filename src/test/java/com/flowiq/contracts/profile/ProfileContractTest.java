package com.flowiq.contracts.profile;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.contracts.ContractAssertions;
import com.flowiq.contracts.ContractSchemas;
import com.flowiq.contracts.base.BaseContractTest;
import com.flowiq.models.response.ProfileResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic("Contract Testing")
@Feature("Profile")
public class ProfileContractTest extends BaseContractTest {

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    @Test(groups = {"contract", "profile", "settings"})
    @Story("GET /api/profile")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Profile response matches contract schema")
    public void profileShouldMatchContract() {
        ApiCallResult<ProfileResponse> result = profileService.fetchProfile();

        ContractAssertions.assertAllRequired(result, 200, ContractSchemas.PROFILE, "email");
    }
}
