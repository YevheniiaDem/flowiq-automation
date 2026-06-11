package com.flowiq.validation;

import com.flowiq.clients.ApiCallResult;
import com.flowiq.clients.ApiResponse;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@Slf4j
public final class JsonSchemaValidator {

    private static final String SCHEMAS_PREFIX = "schemas/";

    private JsonSchemaValidator() {
    }

    @Step("Validate JSON schema: {schemaPath}")
    public static void validate(ApiResponse response, String schemaPath) {
        String classpath = resolveClasspath(schemaPath);
        log.debug("Validating response against schema: {}", classpath);
        response.getRaw().then().body(matchesJsonSchemaInClasspath(classpath));
    }

    @Step("Validate JSON schema: {schemaPath}")
    public static void validate(ApiCallResult<?> result, String schemaPath) {
        if (result.getResponse() == null) {
            throw new IllegalArgumentException("ApiCallResult has no underlying ApiResponse for schema validation");
        }
        validate(result.getResponse(), schemaPath);
    }

    private static String resolveClasspath(String schemaPath) {
        if (schemaPath.startsWith(SCHEMAS_PREFIX)) {
            return schemaPath;
        }
        return SCHEMAS_PREFIX + schemaPath;
    }
}
