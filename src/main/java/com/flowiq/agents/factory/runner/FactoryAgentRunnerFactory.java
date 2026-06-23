package com.flowiq.agents.factory.runner;

import com.flowiq.agents.ApiChangeDetectionAgent;
import com.flowiq.agents.architecture.ArchitectureDriftAgent;
import com.flowiq.agents.factory.model.FactoryAgentType;
import com.flowiq.agents.flaky.FlakyTestInvestigator;
import com.flowiq.agents.gap.TestGapAnalyzerAgent;
import com.flowiq.agents.generator.SmartTestGeneratorAgent;
import com.flowiq.agents.maintenance.TestMaintenanceAgent;
import com.flowiq.agents.prreview.PullRequestReviewAgent;
import com.flowiq.agents.regressionrisk.RiskBasedRegressionAgent;
import com.flowiq.agents.release.ReleaseRiskAssessmentAgent;
import com.flowiq.agents.review.TestReviewAgent;
import com.flowiq.agents.rootcause.RootCauseAnalysisAgent;
import com.flowiq.agents.selfhealing.SelfHealingLocatorAgent;
import com.flowiq.agents.traceability.RequirementsTraceabilityAgent;

import java.util.List;

public final class FactoryAgentRunnerFactory {

    private FactoryAgentRunnerFactory() {
    }

    public static List<FactoryAgentRunner> prIntelligenceRunners() {
        return List.of(
                runner(FactoryAgentType.PULL_REQUEST_REVIEW, () -> new PullRequestReviewAgent().run()),
                runner(FactoryAgentType.TEST_REVIEW, () -> new TestReviewAgent().run()),
                runner(FactoryAgentType.API_CHANGE_DETECTION, () -> new ApiChangeDetectionAgent().run()),
                runner(FactoryAgentType.RISK_BASED_REGRESSION, () -> new RiskBasedRegressionAgent().run()));
    }

    public static List<FactoryAgentRunner> governanceIntelligenceRunners() {
        return List.of(
                runner(FactoryAgentType.ARCHITECTURE_DRIFT, () -> new ArchitectureDriftAgent().run()),
                runner(FactoryAgentType.REQUIREMENTS_TRACEABILITY, () -> new RequirementsTraceabilityAgent().run()),
                runner(FactoryAgentType.TEST_GAP_ANALYZER, () -> new TestGapAnalyzerAgent().run()),
                runner(FactoryAgentType.TEST_MAINTENANCE, () -> new TestMaintenanceAgent().run()));
    }

    public static List<FactoryAgentRunner> failureIntelligenceRunners() {
        return List.of(
                runner(FactoryAgentType.ROOT_CAUSE_ANALYSIS, () -> new RootCauseAnalysisAgent().run()),
                runner(FactoryAgentType.FLAKY_TEST_INVESTIGATOR, () -> new FlakyTestInvestigator().run()),
                runner(FactoryAgentType.SELF_HEALING_LOCATOR, () -> new SelfHealingLocatorAgent().run()));
    }

    public static FactoryAgentRunner releaseRiskRunner() {
        return runner(FactoryAgentType.RELEASE_RISK_ASSESSMENT, () -> new ReleaseRiskAssessmentAgent().run());
    }

    public static FactoryAgentRunner smartTestGeneratorRunner() {
        return runner(FactoryAgentType.SMART_TEST_GENERATOR, () -> new SmartTestGeneratorAgent().run());
    }

    private static FactoryAgentRunner runner(FactoryAgentType type, FactoryAgentExecution execution) {
        return new DelegatingFactoryAgentRunner(type, execution);
    }
}
