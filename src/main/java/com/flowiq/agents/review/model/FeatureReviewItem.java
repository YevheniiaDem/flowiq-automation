package com.flowiq.agents.review.model;

import com.flowiq.agents.gap.model.GapSeverity;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class FeatureReviewItem {
    FeatureChange feature;
    CoverageStatus coverageStatus;
  @Singular("missingTest")
    List<String> missingTests;
    GapSeverity risk;
    String recommendation;
    ReviewVerdict verdict;
}
