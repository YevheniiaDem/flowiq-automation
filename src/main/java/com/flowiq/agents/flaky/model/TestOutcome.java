package com.flowiq.agents.flaky.model;

public enum TestOutcome {
    PASSED,
    FAILED,
    BROKEN,
    SKIPPED;

    public boolean isFailure() {
        return this == FAILED || this == BROKEN;
    }

    public static TestOutcome fromAllureStatus(String status) {
        if (status == null) {
            return SKIPPED;
        }
        return switch (status.toLowerCase()) {
            case "passed" -> PASSED;
            case "failed" -> FAILED;
            case "broken" -> BROKEN;
            case "skipped" -> SKIPPED;
            default -> SKIPPED;
        };
    }
}
