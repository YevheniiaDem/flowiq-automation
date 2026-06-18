package com.flowiq.agents.gap.analyzer;

import com.flowiq.agents.gap.model.TestGap;

import java.util.List;

/**
 * Strategy interface for test gap analyzers.
 */
public interface GapAnalyzer {

    String name();

    List<TestGap> analyze(GapAnalysisContext context);
}
