package com.flowiq.agents.review.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class TestReviewResult {
    Instant reviewedAt;
    ReviewVerdict overallVerdict;
    int featuresReviewed;
    int rejectedCount;
    int approvedWithRiskCount;
  @Singular("feature")
    List<FeatureReviewItem> features;
  @Singular("summaryLine")
    List<String> executiveSummary;
    String dataSourcesSummary;
    String pullRequestSummary;
}
