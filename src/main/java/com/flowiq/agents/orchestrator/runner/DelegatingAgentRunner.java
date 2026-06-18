package com.flowiq.agents.orchestrator.runner;

import com.flowiq.agents.orchestrator.model.QualityAgentType;

@FunctionalInterface
interface AgentExecution {

    Object execute() throws Exception;
}

class DelegatingAgentRunner extends AbstractQualityAgentRunner {

    private final AgentExecution execution;

    DelegatingAgentRunner(QualityAgentType agentType, AgentExecution execution) {
        super(agentType);
        this.execution = execution;
    }

    @Override
    protected Object execute() throws Exception {
        return execution.execute();
    }
}
