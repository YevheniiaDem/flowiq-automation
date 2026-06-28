package com.flowiq.clients;

import io.qameta.allure.Step;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;

import java.util.Arrays;

public final class BaseResponseSpecification {

    private BaseResponseSpecification() {
    }

    public static ResponseSpecification ok() {
        return status(200);
    }

    public static ResponseSpecification created() {
        return status(201);
    }

    public static ResponseSpecification noContent() {
        return status(204);
    }

    public static ResponseSpecification unauthorized() {
        return status(401);
    }

    public static ResponseSpecification badRequest() {
        return status(400);
    }

    public static ResponseSpecification status(int expectedStatus) {
        return new ResponseSpecBuilder()
                .expectStatusCode(expectedStatus)
                .build();
    }

    @Step("Validate response status {expectedStatus}")
    public static void validate(ApiResponse response, int expectedStatus) {
        response.getRaw().then().spec(status(expectedStatus));
    }

    @Step("Validate response status is one of {expectedStatuses}")
    public static void validateAnyOf(ApiResponse response, int... expectedStatuses) {
        int actual = response.getStatusCode();
        boolean match = Arrays.stream(expectedStatuses).anyMatch(s -> s == actual);
        if (!match) {
            throw new AssertionError("Expected status one of " + Arrays.toString(expectedStatuses)
                    + " but was " + actual + ". Body: " + response.getBodyAsString());
        }
    }

    @Step("Extract response body with expected status {expectedStatus}")
    public static <T> T extract(ApiResponse response, Class<T> type, int expectedStatus) {
        validate(response, expectedStatus);
        return response.as(type);
    }

    @Step("Extract successful response body")
    public static <T> T extractOk(ApiResponse response, Class<T> type) {
        return extract(response, type, 200);
    }

    @Step("Extract created response body")
    public static <T> T extractCreated(ApiResponse response, Class<T> type) {
        return extract(response, type, 201);
    }
}
