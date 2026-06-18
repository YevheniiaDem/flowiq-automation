package com.flowiq.agents.flaky.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TestStabilityMetrics {
    String testKey;
    String className;
    String methodName;
    String suite;
    int totalRuns;
    int passCount;
    int failCount;
    int brokenCount;
    int skipCount;
    double passRate;
    double failureRate;
    double flakinessPercent;
    boolean flaky;
}
