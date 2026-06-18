package com.flowiq.agents.flaky.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TestExecutionRecord {
    String testKey;
    String className;
    String methodName;
    String suite;
    TestOutcome outcome;
    String message;
    String stackTrace;
    String source;
    long durationMs;
}
