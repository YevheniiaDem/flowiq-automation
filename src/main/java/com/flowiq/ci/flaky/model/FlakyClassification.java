package com.flowiq.ci.flaky.model;

/**
 * How a test was classified — business-test instability only (never CI infrastructure retries).
 */
public enum FlakyClassification {
    INTERMITTENT_OUTCOME,
    DURATION_UNSTABLE,
    INTERMITTENT_AND_DURATION
}
