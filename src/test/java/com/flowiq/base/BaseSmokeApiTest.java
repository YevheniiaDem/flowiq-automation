package com.flowiq.base;

import com.flowiq.assertions.ApiAssertions;
import com.flowiq.auth.TokenManager;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.models.response.AuthResponse;
import org.testng.annotations.BeforeMethod;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseSmokeApiTest extends BaseApiTest {

    @BeforeMethod(alwaysRun = true)
    public void authenticateForSmokeTests() {
        if (!TokenManager.isAuthenticated()) {
            loginAsDefaultUser();
        }
    }

    protected void assertUnauthorized(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCodeOneOf(result, 401, 403);
        assertThat(result.isSuccessful()).isFalse();
    }

    protected void assertValidationError(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCodeOneOf(result, 400, 422);
        assertThat(result.isSuccessful()).isFalse();
    }

    protected void assertMatchesSchema(ApiCallResult<?> result, String schemaPath) {
        ApiAssertions.assertMatchesSchema(result, schemaPath);
    }

    protected void assertHappyPath(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCode(result, 200);
        assertThat(result.isSuccessful()).isTrue();
    }

    protected AuthResponse reLogin() {
        TokenManager.clear();
        return loginAsDefaultUser();
    }
}
