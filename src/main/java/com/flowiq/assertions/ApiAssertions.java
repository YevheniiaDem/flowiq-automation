package com.flowiq.assertions;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.clients.ApiResponse;
import com.flowiq.clients.BaseResponseSpecification;
import com.flowiq.validation.JsonSchemaValidator;
import org.assertj.core.api.Assertions;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.awaitility.Awaitility.await;

public final class ApiAssertions {

    private ApiAssertions() {
    }

    public static void assertStatusCode(ApiResponse response, int expectedStatus) {
        Assertions.assertThat(response.getStatusCode())
                .as("HTTP status code")
                .isEqualTo(expectedStatus);
    }

    public static void assertStatusCode(ApiCallResult<?> result, int expectedStatus) {
        Assertions.assertThat(result.getStatusCode())
                .as("HTTP status code")
                .isEqualTo(expectedStatus);
    }

    public static void assertStatusCodeOneOf(ApiResponse response, int... expectedStatuses) {
        BaseResponseSpecification.validateAnyOf(response, expectedStatuses);
    }

    public static void assertStatusCodeOneOf(ApiCallResult<?> result, int... expectedStatuses) {
        if (result.getResponse() != null) {
            BaseResponseSpecification.validateAnyOf(result.getResponse(), expectedStatuses);
        } else {
            Assertions.assertThat(result.getStatusCode())
                    .as("HTTP status code")
                    .isIn(java.util.Arrays.stream(expectedStatuses).boxed().toArray(Integer[]::new));
        }
    }

    public static void assertJsonFieldEquals(ApiResponse response, String jsonPath, Object expectedValue) {
        Object actual = response.getRaw().jsonPath().get(jsonPath);
        Assertions.assertThat(actual)
                .as("JSON field '%s'", jsonPath)
                .isEqualTo(expectedValue);
    }

    public static void assertJsonFieldEquals(ApiCallResult<?> result, String jsonPath, Object expectedValue) {
        if (result.getResponse() == null) {
            throw new IllegalArgumentException("ApiCallResult has no underlying response");
        }
        assertJsonFieldEquals(result.getResponse(), jsonPath, expectedValue);
    }

    public static void assertJsonFieldNotNull(ApiResponse response, String jsonPath) {
        Object actual = response.getRaw().jsonPath().get(jsonPath);
        Assertions.assertThat(actual)
                .as("JSON field '%s'", jsonPath)
                .isNotNull();
    }

    public static void assertResponseTimeLessThan(ApiResponse response, long maxMillis) {
        Assertions.assertThat(response.getTime())
                .as("Response time (ms)")
                .isLessThan(maxMillis);
    }

    public static void assertResponseTimeLessThan(ApiCallResult<?> result, long maxMillis) {
        Assertions.assertThat(result.getResponseTimeMs())
                .as("Response time (ms)")
                .isLessThan(maxMillis);
    }

    public static void assertMatchesSchema(ApiResponse response, String schemaPath) {
        JsonSchemaValidator.validate(response, schemaPath);
    }

    public static void assertMatchesSchema(ApiCallResult<?> result, String schemaPath) {
        JsonSchemaValidator.validate(result, schemaPath);
    }

    public static void assertBodyNotNull(ApiCallResult<?> result) {
        Assertions.assertThat(result.getBody())
                .as("Response body")
                .isNotNull();
    }

    public static ApiResponse awaitStatusCode(Supplier<ApiResponse> requestSupplier,
                                            int expectedStatus,
                                            long timeoutSeconds) {
        return await()
                .atMost(timeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(requestSupplier::get, response -> response.getStatusCode() == expectedStatus);
    }
}
