package com.flowiq.agents.orchestrator.runner;

import com.flowiq.agents.orchestrator.model.QualityAgentRunResult;
import com.flowiq.agents.orchestrator.model.QualityAgentType;

public interface QualityAgentRunner {

    QualityAgentType agentType();

    QualityAgentRunResult run();
}
