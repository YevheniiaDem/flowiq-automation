package com.flowiq.services;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.clients.ApiRequestExecutor;
import com.flowiq.clients.ApiResponse;
import com.flowiq.clients.BaseRequestSpecification;
import com.flowiq.clients.BaseResponseSpecification;
import io.qameta.allure.Step;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract class BaseApiService {

    protected ApiResponse get(String path) {
        return get(path, Map.of());
    }

    @Step("GET {path} with query params")
    protected ApiResponse get(String path, Map<String, ?> queryParams) {
        return ApiRequestExecutor.get(authenticatedSpec(), path, queryParams);
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
        return ApiRequestExecutor.get(baseSpec(), path, queryParams);
    }

    @Step("POST {path} (unauthenticated)")
    protected ApiResponse postUnauthenticated(String path, Object body) {
        return ApiRequestExecutor.post(baseSpec(), path, body);
    }

    @Step("POST {path}")
    protected ApiResponse post(String path, Object body) {
        return ApiRequestExecutor.post(authenticatedSpec(), path, body);
    }

    @Step("POST {path} (public)")
    protected ApiResponse postPublic(String path, Object body) {
        return postUnauthenticated(path, body);
    }

    @Step("POST {path} without body")
    protected ApiResponse post(String path) {
        return ApiRequestExecutor.post(authenticatedSpec(), path, null);
    }

    @Step("PUT {path}")
    protected ApiResponse put(String path, Object body) {
        return ApiRequestExecutor.put(authenticatedSpec(), path, body);
    }

    @Step("PUT {path} without body")
    protected ApiResponse put(String path) {
        return ApiRequestExecutor.put(authenticatedSpec(), path);
    }

    @Step("DELETE {path}")
    protected ApiResponse delete(String path) {
        return ApiRequestExecutor.delete(authenticatedSpec(), path);
    }

    @Step("POST multipart {path}")
    protected ApiResponse postMultipart(String path, File file) {
        return ApiRequestExecutor.postMultipart(multipartSpec(), path, file);
    }

    @Step("POST multipart {path} (unauthenticated)")
    protected ApiResponse postMultipartUnauthenticated(String path, File file) {
        return ApiRequestExecutor.postMultipart(multipartUnauthenticatedSpec(), path, file);
    }

    protected <T> List<T> getList(String path, Class<T> itemType) {
        return get(path).jsonPath().getList("", itemType);
    }

    protected <T> List<T> getList(String path, Map<String, ?> queryParams, Class<T> itemType) {
        return get(path, queryParams).jsonPath().getList("", itemType);
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

    protected <T> T putOk(String path, Object body, Class<T> type) {
        return BaseResponseSpecification.extractOk(put(path, body), type);
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

    protected <T> ApiCallResult<T> attemptPostUnauthorized(String path, Object body, Class<T> bodyType) {
        return ApiCallResult.from(postUnauthenticated(path, body), bodyType);
    }

    protected <T> ApiCallResult<T> attemptPutUnauthorized(String path, Object body, Class<T> bodyType) {
        return ApiCallResult.from(putUnauthenticated(path, body), bodyType);
    }

    protected <T> ApiCallResult<T> attemptPutUnauthorized(String path, Class<T> bodyType) {
        return ApiCallResult.from(putUnauthenticated(path), bodyType);
    }

    protected ApiResponse putUnauthenticated(String path, Object body) {
        return ApiRequestExecutor.put(baseSpec(), path, body);
    }

    protected ApiResponse putUnauthenticated(String path) {
        return ApiRequestExecutor.put(baseSpec(), path);
    }

    protected <T> ApiCallResult<T> attemptGet(String path, Class<T> bodyType) {
        return ApiCallResult.from(get(path), bodyType);
    }

    protected <T> ApiCallResult<T> attemptGet(String path, Map<String, ?> queryParams, Class<T> bodyType) {
        return ApiCallResult.from(get(path, queryParams), bodyType);
    }

    protected <T> ApiCallResult<T> attemptPut(String path, Object body, Class<T> bodyType) {
        return ApiCallResult.from(put(path, body), bodyType);
    }

    protected <T> ApiCallResult<T> attemptPut(String path, Class<T> bodyType) {
        return ApiCallResult.from(put(path), bodyType);
    }

    protected ApiCallResult<Void> attemptDelete(String path) {
        return ApiCallResult.from(delete(path));
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

    private RequestSpecification baseSpec() {
        return BaseRequestSpecification.base();
    }

    private RequestSpecification multipartSpec() {
        return BaseRequestSpecification.multipart();
    }

    private RequestSpecification multipartUnauthenticatedSpec() {
        return BaseRequestSpecification.multipartUnauthenticated();
    }
}
