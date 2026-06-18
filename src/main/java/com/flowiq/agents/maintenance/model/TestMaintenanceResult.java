package com.flowiq.agents.maintenance.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class TestMaintenanceResult {
    Instant analyzedAt;
    int automationHealthScore;
    MaintenanceHealthCategory healthCategory;
    int findingsCount;
    int deadComponents;
    int duplicateComponents;
    int flakyCandidates;
    @Singular("finding")
    List<MaintenanceFinding> findings;
    @Singular("technicalDebtSummaryLine")
    List<String> technicalDebtSummary;
    @Singular("refactoringRecommendationLine")
    List<String> refactoringRecommendations;
    @Singular("topPriorityFixLine")
    List<String> topPriorityFixes;
    String dataSourcesSummary;
}
