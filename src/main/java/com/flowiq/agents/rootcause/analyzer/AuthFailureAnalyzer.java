package com.flowiq.agents.rootcause.analyzer;

import com.flowiq.agents.rootcause.model.RootCauseCategory;

import java.util.regex.Pattern;

public class AuthFailureAnalyzer extends AbstractPatternRootCauseAnalyzer {

    public AuthFailureAnalyzer() {
        super(RootCauseCategory.AUTH,
                Pattern.compile("(?i)(401\\s+unauthorized|403\\s+forbidden|unauthorized|invalid token"
                        + "|jwt expired|access denied|authentication failed|missing bearer"
                        + "|status\\s*code[:\\s]+401|status\\s*code[:\\s]+403)",
                        Pattern.DOTALL),
                "Authentication or authorization failure — expired token, missing credentials, or RBAC mismatch.",
                86);
    }
}
