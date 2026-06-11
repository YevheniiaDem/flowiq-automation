package com.flowiq.clients;

import io.restassured.response.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class ApiResponse {

    private final Response raw;

    public static ApiResponse from(Response response) {
        return new ApiResponse(response);
    }

    public int getStatusCode() {
        return raw.getStatusCode();
    }

    public long getTime() {
        return raw.getTime();
    }

    public String getBodyAsString() {
        return raw.getBody().asString();
    }

    public <T> T as(Class<T> clazz) {
        return raw.as(clazz);
    }

    public boolean isSuccessful() {
        int code = getStatusCode();
        return code >= 200 && code < 300;
    }
}
