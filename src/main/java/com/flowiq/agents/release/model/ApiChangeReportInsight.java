package com.flowiq.agents.release.model;

import com.flowiq.agents.model.RiskLevel;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ApiChangeReportInsight {
    boolean reportFound;
    RiskLevel riskLevel;
    int totalChanges;
    int breakingChanges;
  @Singular
    List<String> breakingChangeDescriptions;
  @Singular
    List<String> affectedContractTests;
    String summary;
}
