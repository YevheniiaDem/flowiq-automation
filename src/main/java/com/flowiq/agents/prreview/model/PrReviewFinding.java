package com.flowiq.agents.prreview.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PrReviewFinding {
    PrReviewCategory category;
    PrReviewArea area;
    PrReviewSeverity severity;
    String title;
    String location;
    String recommendation;
    boolean blocking;
}
