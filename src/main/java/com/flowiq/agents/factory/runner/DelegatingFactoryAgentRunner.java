package com.flowiq.agents.factory.runner;

import com.flowiq.agents.factory.model.FactoryAgentType;

@FunctionalInterface
interface FactoryAgentExecution {

    Object execute() throws Exception;
}

class DelegatingFactoryAgentRunner extends AbstractFactoryAgentRunner {

    private final FactoryAgentExecution execution;

    DelegatingFactoryAgentRunner(FactoryAgentType agentType, FactoryAgentExecution execution) {
        super(agentType);
        this.execution = execution;
    }

    @Override
    protected Object execute() throws Exception {
        return execution.execute();
    }
}
