package com.flowiq.agents.prreview.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class PullRequestReviewResult {
    Instant reviewedAt;
    PrReviewVerdict verdict;
    int findingsCount;
    int criticalFindings;
    int highFindings;
    @Singular("changedFile")
    List<String> changedFiles;
    @Singular("finding")
    List<PrReviewFinding> findings;
    @Singular("summaryLine")
    List<String> executiveSummary;
    String recommendation;
    String dataSourcesSummary;
    String pullRequestSummary;
}
