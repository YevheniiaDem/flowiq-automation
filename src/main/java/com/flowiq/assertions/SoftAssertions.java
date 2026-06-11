package com.flowiq.assertions;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.clients.ApiResponse;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.ObjectAssert;

public class SoftAssertions {

    private final org.assertj.core.api.SoftAssertions softly = new org.assertj.core.api.SoftAssertions();

    public AbstractStringAssert<?> assertThat(String actual) {
        return softly.assertThat(actual);
    }

    public <T> ObjectAssert<T> assertThat(T actual) {
        return softly.assertThat(actual);
    }

    public void assertStatusCode(ApiResponse response, int expectedStatus) {
        softly.assertThat(response.getStatusCode())
                .as("HTTP status code")
                .isEqualTo(expectedStatus);
    }

    public void assertStatusCode(ApiCallResult<?> result, int expectedStatus) {
        softly.assertThat(result.getStatusCode())
                .as("HTTP status code")
                .isEqualTo(expectedStatus);
    }

    public void assertJsonFieldEquals(ApiResponse response, String jsonPath, Object expectedValue) {
        Object actual = response.getRaw().jsonPath().get(jsonPath);
        softly.assertThat(actual)
                .as("JSON field '%s'", jsonPath)
                .isEqualTo(expectedValue);
    }

    public void assertJsonFieldNotNull(ApiResponse response, String jsonPath) {
        Object actual = response.getRaw().jsonPath().get(jsonPath);
        softly.assertThat(actual)
                .as("JSON field '%s'", jsonPath)
                .isNotNull();
    }

    public void assertResponseTimeLessThan(ApiResponse response, long maxMillis) {
        softly.assertThat(response.getTime())
                .as("Response time (ms)")
                .isLessThan(maxMillis);
    }

    public void assertResponseTimeLessThan(ApiCallResult<?> result, long maxMillis) {
        softly.assertThat(result.getResponseTimeMs())
                .as("Response time (ms)")
                .isLessThan(maxMillis);
    }

    public void assertAll() {
        softly.assertAll();
    }
}
