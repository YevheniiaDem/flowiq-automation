package com.flowiq.agents.generator.generator;

import com.flowiq.agents.generator.model.EndpointTestContext;
import com.flowiq.agents.generator.model.TestScenario;

import java.util.List;

/**
 * Strategy interface for QA scenario generators.
 */
public interface ScenarioGenerator {

    ScenarioGeneratorType type();

    List<TestScenario> generate(EndpointTestContext context);
}
