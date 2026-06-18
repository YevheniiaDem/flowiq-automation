package com.flowiq.agents.rootcause.analyzer;

import com.flowiq.agents.rootcause.model.RootCauseCategory;

import java.util.regex.Pattern;

public class UiFailureAnalyzer extends AbstractPatternRootCauseAnalyzer {

    public UiFailureAnalyzer() {
        super(RootCauseCategory.UI_BUG,
                Pattern.compile("(?i)(playwright|locator|element not found|strict mode violation"
                        + "|waiting for selector|TimeoutError|page\\.click|getByTestId"
                        + "|not visible|detached from DOM|navigation timeout)",
                        Pattern.DOTALL),
                "UI layer failure — locator instability, rendering delay, or broken page object.",
                88);
    }

    @Override
    protected int adjustConfidence(int confidence, FailureAnalysisContext context) {
        int adjusted = confidence;
        String className = context.failedTest().getExecution().getClassName();
        if (className != null && (className.contains(".ui.") || className.contains("SmokeTest"))) {
            adjusted += 7;
        }
        if (!context.failedTest().getScreenshots().isEmpty()) {
            adjusted += 5;
        }
        if (!context.failedTest().getTraces().isEmpty()) {
            adjusted += 5;
        }
        return adjusted;
    }
}
