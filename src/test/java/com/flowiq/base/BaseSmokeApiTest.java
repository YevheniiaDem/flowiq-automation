package com.flowiq.base;

import com.flowiq.assertions.ApiAssertions;
import com.flowiq.auth.TokenManager;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.models.response.AuthResponse;
import org.testng.annotations.BeforeMethod;

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
