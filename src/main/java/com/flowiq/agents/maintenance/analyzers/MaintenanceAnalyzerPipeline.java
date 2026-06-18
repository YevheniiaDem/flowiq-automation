package com.flowiq.agents.maintenance.analyzers;

import com.flowiq.agents.maintenance.model.MaintenanceFinding;
import com.flowiq.agents.maintenance.scanner.MaintenanceContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

public class MaintenanceAnalyzerPipeline {

    private final List<MaintenanceAnalyzer> analyzers;

    public MaintenanceAnalyzerPipeline() {
        this(List.of(
                new DuplicateTestAnalyzer(),
                new DeadTestAnalyzer(),
                new DeadPageObjectAnalyzer(),
                new DeadDtoAnalyzer(),
                new DeadSchemaAnalyzer(),
                new LocatorQualityAnalyzer(),
                new OversizedPageObjectAnalyzer(),
                new OversizedTestClassAnalyzer(),
                new TestComplexityAnalyzer(),
                new NamingConventionAnalyzer()));
    }

    public MaintenanceAnalyzerPipeline(List<MaintenanceAnalyzer> analyzers) {
        this.analyzers = List.copyOf(analyzers);
    }

    public List<MaintenanceFinding> analyze(MaintenanceContext context) {
        List<MaintenanceFinding> findings = new ArrayList<>();
        for (MaintenanceAnalyzer analyzer : analyzers) {
            findings.addAll(analyzer.analyze(context));
        }
        findings.sort(Comparator
                .comparingInt(MaintenanceFinding::getPriorityRank)
                .thenComparing(f -> f.getSeverity().ordinal()));
        return List.copyOf(new LinkedHashSet<>(findings));
    }
}
