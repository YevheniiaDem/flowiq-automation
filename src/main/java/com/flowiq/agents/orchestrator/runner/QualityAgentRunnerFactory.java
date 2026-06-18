package com.flowiq.agents.orchestrator.runner;

import com.flowiq.agents.ApiChangeDetectionAgent;
import com.flowiq.agents.architecture.ArchitectureDriftAgent;
import com.flowiq.agents.flaky.FlakyTestInvestigator;
import com.flowiq.agents.gap.TestGapAnalyzerAgent;
import com.flowiq.agents.generator.SmartTestGeneratorAgent;
import com.flowiq.agents.orchestrator.model.QualityAgentType;
import com.flowiq.agents.regressionrisk.RiskBasedRegressionAgent;
import com.flowiq.agents.release.ReleaseRiskAssessmentAgent;
import com.flowiq.agents.review.TestReviewAgent;
import com.flowiq.agents.rootcause.RootCauseAnalysisAgent;
import com.flowiq.agents.selfhealing.SelfHealingLocatorAgent;
import com.flowiq.agents.traceability.RequirementsTraceabilityAgent;

import java.util.List;

public final class QualityAgentRunnerFactory {

    private QualityAgentRunnerFactory() {
    }

    public static List<QualityAgentRunner> defaultRunners() {
        return List.of(
                new DelegatingAgentRunner(QualityAgentType.API_CHANGE_DETECTION,
                        () -> new ApiChangeDetectionAgent().run()),
                new DelegatingAgentRunner(QualityAgentType.TEST_GAP_ANALYZER,
                        () -> new TestGapAnalyzerAgent().run()),
                new DelegatingAgentRunner(QualityAgentType.FLAKY_TEST_INVESTIGATOR,
                        () -> new FlakyTestInvestigator().run()),
                new DelegatingAgentRunner(QualityAgentType.SMART_TEST_GENERATOR,
                        () -> new SmartTestGeneratorAgent().run()),
                new DelegatingAgentRunner(QualityAgentType.RELEASE_RISK_ASSESSMENT,
                        () -> new ReleaseRiskAssessmentAgent().run()),
                new DelegatingAgentRunner(QualityAgentType.REQUIREMENTS_TRACEABILITY,
                        () -> new RequirementsTraceabilityAgent().run()),
                new DelegatingAgentRunner(QualityAgentType.SELF_HEALING_LOCATOR,
                        () -> new SelfHealingLocatorAgent().run()),
                new DelegatingAgentRunner(QualityAgentType.TEST_REVIEW,
                        () -> new TestReviewAgent().run()),
                new DelegatingAgentRunner(QualityAgentType.ROOT_CAUSE_ANALYSIS,
                        () -> new RootCauseAnalysisAgent().run()),
                new DelegatingAgentRunner(QualityAgentType.RISK_BASED_REGRESSION,
                        () -> new RiskBasedRegressionAgent().run()),
                new DelegatingAgentRunner(QualityAgentType.ARCHITECTURE_DRIFT,
                        () -> new ArchitectureDriftAgent().run()));
    }
}
