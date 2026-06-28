package com.flowiq.ci.flaky.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class CiFlakyReport {
    Instant analyzedAt;
    String runId;
    String workflow;
    int currentRunTotal;
    int currentRunPassed;
    int currentRunFailed;
    int flakyCount;
    int failedOnlyCount;
    int durationUnstableCount;
    int recoveredThisRunCount;
    int historyRunCount;
    @Singular
    List<CiFlakyTestEntry> flakyTests;
    @Singular
    List<CiFlakyTestEntry> failedTests;
    @Singular
    List<String> summaryLines;
}
