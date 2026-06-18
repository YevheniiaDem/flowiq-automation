package com.flowiq.agents.traceability.model;

import com.flowiq.agents.gap.model.GapSeverity;
import com.flowiq.agents.model.TestSuiteType;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class FeatureTraceabilityRow {
    String module;
    String featureName;
    String endpointsSummary;
    boolean smokeCovered;
    boolean regressionCovered;
    boolean contractCovered;
    boolean uiCovered;
    String smokeTests;
    String regressionTests;
    String contractTests;
    String uiTests;
    double coveragePercent;
    int endpointCount;
    boolean documentedInDocs;
    GapSeverity businessImpact;
    boolean highRisk;
  @Singular
    Set<TestSuiteType> missingSuites;
  @Singular("docSource")
    Set<String> docSources;
}
