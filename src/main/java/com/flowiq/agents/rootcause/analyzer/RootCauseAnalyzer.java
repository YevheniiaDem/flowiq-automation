package com.flowiq.agents.rootcause.analyzer;

import com.flowiq.agents.rootcause.model.FailedTestContext;
import com.flowiq.agents.rootcause.model.RootCauseCategory;
import com.flowiq.agents.rootcause.model.RootCauseHypothesis;

import java.util.Optional;

public interface RootCauseAnalyzer {

    RootCauseCategory type();

    Optional<RootCauseHypothesis> analyze(FailureAnalysisContext context);
}
