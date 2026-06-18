package com.flowiq.agents.rootcause.analyzer;

import com.flowiq.agents.rootcause.model.RootCauseCategory;

import java.util.regex.Pattern;

public class BackendFailureAnalyzer extends AbstractPatternRootCauseAnalyzer {

    public BackendFailureAnalyzer() {
        super(RootCauseCategory.BACKEND_BUG,
                Pattern.compile("(?i)(internal server error|http\\s*5\\d\\d|status\\s*code[:\\s]+5\\d\\d"
                        + "|NullPointerException|IllegalStateException|ResponseStatusException"
                        + "|Unhandled exception|ERROR\\s+\\[.*\\].*Exception)",
                        Pattern.DOTALL),
                "Application backend threw an unhandled error or returned HTTP 5xx.",
                85);
    }

    @Override
    protected int adjustConfidence(int confidence, FailureAnalysisContext context) {
        int adjusted = confidence;
        if (!context.backendLogExcerpt().isEmpty()) {
            adjusted += 10;
        }
        String className = context.failedTest().getExecution().getClassName();
        if (className != null && !className.toLowerCase().contains(".ui.")) {
            adjusted += 5;
        }
        return adjusted;
    }
}
