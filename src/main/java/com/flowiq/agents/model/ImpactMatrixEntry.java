package com.flowiq.agents.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ImpactMatrixEntry {
    String apiPath;
    String httpMethod;
    List<ApiChange> changes;
    List<String> contractTests;
    List<String> smokeTests;
    List<String> regressionTests;
    List<String> uiTests;
    RiskLevel riskLevel;
}
