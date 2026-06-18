package com.flowiq.agents.prreview.analyzers;

import com.flowiq.agents.prreview.model.PrReviewFinding;
import com.flowiq.agents.prreview.scanner.PrReviewContext;

import java.util.List;

public interface PrReviewAnalyzer {

    String name();

    List<PrReviewFinding> analyze(PrReviewContext context);
}
