package com.flowiq.api.regression.support;

import com.flowiq.assertions.ApiAssertions;
import com.flowiq.clients.ApiCallResult;

import static org.assertj.core.api.Assertions.assertThat;

public final class RegressionAssertions {

    private RegressionAssertions() {
    }

    public static void assertOk(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCode(result, 200);
        assertThat(result.isSuccessful()).isTrue();
    }

    public static void assertCreated(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCode(result, 201);
        assertThat(result.isSuccessful()).isTrue();
    }

    public static void assertNoContent(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCode(result, 204);
    }

    public static void assertUnauthorized(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCodeOneOf(result, 401, 403);
        assertThat(result.isSuccessful()).isFalse();
    }

    public static void assertForbidden(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCodeOneOf(result, 403, 404);
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

    public static void assertConflict(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCodeOneOf(result, 400, 409, 422);
        assertThat(result.isSuccessful()).isFalse();
    }
}
