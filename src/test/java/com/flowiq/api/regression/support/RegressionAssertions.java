package com.flowiq.api.regression.support;

import com.flowiq.assertions.ApiAssertions;
import com.flowiq.clients.ApiCallResult;

public final class RegressionAssertions {

    private RegressionAssertions() {
    }

    public static void assertOk(ApiCallResult<?> result) {
        ApiAssertions.assertOk(result);
    }

    public static void assertCreated(ApiCallResult<?> result) {
        ApiAssertions.assertCreated(result);
    }

    public static void assertNoContent(ApiCallResult<?> result) {
        ApiAssertions.assertNoContent(result);
    }

    public static void assertUnauthorized(ApiCallResult<?> result) {
        ApiAssertions.assertUnauthorized(result);
    }

    public static void assertForbidden(ApiCallResult<?> result) {
        ApiAssertions.assertForbidden(result);
    }

    public static void assertValidationError(ApiCallResult<?> result) {
        ApiAssertions.assertValidationError(result);
    }

    public static void assertRejectedWithClientOrServerError(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCodeOneOf(result, 400, 422, 500);
        org.assertj.core.api.Assertions.assertThat(result.isSuccessful()).isFalse();
    }

    public static void assertNotFoundOrForbidden(ApiCallResult<?> result) {
        ApiAssertions.assertStatusCodeOneOf(result, 403, 404, 500);
        org.assertj.core.api.Assertions.assertThat(result.isSuccessful()).isFalse();
    }

    public static void assertNotFound(ApiCallResult<?> result) {
        ApiAssertions.assertNotFound(result);
    }

    public static void assertConflict(ApiCallResult<?> result) {
        ApiAssertions.assertConflict(result);
    }
}
