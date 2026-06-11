package com.flowiq.clients;

import com.flowiq.auth.TokenManager;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ApiClientFactory {

    private ApiClientFactory() {
    }

    public static RequestSpecification baseSpec() {
        return BaseRequestSpecification.base();
    }

    public static RequestSpecification authenticatedSpec() {
        return BaseRequestSpecification.authenticated();
    }

    public static RequestSpecification authenticatedSpec(String token) {
        return BaseRequestSpecification.authenticated(token);
    }

    public static void reset() {
        BaseRequestSpecification.reset();
        TokenManager.clear();
        RestAssured.reset();
        log.debug("API client reset");
    }
}
