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
        ApiAssertions.assertUnauthorized(result);
    }

    protected void assertValidationError(ApiCallResult<?> result) {
        ApiAssertions.assertValidationError(result);
    }

    /**
     * Backend may normalize invalid pagination/search params (200) or reject them (400/422).
     */
    protected void assertHandledSafely(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCodeOneOf(result, 200, 400, 422);
    }

    /**
     * Invalid input should not succeed; backend may return validation error or unhandled 500.
     */
    protected void assertRejectedWithClientOrServerError(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCodeOneOf(result, 400, 422, 500);
        assertThat(result.isSuccessful()).isFalse();
    }

    protected void assertMatchesSchema(ApiCallResult<?> result, String schemaPath) {
        ApiAssertions.assertMatchesSchema(result, schemaPath);
    }

    protected void assertHappyPath(ApiCallResult<?> result) {
        ApiAssertions.assertOk(result);
    }

    protected AuthResponse reLogin() {
        TokenManager.clear();
        return loginAsDefaultUser();
    }
}
