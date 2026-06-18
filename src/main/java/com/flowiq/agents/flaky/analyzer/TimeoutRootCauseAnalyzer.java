package com.flowiq.agents.flaky.analyzer;

import com.flowiq.agents.flaky.model.RootCauseType;

import java.util.regex.Pattern;

public class TimeoutRootCauseAnalyzer extends AbstractPatternRootCauseAnalyzer {

    public TimeoutRootCauseAnalyzer() {
        super(RootCauseType.TIMEOUT,
                Pattern.compile("(?i)(timeout|timed out|TimeoutError|exceeded|awaitility|waiting for)",
                        Pattern.DOTALL),
                "Test exceeded configured timeout — likely slow UI render, API latency, or insufficient wait strategy.",
                0.85);
    }
}
