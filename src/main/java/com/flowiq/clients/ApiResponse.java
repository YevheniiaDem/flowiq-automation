package com.flowiq.clients;

import com.flowiq.utils.JsonUtils;
import io.restassured.path.json.JsonPath;
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

    public JsonPath jsonPath() {
        return raw.jsonPath();
    }

    public <T> T as(Class<T> clazz) {
        String body = getBodyAsString();
        if (body == null || body.isBlank()) {
            return null;
        }
        return JsonUtils.fromJson(body, clazz);
    }

    public boolean isSuccessful() {
        int code = getStatusCode();
        return code >= 200 && code < 300;
    }
}
