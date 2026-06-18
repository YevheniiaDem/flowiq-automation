package com.flowiq.agents.selfhealing.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class SelfHealingResult {
    Instant analyzedAt;
    int failuresAnalyzed;
    int suggestionsGenerated;
  @Singular("suggestion")
    List<LocatorSuggestion> suggestions;
  @Singular("summaryLine")
    List<String> executiveSummary;
    String dataSourcesSummary;
}
