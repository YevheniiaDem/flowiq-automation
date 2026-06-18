package com.flowiq.agents.release.model;

import com.flowiq.agents.model.TestSuiteType;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SuiteExecutionSummary {
    TestSuiteType suiteType;
    int totalTests;
    int passed;
    int failed;
    int broken;
    int skipped;
    double passRate;
  @Singular
    List<String> failureDetails;
    String dataSource;

    public int failureCount() {
        return failed + broken;
    }

    public boolean hasFailures() {
        return failureCount() > 0;
    }
}
