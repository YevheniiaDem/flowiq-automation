package com.flowiq.clients;

import com.flowiq.support.RetrySupport;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.util.Map;
import java.util.function.Function;

import static io.restassured.RestAssured.given;

/**
 * Central RestAssured execution with retry. All domain services route HTTP calls through here.
 */
public final class ApiRequestExecutor {

    private ApiRequestExecutor() {
    }

    public static ApiResponse get(RequestSpecification spec, String path) {
        return get(spec, path, Map.of());
    }

    public static ApiResponse get(RequestSpecification spec, String path, Map<String, ?> queryParams) {
        return execute(spec, request -> request.queryParams(queryParams).when().get(path));
    }

    public static ApiResponse post(RequestSpecification spec, String path, Object body) {
        return execute(spec, request -> {
            if (body != null) {
                request.body(body);
            }
            return request.when().post(path);
        });
    }

    public static ApiResponse put(RequestSpecification spec, String path, Object body) {
        return execute(spec, request -> {
            if (body != null) {
                request.body(body);
            }
            return request.when().put(path);
        });
    }

    public static ApiResponse put(RequestSpecification spec, String path) {
        return put(spec, path, null);
    }

    public static ApiResponse delete(RequestSpecification spec, String path) {
        return execute(spec, request -> request.when().delete(path));
    }

    public static ApiResponse postMultipart(RequestSpecification spec, String path, File file) {
        return execute(spec, request -> request.multiPart("file", file).when().post(path));
    }

    private static ApiResponse execute(RequestSpecification spec,
                                       Function<RequestSpecification, Response> action) {
        return RetrySupport.executeApi(() -> ApiResponse.from(action.apply(given().spec(spec))));
    }
}
