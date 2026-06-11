package com.flowiq.clients;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiCallResult<T> {

    ApiResponse response;
    int statusCode;
    long responseTimeMs;
    T body;
    String rawBody;

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public static <T> ApiCallResult<T> from(ApiResponse response, Class<T> bodyType) {
        T body = response.isSuccessful() && bodyType != Void.class
                ? response.as(bodyType)
                : null;
        return ApiCallResult.<T>builder()
                .response(response)
                .statusCode(response.getStatusCode())
                .responseTimeMs(response.getTime())
                .body(body)
                .rawBody(response.getBodyAsString())
                .build();
    }

    public static ApiCallResult<Void> from(ApiResponse response) {
        return from(response, Void.class);
    }
}
