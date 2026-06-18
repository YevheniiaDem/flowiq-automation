package com.flowiq.agents.flaky.analyzer;

import com.flowiq.agents.flaky.model.RootCauseType;

import java.util.regex.Pattern;

public class LocatorRootCauseAnalyzer extends AbstractPatternRootCauseAnalyzer {

    public LocatorRootCauseAnalyzer() {
        super(RootCauseType.LOCATOR_ISSUE,
                Pattern.compile("(?i)(locator|element not found|strict mode|playwright|no node found|selector)",
                        Pattern.DOTALL),
                "UI locator instability — DOM timing, missing data-testid, or strict-mode duplicate matches.",
                0.88);
    }
}
