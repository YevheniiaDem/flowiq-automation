package com.flowiq.agents.selfhealing.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LocatorSuggestion {
    String testKey;
    String testName;
    String oldLocator;
    String suggestedLocator;
    LocatorType suggestedLocatorType;
    LocatorConfidence confidence;
    double similarityScore;
    String reasoning;
    LocatorRisk risk;
    String screenshotPath;
    String domSnapshotPath;
    boolean llmEnriched;
}
