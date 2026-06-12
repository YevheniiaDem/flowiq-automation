package com.flowiq.contracts;

import com.flowiq.assertions.ApiAssertions;
import com.flowiq.clients.ApiCallResult;
import com.flowiq.clients.ApiResponse;
import com.flowiq.validation.JsonSchemaValidator;
import io.qameta.allure.Step;
import org.assertj.core.api.Assertions;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ContractAssertions {

    private ContractAssertions() {
    }

    @Step("Contract: HTTP {expectedStatus} and schema {schemaPath}")
    public static void assertContractResponse(ApiCallResult<?> result, int expectedStatus, String schemaPath) {
        ApiAssertions.assertStatusCode(result, expectedStatus);
        assertSchemaValid(result, schemaPath);
    }

    @Step("Validate response schema: {schemaPath}")
    public static void assertSchemaValid(ApiCallResult<?> result, String schemaPath) {
        JsonSchemaValidator.validate(result, schemaPath);
    }

    @Step("Validate response schema: {schemaPath}")
    public static void assertSchemaValid(ApiResponse response, String schemaPath) {
        JsonSchemaValidator.validate(response, schemaPath);
    }

    @Step("Assert required JSON fields present: {jsonPaths}")
    public static void assertRequiredFieldsPresent(ApiResponse response, String... jsonPaths) {
        for (String path : jsonPaths) {
            ApiAssertions.assertJsonFieldNotNull(response, path);
        }
    }

    @Step("Assert required JSON fields present: {jsonPaths}")
    public static void assertRequiredFieldsPresent(ApiCallResult<?> result, String... jsonPaths) {
        Assertions.assertThat(result.getResponse())
                .as("ApiCallResult response")
                .isNotNull();
        assertRequiredFieldsPresent(result.getResponse(), jsonPaths);
    }

    @Step("Assert enum value at {jsonPath}")
    public static void assertEnumValue(ApiResponse response, String jsonPath, Class<? extends Enum<?>> enumType) {
        Object value = response.getRaw().jsonPath().get(jsonPath);
        Assertions.assertThat(value)
                .as("JSON field '%s'", jsonPath)
                .isNotNull();
        Set<String> allowed = enumNames(enumType);
        Assertions.assertThat(String.valueOf(value))
                .as("Enum at '%s'", jsonPath)
                .isIn(allowed);
    }

    @Step("Assert enum values in array at {jsonPath}")
    public static void assertEnumValues(ApiResponse response, String jsonPath, Class<? extends Enum<?>> enumType) {
        List<?> values = response.getRaw().jsonPath().getList(jsonPath);
        if (values == null || values.isEmpty()) {
            return;
        }
        Set<String> allowed = enumNames(enumType);
        values.stream()
                .map(String::valueOf)
                .forEach(value -> Assertions.assertThat(value)
                        .as("Enum value in '%s'", jsonPath)
                        .isIn(allowed));
    }

    @Step("Assert enum values in nested array {arrayPath}.{fieldName}")
    public static void assertEnumValuesInNestedArray(ApiResponse response,
                                                     String arrayPath,
                                                     String fieldName,
                                                     Class<? extends Enum<?>> enumType) {
        List<?> items = response.getRaw().jsonPath().getList(arrayPath);
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<String> allowed = enumNames(enumType);
        List<?> fieldValues = response.getRaw().jsonPath().getList(arrayPath + "." + fieldName);
        if (fieldValues == null) {
            return;
        }
        fieldValues.stream()
                .map(String::valueOf)
                .forEach(value -> Assertions.assertThat(value)
                        .as("Enum '%s' in '%s'", fieldName, arrayPath)
                        .isIn(allowed));
    }

    private static Set<String> enumNames(Class<? extends Enum<?>> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());
    }

    @SafeVarargs
    public static void assertAllRequired(ApiCallResult<?> result, int status, String schema, String... fields) {
        assertContractResponse(result, status, schema);
        assertRequiredFieldsPresent(result, fields);
    }
}
