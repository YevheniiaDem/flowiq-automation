package com.flowiq.agents.factory.runner;

import com.flowiq.agents.factory.model.FactoryAgentRunResult;
import com.flowiq.agents.factory.model.FactoryAgentType;

public interface FactoryAgentRunner {

    FactoryAgentType agentType();

    FactoryAgentRunResult run();
}
