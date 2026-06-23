package com.flowiq.agents.factory.runner;

import com.flowiq.agents.factory.model.FactoryAgentRunResult;
import com.flowiq.agents.factory.model.FactoryAgentType;

public abstract class AbstractFactoryAgentRunner implements FactoryAgentRunner {

    private final FactoryAgentType agentType;

    protected AbstractFactoryAgentRunner(FactoryAgentType agentType) {
        this.agentType = agentType;
    }

    @Override
    public FactoryAgentType agentType() {
        return agentType;
    }

    @Override
    public FactoryAgentRunResult run() {
        long start = System.currentTimeMillis();
        try {
            Object payload = execute();
            return FactoryAgentRunResult.success(agentType, payload, elapsed(start));
        } catch (Exception e) {
            return FactoryAgentRunResult.failure(agentType, e.getMessage(), elapsed(start));
        }
    }

    protected abstract Object execute() throws Exception;

    private static long elapsed(long start) {
        return System.currentTimeMillis() - start;
    }
}
