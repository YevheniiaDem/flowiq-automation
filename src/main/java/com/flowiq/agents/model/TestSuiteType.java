package com.flowiq.agents.model;

public enum TestSuiteType {
    CONTRACT("Contract Tests"),
    SMOKE("Smoke Tests"),
    REGRESSION("Regression Tests"),
    UI("UI Tests");

    private final String displayName;

    TestSuiteType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
