package com.flowiq.agents.release.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class FlakyReportInsight {
    boolean reportFound;
    int flakyTestCount;
    double portfolioPassRate;
    double portfolioFlakinessPercent;
  @Singular
    List<String> topUnstableTests;
    String summary;
}
