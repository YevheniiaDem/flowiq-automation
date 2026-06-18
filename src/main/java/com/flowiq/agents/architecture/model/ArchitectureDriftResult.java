package com.flowiq.agents.architecture.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class ArchitectureDriftResult {
    Instant analyzedAt;
    int architectureHealthScore;
    int issuesFound;
    int criticalIssues;
  @Singular("issue")
    List<ArchitectureDriftIssue> issues;
  @Singular("summaryLine")
    List<String> executiveSummary;
    String dataSourcesSummary;
}
