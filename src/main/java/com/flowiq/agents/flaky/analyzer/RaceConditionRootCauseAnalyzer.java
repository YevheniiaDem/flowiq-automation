package com.flowiq.agents.flaky.analyzer;

import com.flowiq.agents.flaky.model.RootCauseType;

import java.util.regex.Pattern;

public class RaceConditionRootCauseAnalyzer extends AbstractPatternRootCauseAnalyzer {

    public RaceConditionRootCauseAnalyzer() {
        super(RootCauseType.RACE_CONDITION,
                Pattern.compile("(?i)(stale|race|intermittent|sometimes|flaky|parallel|concurrent|already (closed|exists))",
                        Pattern.DOTALL),
                "Probable race condition — async state not settled before assertion or shared test data collision.",
                0.75);
    }
}
