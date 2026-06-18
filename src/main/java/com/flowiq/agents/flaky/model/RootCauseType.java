package com.flowiq.agents.flaky.model;

public enum RootCauseType {
    TIMEOUT,
    LOCATOR_ISSUE,
    NETWORK_INSTABILITY,
    BACKEND_INSTABILITY,
    RACE_CONDITION,
    UNKNOWN
}
