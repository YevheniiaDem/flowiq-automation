package com.flowiq.agents.flaky.analyzer;

import com.flowiq.agents.flaky.model.RootCauseHypothesis;
import com.flowiq.agents.flaky.model.RootCauseType;
import com.flowiq.agents.flaky.model.TestExecutionRecord;

import java.util.List;

/**
 * Strategy interface for flaky test root-cause analysis.
 */
public interface RootCauseAnalyzer {

    RootCauseType type();

    RootCauseHypothesis analyze(String combinedFailureText, List<TestExecutionRecord> failureRuns);
}
