package com.flowiq.agents.model;

public enum ChangeType {
    ADDED_ENDPOINT(false),
    REMOVED_ENDPOINT(true),
    MODIFIED_REQUEST_SCHEMA(false),
    MODIFIED_RESPONSE_SCHEMA(true),
    ADDED_REQUIRED_FIELD(true),
    REMOVED_REQUIRED_FIELD(false),
    ENUM_VALUE_ADDED(false),
    ENUM_VALUE_REMOVED(true),
    ENUM_REMOVED(true),
    STATUS_CODE_ADDED(false),
    STATUS_CODE_REMOVED(true),
    BREAKING_CHANGE(true);

    private final boolean breakingByDefault;

    ChangeType(boolean breakingByDefault) {
        this.breakingByDefault = breakingByDefault;
    }

    public boolean isBreakingByDefault() {
        return breakingByDefault;
    }
}
