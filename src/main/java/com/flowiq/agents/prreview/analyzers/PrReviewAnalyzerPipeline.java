package com.flowiq.agents.prreview.analyzers;

import com.flowiq.agents.prreview.model.PrReviewFinding;
import com.flowiq.agents.prreview.scanner.PrReviewContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

public class PrReviewAnalyzerPipeline {

    private final List<PrReviewAnalyzer> analyzers;

    public PrReviewAnalyzerPipeline() {
        this(List.of(
                new ApiReviewAnalyzer(),
                new BackendReviewAnalyzer(),
                new AutomationReviewAnalyzer(),
                new UiReviewAnalyzer(),
                new QualityReviewAnalyzer()));
    }

    public PrReviewAnalyzerPipeline(List<PrReviewAnalyzer> analyzers) {
        this.analyzers = List.copyOf(analyzers);
    }

    public List<PrReviewFinding> analyze(PrReviewContext context) {
        List<PrReviewFinding> findings = new ArrayList<>();
        for (PrReviewAnalyzer analyzer : analyzers) {
            findings.addAll(analyzer.analyze(context));
        }
        findings.sort(Comparator
                .comparing((PrReviewFinding f) -> f.getSeverity().ordinal())
                .thenComparing(PrReviewFinding::getCategory));
        return List.copyOf(new LinkedHashSet<>(findings));
    }
}
