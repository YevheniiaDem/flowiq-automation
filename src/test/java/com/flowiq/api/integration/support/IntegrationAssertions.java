package com.flowiq.api.integration.support;

import com.flowiq.assertions.ApiAssertions;
import com.flowiq.clients.ApiCallResult;

public final class IntegrationAssertions {

    private IntegrationAssertions() {
    }

    public static void assertSuccess(ApiCallResult<?> result, int expectedStatus) {
        ApiAssertions.assertStatusCode(result, expectedStatus);
        org.assertj.core.api.Assertions.assertThat(result.isSuccessful()).isTrue();
    }

    public static void assertUnauthorized(ApiCallResult<?> result) {
        ApiAssertions.assertUnauthorized(result);
    }

    public static void assertValidationError(ApiCallResult<?> result) {
        ApiAssertions.assertValidationError(result);
    }

    public static void assertNotFound(ApiCallResult<?> result) {
        ApiAssertions.assertNotFound(result);
    }

    public static void assertForbiddenOrNotFound(ApiCallResult<?> result) {
        ApiAssertions.assertForbidden(result);
    }
}
