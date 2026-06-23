package com.flowiq.agents.factory.runner;

import com.flowiq.agents.factory.config.AiQualityFactoryConfig;
import com.flowiq.agents.factory.config.FailureIntelligenceConfig;
import com.flowiq.agents.factory.config.GovernanceIntelligenceConfig;
import com.flowiq.agents.factory.config.PrIntelligenceConfig;
import com.flowiq.agents.factory.model.FactoryAgentResultsBundle;
import com.flowiq.agents.factory.model.FactoryAgentRunResult;

import java.util.ArrayList;
import java.util.List;

public final class FactoryAgentExecutionPipeline {

    private FactoryAgentExecutionPipeline() {
    }

    public static ExecutionOutcome execute(List<FactoryAgentRunner> runners, boolean continueOnFailure) {
        FactoryAgentResultsBundle bundle = new FactoryAgentResultsBundle();
        List<FactoryAgentRunResult> runs = new ArrayList<>();
        int failed = 0;

        for (FactoryAgentRunner runner : runners) {
            FactoryAgentRunResult result = runner.run();
            runs.add(result);
            if (result.isSuccess()) {
                bundle.add(result);
            } else {
                failed++;
                if (!continueOnFailure) {
                    break;
                }
            }
        }
        return new ExecutionOutcome(bundle, runs, failed);
    }

    public static ExecutionOutcome execute(FactoryAgentRunner runner, boolean continueOnFailure) {
        return execute(List.of(runner), continueOnFailure);
    }

    public record ExecutionOutcome(
            FactoryAgentResultsBundle bundle,
            List<FactoryAgentRunResult> runs,
            int failedCount) {

        public int succeededCount() {
            return runs.size() - failedCount;
        }
    }

    public static boolean continueOnFailure(PrIntelligenceConfig config) {
        return config.continueOnFailure();
    }

    public static boolean continueOnFailure(GovernanceIntelligenceConfig config) {
        return config.continueOnFailure();
    }

    public static boolean continueOnFailure(FailureIntelligenceConfig config) {
        return config.continueOnFailure();
    }

    public static boolean continueOnFailure(AiQualityFactoryConfig config) {
        return config.continueOnFailure();
    }
}
