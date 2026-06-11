package com.flowiq.clients;

import com.flowiq.auth.TokenManager;
import com.flowiq.clients.filters.RequestLoggingFilter;
import com.flowiq.clients.filters.ResponseLoggingFilter;
import com.flowiq.config.ConfigManager;
import com.flowiq.config.EnvironmentConfig;
import com.flowiq.constants.TestConstants;
import com.flowiq.utils.JsonUtils;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class BaseRequestSpecification {

    private static RequestSpecification baseSpec;

    static {
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                ObjectMapperConfig.objectMapperConfig()
                        .jackson2ObjectMapperFactory((type, charset) -> JsonUtils.mapper()));
    }

    private BaseRequestSpecification() {
    }

    public static RequestSpecification base() {
        if (baseSpec == null) {
            synchronized (BaseRequestSpecification.class) {
                if (baseSpec == null) {
                    baseSpec = buildBase();
                }
            }
        }
        return baseSpec;
    }

    public static RequestSpecification authenticated() {
        String token = TokenManager.getAccessToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("No JWT token. Authenticate via AuthService.login() first.");
        }
        return authenticated(token);
    }

    public static RequestSpecification authenticated(String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(base())
                .addHeader(TestConstants.AUTHORIZATION_HEADER, TestConstants.BEARER_PREFIX + token)
                .build();
    }

    public static RequestSpecification multipart() {
        return new RequestSpecBuilder()
                .addRequestSpecification(authenticated())
                .setContentType("multipart/form-data")
                .build();
    }

    public static void reset() {
        baseSpec = null;
    }

    private static RequestSpecification buildBase() {
        EnvironmentConfig config = ConfigManager.getConfig();
        log.info("Building request specification for: {}", config.apiUrl());

        return new RequestSpecBuilder()
                .setBaseUri(config.apiUrl())
                .setContentType(ContentType.JSON)
                .setAccept(TestConstants.ACCEPT_JSON)
                .addHeader(TestConstants.APP_LANGUAGE_HEADER, TestConstants.DEFAULT_LANGUAGE)
                .addHeader(TestConstants.APP_CURRENCY_HEADER, TestConstants.DEFAULT_CURRENCY)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new AllureRestAssured())
                .setRelaxedHTTPSValidation()
                .build();
    }
}
