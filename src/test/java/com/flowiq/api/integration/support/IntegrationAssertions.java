package com.flowiq.api.integration.support;

import com.flowiq.assertions.ApiAssertions;
import com.flowiq.clients.ApiCallResult;

import static org.assertj.core.api.Assertions.assertThat;

public final class IntegrationAssertions {

    private IntegrationAssertions() {
    }

    public static void assertSuccess(ApiCallResult<?> result, int expectedStatus) {
        ApiAssertions.assertStatusCode(result, expectedStatus);
        assertThat(result.isSuccessful()).isTrue();
    }

    public static void assertUnauthorized(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCodeOneOf(result, 401, 403);
        assertThat(result.isSuccessful()).isFalse();
    }

    public static void assertValidationError(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCodeOneOf(result, 400, 422);
        assertThat(result.isSuccessful()).isFalse();
    }

    public static void assertNotFound(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCodeOneOf(result, 404);
        assertThat(result.isSuccessful()).isFalse();
    }

    public static void assertForbiddenOrNotFound(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCodeOneOf(result, 403, 404);
        assertThat(result.isSuccessful()).isFalse();
    }
}
