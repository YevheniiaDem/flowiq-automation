package com.flowiq.agents.orchestrator.runner;

import com.flowiq.agents.orchestrator.model.QualityAgentRunResult;
import com.flowiq.agents.orchestrator.model.QualityAgentType;

public abstract class AbstractQualityAgentRunner implements QualityAgentRunner {

    private final QualityAgentType agentType;

    protected AbstractQualityAgentRunner(QualityAgentType agentType) {
        this.agentType = agentType;
    }

    @Override
    public QualityAgentType agentType() {
        return agentType;
    }

    @Override
    public QualityAgentRunResult run() {
        long start = System.currentTimeMillis();
        try {
            Object payload = execute();
            return QualityAgentRunResult.success(agentType, payload, elapsed(start));
        } catch (Exception e) {
            return QualityAgentRunResult.failure(agentType, e.getMessage(), elapsed(start));
        }
    }

    protected abstract Object execute() throws Exception;

    private static long elapsed(long start) {
        return System.currentTimeMillis() - start;
    }
}
