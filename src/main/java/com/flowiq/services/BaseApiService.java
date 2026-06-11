package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.clients.ApiResponse;
import com.flowiq.clients.BaseRequestSpecification;
import com.flowiq.clients.BaseResponseSpecification;
import com.flowiq.support.RetrySupport;
import io.qameta.allure.Step;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.util.Map;

import static io.restassured.RestAssured.given;

public abstract class BaseApiService {

    protected ApiResponse get(String path) {
        return get(path, Map.of());
    }

    @Step("GET {path} with query params")
    protected ApiResponse get(String path, Map<String, ?> queryParams) {
        return RetrySupport.executeApi(() -> {
            ApiResponse response = ApiResponse.from(
                    given()
                            .spec(authenticatedSpec())
                            .queryParams(queryParams)
                            .when()
                            .get(path)
            );
            return response;
        });
    }

    @Step("GET {path} (public)")
    protected ApiResponse getPublic(String path) {
        return getUnauthenticated(path);
    }

    @Step("GET {path} (unauthenticated)")
    protected ApiResponse getUnauthenticated(String path) {
        return getUnauthenticated(path, Map.of());
    }

    @Step("GET {path} (unauthenticated) with query params")
    protected ApiResponse getUnauthenticated(String path, Map<String, ?> queryParams) {
        return RetrySupport.executeApi(() -> ApiResponse.from(
                given()
                        .spec(BaseRequestSpecification.base())
                        .queryParams(queryParams)
                        .when()
                        .get(path)
        ));
    }

    @Step("POST {path} (unauthenticated)")
    protected ApiResponse postUnauthenticated(String path, Object body) {
        return RetrySupport.executeApi(() -> ApiResponse.from(
                given()
                        .spec(BaseRequestSpecification.base())
                        .body(body)
                        .when()
                        .post(path)
        ));
    }

    @Step("POST {path}")
    protected ApiResponse post(String path, Object body) {
        return RetrySupport.executeApi(() -> ApiResponse.from(
                given()
                        .spec(authenticatedSpec())
                        .body(body)
                        .when()
                        .post(path)
        ));
    }

    @Step("POST {path} (public)")
    protected ApiResponse postPublic(String path, Object body) {
        return RetrySupport.executeApi(() -> ApiResponse.from(
                given()
                        .spec(BaseRequestSpecification.base())
                        .body(body)
                        .when()
                        .post(path)
        ));
    }

    @Step("POST {path} without body")
    protected ApiResponse post(String path) {
        return RetrySupport.executeApi(() -> ApiResponse.from(
                given()
                        .spec(authenticatedSpec())
                        .when()
                        .post(path)
        ));
    }

    @Step("PUT {path}")
    protected ApiResponse put(String path, Object body) {
        return RetrySupport.executeApi(() -> ApiResponse.from(
                given()
                        .spec(authenticatedSpec())
                        .body(body)
                        .when()
                        .put(path)
        ));
    }

    @Step("PUT {path} without body")
    protected ApiResponse put(String path) {
        return RetrySupport.executeApi(() -> ApiResponse.from(
                given()
                        .spec(authenticatedSpec())
                        .when()
                        .put(path)
        ));
    }

    @Step("DELETE {path}")
    protected ApiResponse delete(String path) {
        return RetrySupport.executeApi(() -> ApiResponse.from(
                given()
                        .spec(authenticatedSpec())
                        .when()
                        .delete(path)
        ));
    }

    @Step("POST multipart {path}")
    protected ApiResponse postMultipart(String path, File file) {
        return RetrySupport.executeApi(() -> ApiResponse.from(
                given()
                        .spec(BaseRequestSpecification.multipart())
                        .multiPart("file", file)
                        .when()
                        .post(path)
        ));
    }

    protected <T> T getOk(String path, Class<T> type) {
        return BaseResponseSpecification.extractOk(get(path), type);
    }

    protected <T> T getOk(String path, Map<String, ?> queryParams, Class<T> type) {
        return BaseResponseSpecification.extractOk(get(path, queryParams), type);
    }

    protected <T> T postCreated(String path, Object body, Class<T> type) {
        return BaseResponseSpecification.extractCreated(post(path, body), type);
    }

    protected <T> ApiCallResult<T> fetch(String path, Class<T> bodyType) {
        return ApiCallResult.from(get(path), bodyType);
    }

    protected <T> ApiCallResult<T> fetch(String path, Map<String, ?> queryParams, Class<T> bodyType) {
        return ApiCallResult.from(get(path, queryParams), bodyType);
    }

    protected <T> ApiCallResult<T> fetchPublic(String path, Object body, Class<T> bodyType) {
        return ApiCallResult.from(postPublic(path, body), bodyType);
    }

    protected <T> ApiCallResult<T> fetchUnauthenticated(String path, Class<T> bodyType) {
        return ApiCallResult.from(getUnauthenticated(path), bodyType);
    }

    protected <T> ApiCallResult<T> fetchUnauthenticated(String path, Map<String, ?> queryParams, Class<T> bodyType) {
        return ApiCallResult.from(getUnauthenticated(path, queryParams), bodyType);
    }

    protected <T> ApiCallResult<T> attemptPost(String path, Object body, Class<T> bodyType) {
        return ApiCallResult.from(post(path, body), bodyType);
    }

    protected ApiCallResult<Void> attemptPostMultipart(String path, File file) {
        return ApiCallResult.from(postMultipart(path, file));
    }

    protected void deleteNoContent(String path) {
        BaseResponseSpecification.validate(delete(path), 204);
    }

    private RequestSpecification authenticatedSpec() {
        return BaseRequestSpecification.authenticated();
    }
}
