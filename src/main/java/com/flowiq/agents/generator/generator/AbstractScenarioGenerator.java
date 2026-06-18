package com.flowiq.agents.generator.generator;

import com.flowiq.agents.generator.model.EndpointTestContext;
import com.flowiq.agents.generator.model.ScenarioType;

abstract class AbstractScenarioGenerator implements ScenarioGenerator {

    protected boolean isUncovered(EndpointTestContext context, ScenarioType type) {
        return !context.getCoveredScenarioTypes().contains(type);
    }

    protected String scenarioId(EndpointTestContext context, ScenarioType type, String suffix) {
        return context.getModule() + "-" + context.getOperation().method() + "-"
                + context.getNormalizedPath().replace("/", "-").replace("{", "").replace("}", "")
                + "-" + type.name().toLowerCase() + "-" + suffix;
    }
}
