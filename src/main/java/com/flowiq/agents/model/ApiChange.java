package com.flowiq.agents.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiChange {
    ChangeType type;
    String path;
    String method;
    String schema;
    String field;
    String description;
    boolean breaking;

    public static ApiChange of(ChangeType type, String description) {
        return ApiChange.builder()
                .type(type)
                .description(description)
                .breaking(type.isBreakingByDefault())
                .build();
    }

    public static ApiChange endpoint(ChangeType type, String method, String path, String description) {
        return ApiChange.builder()
                .type(type)
                .method(method)
                .path(path)
                .description(description)
                .breaking(type.isBreakingByDefault())
                .build();
    }

    public static ApiChange schema(ChangeType type, String schema, String description, boolean breaking) {
        return ApiChange.builder()
                .type(type)
                .schema(schema)
                .description(description)
                .breaking(breaking)
                .build();
    }

    public static ApiChange field(ChangeType type, String schema, String field, String description) {
        return ApiChange.builder()
                .type(type)
                .schema(schema)
                .field(field)
                .description(description)
                .breaking(type.isBreakingByDefault())
                .build();
    }
}
