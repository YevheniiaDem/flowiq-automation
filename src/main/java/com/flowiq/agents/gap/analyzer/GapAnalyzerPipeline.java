package com.flowiq.agents.gap.analyzer;

import com.flowiq.agents.gap.model.TestGap;
import com.flowiq.agents.gap.prioritizer.BusinessImpactPrioritizer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

@Slf4j
public class GapAnalyzerPipeline {

    private final List<GapAnalyzer> analyzers;

    public GapAnalyzerPipeline(BusinessImpactPrioritizer prioritizer) {
        this.analyzers = List.of(
                new EndpointCoverageGapAnalyzer(prioritizer),
                new CrudGapAnalyzer(prioritizer),
                new NegativeScenarioGapAnalyzer(prioritizer),
                new AuthorizationGapAnalyzer(prioritizer)
        );
    }

    public List<TestGap> analyze(GapAnalysisContext context) {
        List<TestGap> gaps = new ArrayList<>();
        for (GapAnalyzer analyzer : analyzers) {
            List<TestGap> detected = analyzer.analyze(context);
            log.debug("{} detected {} gap(s)", analyzer.name(), detected.size());
            gaps.addAll(detected);
        }
        gaps.sort(Comparator.comparing(TestGap::getSeverity).thenComparing(TestGap::getModule));
        return List.copyOf(new LinkedHashSet<>(gaps));
    }
}
