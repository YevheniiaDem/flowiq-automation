package com.flowiq.agents.generator.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class TestScenario {
    String id;
    String title;
    ScenarioType type;
    String module;
    String endpoint;
    String httpMethod;
  @Singular
    List<String> preconditions;
  @Singular
    List<String> steps;
    String expectedResult;
    ScenarioPriority priority;
    ScenarioRisk risk;
}
