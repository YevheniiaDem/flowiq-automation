package com.flowiq.ci.flaky.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CiFlakyTestEntry {
    String testKey;
    String className;
    String methodName;
    String suite;
    FlakyClassification classification;
    boolean failedInCurrentRun;
    boolean passedInCurrentRun;
    boolean recoveredThisRun;
    double flakinessPercent;
    int totalRuns;
    int passCount;
    int failCount;
    double durationCv;
    long avgDurationMs;
    long minDurationMs;
    long maxDurationMs;
    String currentOutcome;
}
